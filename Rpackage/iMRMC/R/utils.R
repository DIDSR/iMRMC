#' roc2binary
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

#' successDFtoROCdf
#'
#' @param df Each row contains a success observation for one reader evaluating one case
#'
#' @return data frame ready for doIMRMC
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

