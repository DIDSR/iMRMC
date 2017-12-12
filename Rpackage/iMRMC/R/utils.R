## createIMRMCdf ####
#' Convert a data frame with all needed factors to doIMRMC formatted data frame
#'
#' @param dFrame This data frame includes columns for readerID, caseID, modalityID, score, and truth.
#'        These columns are not expected to be named as such and other columns may exist.
#' @param keyColumns This list identifies the column names of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***", modalityID = "***", score = "***", truth="***")
#' @param truePositiveFactor The true positive label, such as "cancer" or "1"
#'
#' @return output a doIMRMC formatted data frame: rows for truth and rows for data
#'
#' @export
#'
# @examples
createIMRMCdf <- function(
  dFrame,
  keyColumns = list(readerID = "readerID", caseID = "caseID", modalityID = "modalityID", score = "score", truth = "truth"),
  truePositiveFactor = "cancer"
){

  readerID <- keyColumns$readerID
  caseID <- keyColumns$caseID
  modalityID <- keyColumns$modalityID
  score <- keyColumns$score
  truth <- keyColumns$truth

  data0 <- dFrame[dFrame[ , truth] != truePositiveFactor, ]
  data0 <- data.frame(
    readerID   = data0[ , readerID],
    caseID     = data0[ , caseID],
    modalityID = data0[ , modalityID],
    score      = data0[ , score]
  )
  data1 <- dFrame[dFrame[ , truth] == truePositiveFactor, ]
  data1 <- data.frame(
    readerID   = data1[ , readerID],
    caseID     = data1[ , caseID],
    modalityID = data1[ , modalityID],
    score      = data1[ , score]
  )
  truth0 <- data.frame(
    readerID = "truth",
    caseID = droplevels(unique(data0$caseID)),
    modalityID = "truth",
    score = 0
  )
  truth1 <- data.frame(
    readerID = "truth",
    caseID = droplevels(unique(data1$caseID)),
    modalityID = "truth",
    score = 1
  )
  IMRMCdf <- droplevels(rbind(truth0, truth1, data0, data1))
  IMRMCdf <- IMRMCdf[!is.na(IMRMCdf$score), ]

  return(IMRMCdf)

}

## undoIMRMCdf ####
#' Convert a doIMRMC formatted data frame to a standard data frame
#' with all factors.
#'
#' @details Delete rows specifying truth and put the truth information on every row.
#'
#' @param df.MRMC This data frame includes columns for readerID, caseID, modalityID, score.
#'        Each row is a reader x case x modality observation from the study
#'        In addition to observations from the study,
#'            this data frame requires rows specifying the truth for each caseID.
#'        For truth specifications, the readerID needs to equal "truth" or "-1",
#'            modalityID can be anything ("truth" is a good choice),
#'            and score should be 0 for signal-absent normal case, 1 for signal-present disease case.
#'
#' @return output a data frame with columns readerID, caseID, modalityID, score, truth
#'
#' @export
#'
# @examples
undoIMRMCdf <- function(df.MRMC) {

  # Separate the data frame into rows corresponding to truth
  # and rows corresping to observations
  df.Truth <- df.MRMC[df.MRMC$readerID == "-1", ]
  if (nrow(df.Truth) > 0) {
    df.Obs <- df.MRMC[df.MRMC$readerID != "-1", ]
  } else {
    df.Truth <- df.MRMC[df.MRMC$readerID == "truth"]
    df.Obs <- df.MRMC[df.MRMC$readerID != "truth", ]
  }

  df.Truth <- df.Truth[, c("caseID", "score")]
  names(df.Truth) <- c("caseID", "truth")

  df <- merge(df.Obs, df.Truth, by = "caseID")

  return(df)

}

## roc2binary ####
#' Convert ROC data formatted for doIMRMC to TPF and FPF data formatted for doIMRMC
#'
#' @param df.auc data frame of roc scores formatted for doIMRMC
#' @param threshold The threshold for determining binary decisions
#'
#' @return a list of two data frames (df.tpf and df.fpf) both formatted for doIMRMC
#' @export
#'
# @examples
roc2binary <- function(df.auc, threshold) {
  df.truth <- df.auc[df.auc$readerID == -1 | df.auc$readerID == "truth", ]
  df.scores <- df.auc[df.auc$readerID != -1 & df.auc$readerID != "truth", ]

  truth.neg <- droplevels(df.truth[df.truth$score == 0, ])
  cases.neg <- levels(truth.neg$caseID)
  nC.neg <- nlevels(truth.neg$caseID)

  truth.pos <- droplevels(df.truth[df.truth$score == 1, ])
  cases.pos <- levels(truth.pos$caseID)
  nC.pos <- nlevels(truth.pos$caseID)

  df.scores$pos <- 0
  df.scores$pos[df.scores$score > threshold] <- 1

  index.pos <- as.character(df.scores$caseID) %in% cases.pos
  index.neg <- as.character(df.scores$caseID) %in% cases.neg

  df.tpf <- df.scores[ , c("readerID", "caseID", "modalityID", "score")]
  df.tpf$score <- 0.5
  df.tpf$score[index.pos] <- df.scores$pos[index.pos]

  df.fpf <- df.scores[ , c("readerID", "caseID", "modalityID", "score")]
  df.fpf$score <- 0.5
  df.fpf$score[index.neg] <- df.scores$pos[index.neg]

  return(
    list(
      df.tpf = rbind(df.truth, df.tpf),
      df.fpf = rbind(df.truth, df.fpf)
    )
  )

}

## successDFtoROCdf ####
#' Convert an MRMC data frame of successes to one formatted for doIMRMC
#'
#' @param df Each row contains a success observation for one reader evaluating one case
#'
#' @return data frame ready for doIMRMC
#'
#' @export
#'
# @examples
successDFtoROCdf <- function(df) {

  # Successes are signal-present scores
  df.AUC <- data.frame(
    readerID = df$readerID,
    caseID = df$caseID,
    modalityID = df$modalityID,
    score = df$score
  )
  # Create 5 fake signal-absent observations per reader
  nC0 <- 5
  df.AUC <- rbind(
    data.frame(
      readerID = rep(levels(df$readerID), rep(nC0, nlevels(df$readerID))),
      caseID = rep(paste("fakeCase", 1:nC0, sep = ""), nC0),
      modalityID = rep("modalityA", nC0 * nlevels(df$readerID)),
      score = rep(0.5, nC0 * nlevels(df$readerID))
    ),
    df.AUC
  )
  # Create truth for signal-present cases
  df.AUC <- rbind(
    data.frame(
      readerID = rep("-1", nlevels(df$caseID)),
      caseID = levels(df$caseID),
      modalityID = rep("truth", nlevels(df$caseID)),
      score = rep(1, nlevels(df$caseID))
    ),
    df.AUC
  )
  # Create truth for signal-absent cases
  df.AUC <- rbind(
    data.frame(
      readerID = rep("-1", nC0),
      caseID = paste("fakeCase", 1:nC0, sep = ""),
      modalityID = rep("truth", nC0),
      score = rep(0, nC0)
    ),
    df.AUC
  )

  return(df.AUC)
}

