## createIMRMCdf ####
#' Convert a data frame with all needed factors to doIMRMC formatted data frame
#'
#' @param dFrame This data frame includes columns for readerID, caseID, modalityID, score, and truth.
#'        These columns are not expected to be named as such and other columns may exist.
#' @param keyColumns This list identifies the column names of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***", modalityID = "***", score = "***", truth="***")
#' @param truePositiveFactor The true positive label, such as "cancer" or "1"
#'
#' @return output a doIMRMC formatted data frame: rows for truth and rows for data.
#' The results will be an iMRMC formatted data frame, see \link{dfMRMC_example}
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
    score      = data0[ , score],
    stringsAsFactors = TRUE
  )
  data1 <- dFrame[dFrame[ , truth] == truePositiveFactor, ]
  data1 <- data.frame(
    readerID   = data1[ , readerID],
    caseID     = data1[ , caseID],
    modalityID = data1[ , modalityID],
    score      = data1[ , score],
    stringsAsFactors = TRUE
  )
  truth0 <- data.frame(
    readerID = "truth",
    caseID = droplevels(unique(data0$caseID)),
    modalityID = "truth",
    score = 0,
    stringsAsFactors = TRUE
  )
  truth1 <- data.frame(
    readerID = "truth",
    caseID = droplevels(unique(data1$caseID)),
    modalityID = "truth",
    score = 1,
    stringsAsFactors = TRUE
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
    df.Truth <- df.MRMC[df.MRMC$readerID == "truth", ]
    df.Obs <- df.MRMC[df.MRMC$readerID != "truth", ]
  }

  df.Truth <- df.Truth[, c("caseID", "score")]
  names(df.Truth) <- c("caseID", "truth")

  df <- droplevels(merge(df.Obs, df.Truth, by = "caseID"))

  return(df)

}

# Get a score from an MRMC data frame ####
#' Get a score from an MRMC data frame
#'
#' @param df An MRMC data frame
#'
#' @param iR The numeric index of the readerID
#'
#' @param iC The numeric index of the caseID
#'
#' @param modality The character description of the modalityID
#'
#' @return The score
#'
#' @export
#'
# @examples
# # Simulate an MRMC data frame
# simRoeMetz.config <- sim.gRoeMetz.config()
# dFrameMRMC <- sim.gRoeMetz(simRoeMetz.config)
# desc <- dFrameMRMC[
#   as.numeric(dFrameMRMC$readerID) == 2 &
#     as.numeric(dFrameMRMC$caseID) == 4 &
#     dFrameMRMC$modalityID == "testA",
# ]
# print(desc$score)
# print(getMRMCscore(dFrameMRMC, 2, 4, "testA"))

getMRMCscore <- function(df, iR, iC, modality) {
  score <- df[as.numeric(df$readerID) == iR &
                as.numeric(df$caseID) == iC &
                df$modalityID == modality, "score"]
  if (length(score) == 0) score <- NULL
  return(score)
}

# Convert an MRMC data frame to a score matrix ####
#' Convert an MRMC data frame to a score matrix
#'
#' @description Convert an MRMC data frame to a score matrix, dropping readers or cases with no observations
#'
#' @param dfMRMC An MRMC data frame
#'
#' @param modality The score matrix depends on the modality.
#'   If more than one modality exists in the data frame,
#'   you must specify which modality to subset.
#'
#' @param dropFlag [logical] The default setting (TRUE) removes readers and cases
#'   that have no observations. Dropping them by default will speed up analyses.
#'   Leaving the levels (dropFlag = FALSE) is useful if you need the entire score
#'   or design matrix when comparing or doing analyses with two modalities.
#'
#' @return A matrix that is \emph{nCases} by \emph{nReaders} of the scores each reader reported for each case
#'
#' @importFrom stats qnorm
#'
#' @export
#'
convertDFtoScoreMatrix <- function(dfMRMC, modality = NULL, dropFlag = TRUE) {

  # If modality is specified, subset the data on modalityID == modality
  if (!is.null(modality)) {
    dfMRMC <- dfMRMC[dfMRMC$modalityID == modality, ]
    dfMRMC$modalityID <- factor(dfMRMC$modalityID)
  }

  # Check that there is data from one modality only.
  if (nlevels(dfMRMC$modalityID) != 1) {
    desc <- paste("This function only treats data sets with one modality.\n",
                  "nlevels(dfMRMC$modalityID) =", nlevels(dfMRMC$modalityID), "\n")
    stop(desc)
  }

  # Dropping levels will remove readers or cases that have no observations
  # Dropping them by default will speed up analyses
  # Leaving the levels is useful if you want to see the entire score or design matrix
  if (dropFlag) {
    dfMRMC <- droplevels(dfMRMC)
  }

  caseIDs <- levels(dfMRMC$caseID)
  readerIDs <- levels(dfMRMC$readerID)
  nCases <- nlevels(dfMRMC$caseID)
  nReaders <- nlevels(dfMRMC$readerID)

  scores <- array(NA, c(nCases, nReaders), dimnames = list(caseIDs, readerIDs))

  index <- dfMRMC[ , c("caseID","readerID")]
  index <- data.matrix(index)

  scores[index] <- dfMRMC$score
  return(scores)

}

# Convert an MRMC data frame to a design matrix ####
#' Convert an MRMC data frame to a design matrix
#'
#' @description Convert an MRMC data frame to a design matrix, dropping readers or cases with no observations
#'
#' @param dfMRMC An MRMC data frame
#'
#' @param modality The score matrix depends on the modality.
#'   If more than one modality exists in the data frame,
#'   you must specify which modality to subset.
#'
#' @param dropFlag [logical] The default setting (TRUE) removes readers and cases
#'   that have no observations. Dropping them by default will speed up analyses.
#'   Leaving the levels (dropFlag = FALSE) is useful if you need the entire score
#'   or design matrix when comparing or doing analyses with two modalities.
#'
#' @return A matrix that is \emph{nCases} by \emph{nReaders} indicating which scores were reported for each reader and case
#'
#' @export
#'
convertDFtoDesignMatrix <- function(dfMRMC, modality = NULL, dropFlag = TRUE) {

  # If modality is specified, subset the data on modalityID == modality
  if (!is.null(modality)) {
    dfMRMC <- dfMRMC[dfMRMC$modalityID == modality, ]
  }

  # Reduce the modality factor to what is actually contained
  dfMRMC$modalityID <- factor(dfMRMC$modalityID)

  # Check that there is data from one modality only.
  if (nlevels(dfMRMC$modalityID) != 1) {
    desc <- paste("This function only treats data sets with one modality.\n",
                  "nlevels(dfMRMC$modalityID) =", nlevels(dfMRMC$modalityID), "\n")
    stop(desc)
  }

  # Dropping levels will remove readers or cases that have no observations
  # Dropping them by default will speed up analyses
  # Leaving the levels is useful if you want to see the entire score or design matrix
  if (dropFlag) {
    dfMRMC <- droplevels(dfMRMC)
  }

  caseIDs <- levels(dfMRMC$caseID)
  readerIDs <- levels(dfMRMC$readerID)
  nCases <- nlevels(dfMRMC$caseID)
  nReaders <- nlevels(dfMRMC$readerID)

  design <- array(0, c(nCases, nReaders), dimnames = list(caseIDs, readerIDs))

  index <- dfMRMC[ , c("caseID","readerID")]
  index <- data.matrix(index)

  design[index] <- 1

  return(design)

}

## Extract between-reader between-modality pairs of scores ####
#' Extract between-reader between-modality pairs of scores
#'
#' @param data0 This data frame includes columns for readerID, caseID, modalityID, score.
#'
#' @param modalities The modalities (testA, testB) for the scores to be paired
#'
#' @param keyColumns This list identifies the column names
#' of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***",
#'             modalityID = "***", score = "***", truth="***")
#'
#' @return A data frame of all paired observations.
#'   Each observation comes from a pair of readers evaluating a case in two modalities.
#'   The first column corresponds to one reader evaluating the case in testA.
#'   The second column corresonds to the other reader evaluating the case in testB.
#'
#' @export
#'
# @examples
extractPairedComparisonsBRBM <- function(
    data0,
    modalities = c("testA", "testB"),
    keyColumns = list(readerID = "readerID",
                      caseID = "caseID",
                      modalityID = "modalityID",
                      score = "score")
) {

  # Establish the key column names
  readerID <- keyColumns$readerID
  caseID <- keyColumns$caseID
  modalityID <- keyColumns$modalityID
  score <- keyColumns$score

  # Create an MRMC data frame
  dfMRMC <- data.frame(
    readerID   = data0[ , readerID],
    caseID     = data0[ , caseID],
    modalityID = data0[ , modalityID],
    score      = data0[ , score],
    stringsAsFactors = TRUE
  )

  # Split the data by the input modalities
  df.X <- dfMRMC[dfMRMC$modalityID == modalities[1], ]
  df.Y <- dfMRMC[dfMRMC$modalityID == modalities[2], ]

  # Split the data by readers
  df.XR <- split(df.X, list(df.X$readerID), drop = TRUE)
  nReadersX <- length(df.XR)
  readersX <- names(df.XR)

  # Split the data by readers
  df.YR <- split(df.Y, list(df.Y$readerID), drop = TRUE)
  nReadersY <- length(df.YR)
  readersY <- names(df.YR)

  # For each pair of distinct readers, merge the data
  x <- NULL
  y <- NULL
  for (readerXi in readersX) {
    for (readerYj in readersY) {

      if (readerXi == readerYj) next()

      df.RxR <- merge(df.XR[[readerXi]], df.YR[[readerYj]], by = "caseID")
      x <- c(x, df.RxR$score.x)
      y <- c(y, df.RxR$score.y)

    }

  }

  return(data.frame(x = x, y = y, stringsAsFactors = TRUE))

}

## Extract within-reader between-modality pairs of scores ####
#' Extract within-reader between-modality pairs of scores
#'
#' @param data0 This data frame includes columns for readerID, caseID, modalityID, score.
#'
#' @param modalities The modalities (testA, testB) for the scores to be paired
#'
#' @param keyColumns This list identifies the column names
#' of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***",
#'             modalityID = "***", score = "***", truth="***")
#'
#' @return A data frame of all paired observations.
#'   Each observation comes from a one reader evaluating a case in two modalities
#'   The first column corresponds to one reader evaluating the case in "testA".
#'   The second column corresonds to the same reader evaluating the case in "testB".
#'
#' @export
#'
# @examples
extractPairedComparisonsWRBM <- function(
    data0,
    modalities = "testA",
    keyColumns = list(readerID = "readerID",
                      caseID = "caseID",
                      modalityID = "modalityID",
                      score = "score")
) {

  readerID <- keyColumns$readerID
  caseID <- keyColumns$caseID
  modalityID <- keyColumns$modalityID
  score <- keyColumns$score

  dfMRMC <- data.frame(
    readerID   = data0[ , readerID],
    caseID     = data0[ , caseID],
    modalityID = data0[ , modalityID],
    score      = data0[ , score],
    stringsAsFactors = TRUE
  )

  # Pool reader counts into a contingency table
  modality.X <- modalities[1]
  modality.Y <- modalities[2]

  # Split the data by modalities
  df.X <- dfMRMC[dfMRMC$modalityID == modality.X,]
  df.Y <- dfMRMC[dfMRMC$modalityID == modality.Y,]

  # Split the data by readers
  df.XR <- split(df.X, df.X$readerID, drop = TRUE)
  nReadersX <- length(df.XR)
  readersX <- names(df.XR)

  df.YR <- split(df.Y, df.Y$readerID, drop = TRUE)
  nReadersY <- length(df.YR)
  readersY <- names(df.YR)

  reader.i <- 1
  reader.j <- 2

  x <- NULL
  y <- NULL
  for (readerXi in readersX) {
    for (readerYj in readersY) {

      if (readerXi != readerYj) next()

      df.RxR <- merge(df.XR[[readerXi]], df.YR[[readerYj]], by = "caseID")
      x <- c(x, df.RxR$score.x)
      y <- c(y, df.RxR$score.y)

    }

  }

  return(data.frame(x = x, y = y, stringsAsFactors = TRUE))

}

## createGroups ####
#' Assign a group label to items in a vector
#'
#' @param items A vector of items
#' @param nG The number of groups
#'
#' @return A data frame containing the items and their group labels
#' @export
#'
#' @examples
#' x <- paste("item", 1:10, sep = "")
#' df <- createGroups(x, 3)
#' print(df)
#'
createGroups <- function(items, nG) {

  n <- length(items)

  # Determine the number of items in each group
  nPerG.base <- floor(n/nG)
  remainder <- n - nPerG.base*nG
  nPerG <- rep(nPerG.base, nG)
  if (remainder) nPerG[1:remainder] <- nPerG[1:remainder] + 1

  # Create labels for each reader
  desc <- NULL
  for (i in 1:nG) desc <- c(desc, rep(paste("group", i, sep = ""), nPerG[i]))

  readerGroups <- data.frame(items = items, desc = desc, stringsAsFactors = TRUE)

  return(readerGroups)

}

## renameCol ####
#' Rename a data frame column name or a list object name
#'
#' @param df A data frame
#'
#' @param oldColName Old column name
#'
#' @param newColName New column name
#'
#' @return the data frame with the updated column name
#'
#' @export
#'
# @examples
renameCol <- function(df, oldColName, newColName) {
  names(df)[names(df) == oldColName] <- newColName
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
#' @examples
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Convert ROC MRMC data to TPF and FPF data frames
#' result <- roc2binary(dFrame.imrmc, threshold = 0.9)
#' # Analyze TPF data using doIMRMC
#' tpf_result <- doIMRMC(result$df.tpf)
#' # View(tpf_result$perReader)
#
roc2binary <- function(df.auc, threshold) {

  # Separate truth and reader scores
  df.truth <- df.auc[df.auc$readerID == -1 | df.auc$readerID == "truth", ]
  df.scores <- df.auc[df.auc$readerID != -1 & df.auc$readerID != "truth", ]

  # Separate truth negative and truth positive cases
  truth.neg <- droplevels(df.truth[df.truth$score == 0, ])
  cases.neg <- unique(truth.neg$caseID)

  # if(TRUE) print("check that number of unique cases = number of truth cases.
  #              Insert MRMC format data check.")
  ####
  #### Check that each reader reads each case only once per modality
  #### This also checks that each case only has one truth
  #### If there are duplicates, throw an error
  ####
  index <- duplicated(df.auc[, c("readerID", "caseID", "modalityID")])
  if (any(index)) {
    desc <- paste(
      "\n Some readers have read a case more than once in some modality.",
      "\n Check these rows:",
      "\n", paste((1:nrow(df.auc))[index], collapse = ", ")
    )
    stop(desc)
  }

  ####
  #### Check that the number of unique cases = number of truth cases
  ####
  if(nrow(df.truth) != length(unique(df.scores$caseID))) {
    desc <- paste(
      "\n There is an uneven number of truth cases and unique reader cases.")
    stop(desc)
  }

  truth.pos <- droplevels(df.truth[df.truth$score == 1, ])
  cases.pos <- unique(truth.pos$caseID)

  # Determine if score is above/equiv./below given threshold
  df.scores$pos <- 0
  df.scores$pos[df.scores$score == threshold] <- 0.5
  df.scores$pos[df.scores$score > threshold] <- 1

  # Index positive truth cases and negative truth cases in df.scores (readers)
  index.pos <- as.character(df.scores$caseID) %in% cases.pos
  index.neg <- as.character(df.scores$caseID) %in% cases.neg

  # Calculate TPF dataframe
  # Use positive truth index and pos (threshold adjusted) column
  # Negative truth rows receive score of 0.5
  df.tpf <- df.scores[ , c("readerID", "caseID", "modalityID", "score", "pos")]
  df.tpf$success <- df.tpf$pos
  df.tpf$score[index.neg] <- 0.5
  df.tpf$score[index.pos] <- df.tpf$pos[index.pos]
  df.tpf <- df.tpf[, c("readerID", "caseID", "modalityID", "score")]

  # Calculate FPF dataframe
  # Use negative truth index and pos (threshold adjusted) column
  # Positive truth rows receive score of 0.5
  df.fpf <- df.scores[ , c("readerID", "caseID", "modalityID", "score", "pos")]
  df.fpf$success <- df.fpf$pos
  df.fpf$score[index.pos] <- 0.5
  df.fpf$score[index.neg] <- df.fpf$pos[index.neg]
  df.fpf <- df.fpf[, c("readerID", "caseID", "modalityID", "score")]

  # Build dataframes of truth and TPF or FPF
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
    score = df$score,
    stringsAsFactors = TRUE
  )
  # Create 5 fake signal-absent observations per reader
  nC0 <- 5
  df.AUC <- rbind(
    data.frame(
      readerID = rep(levels(df$readerID), rep(nC0, nlevels(df$readerID))),
      caseID = rep(paste("fakeCase", 1:nC0, sep = ""), nC0),
      modalityID = rep("modalityA", nC0 * nlevels(df$readerID)),
      score = rep(0.5, nC0 * nlevels(df$readerID)),
      stringsAsFactors = TRUE
    ),
    df.AUC
  )
  # Create truth for signal-present cases
  df.AUC <- rbind(
    data.frame(
      readerID = rep("-1", nlevels(df$caseID)),
      caseID = levels(df$caseID),
      modalityID = rep("truth", nlevels(df$caseID)),
      score = rep(1, nlevels(df$caseID)),
      stringsAsFactors = TRUE
    ),
    df.AUC
  )
  # Create truth for signal-absent cases
  df.AUC <- rbind(
    data.frame(
      readerID = rep("-1", nC0),
      caseID = paste("fakeCase", 1:nC0, sep = ""),
      modalityID = rep("truth", nC0),
      score = rep(0, nC0),
      stringsAsFactors = TRUE
    ),
    df.AUC
  )

  return(df.AUC)
}

## createDFdocumentation ####
createDFdocumentation <- function(df) {

  desc1 <- utils::capture.output(utils::str(df))
  nVar <- length(desc1) - 1

  desc2 <- c(
    paste("#\' \\code{", deparse(substitute(df)), "} is a", desc1[1], "\\cr \n"),
    "#\'   \\itemize{ \\cr \n",
    paste("#\'     \\item \\code{", desc1[2:nVar], "} \\cr \n"),
    "#\'   }"
  )

  cat(desc2)

  return(desc2)

}


## deleteCol ####
#' Delete a data frame column
#'
#' @param df A data frame
#'
#' @param colName Column name or list of column names to be deleted
#'
#' @return The data frame without the deleted column or columns
#'
#' @export
#'
# @examples
deleteCol <- function(df, colName) {
  df <- df[ , -which(names(df) %in% c(colName))]
  return(df)
}


## getMRMCdataset ####
#' Import MRMC dataset from the web (https://github.com/DIDSR/iMRMC/wiki/iMRMC-Datasets)
#'
#' @param dataset Possible dataset options available:
#' \itemize{
#'   \item \strong{pilotHTT}: Breast cancer annotation data that is the aggregate of all clean data from the HTT project pilot study. https://github.com/DIDSR/HTT
#'   \item \strong{viperObs}: Individual observations of each reader reading each case from the FDA Validation of Imaging Premarket Evaluation and Regulation (VIPER) Study. https://github.com/DIDSR/viperData
#'   \item \strong{viperObs365}: Individual observations of each reader reading each case from the FDA Validation of Imaging Premarket Evaluation and Regulation (VIPER) Study, truth is based on cancer status at 365 days. https://github.com/DIDSR/viperData
#'   \item \strong{viperObs455}: Individual observations of each reader reading each case from the FDA Validation of Imaging Premarket Evaluation and Regulation (VIPER) Study, truth is based on cancer status at 455 days. https://github.com/DIDSR/viperData
#'   \item \strong{MFcounts_dfClassify}: A data frame comparing mitotic figure counting performance based on whole slide images (WSI images) from four scanners to the counts from a microscope, data is per  candidate mitotic figure and modality. https://github.com/DIDSR/mitoticFigureCounts/tree/master
#'   \item \strong{MFcounts_dfCountROI}: A data frame comparing mitotic figure counting performance based on whole slide images (WSI images) from four scanners to the counts from a microscope, data is per ROI and modality. https://github.com/DIDSR/mitoticFigureCounts/tree/master
#'   \item \strong{MFcounts_dfCountWSI}: A data frame comparing mitotic figure counting performance based on whole slide images (WSI images) from four scanners to the counts from a microscope, data is per WSI and modality. https://github.com/DIDSR/mitoticFigureCounts/tree/master
#'   \item \strong{cardioStudyTruth}: Cardio CT data comparing display color scale effects on diagnostic performance and reader agreement, ground truth data. https://github.com/DIDSR/colorScaleStudyData
#'   \item \strong{cardioStudyRawData}: Cardio CT data comparing display color scale effects on diagnostic performance and reader agreement, reader data. https://github.com/DIDSR/colorScaleStudyData
#'   \item \strong{prostateTruth}: Prostate MRI data comparing display color scale effects on diagnostic performance and reader agreement, ground truth data. https://github.com/DIDSR/colorScaleStudyData
#'   \item \strong{prostateRawData}: Prostate MRI data comparing display color scale effects on diagnostic performance and reader agreement, reader data. https://github.com/DIDSR/colorScaleStudyData
#' }
#'
#' @return desired dataset downloaded from the web as a csv
#'
#' @import utils
#' @export
#'
#' @examples
#' # Save Prostate MRI ground truth and reader data
#' truthData <- getMRMCdataset("prostateTruth")
#' rawData <- getMRMCdataset("prostateRawData")
#
getMRMCdataset <- function(dataset = c("pilotHTT", "viperObs", "viperObs365",
                                       "viperObs455", "MFcounts_dfClassify",
                                       "MFcounts_dfCountROI", "MFcounts_dfCountWSI",
                                       "cardioStudyTruth", "cardioStudyRawData",
                                       "prostateTruth", "prostateRawData")){

  if(dataset == "pilotHTT") {
    link <- "https://raw.githubusercontent.com/DIDSR/HTT/main/inst/extdata/pilotHTT.csv"
    pilotHTT <- read.csv(link)
    print("Annotation data that is the aggregate of all clean data from the HTT project pilot study.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/HTT")

    return(pilotHTT)
  }

  if(dataset == "viperObs") {
    link <- "https://raw.githubusercontent.com/DIDSR/viperData/master/inst/data-raw/viperObs.csv"
    viperObs <- read.csv(link)
    print("Individual observations of each reader reading each case. Simplified and merged version of data based on `viperObs365` and `viperObs455`.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/viperData")

    return(viperObs)
  }

  if(dataset == "viperObs365") {
    link <- "https://raw.githubusercontent.com/DIDSR/viperData/master/inst/data-raw/viperObs365.csv"
    viperObs365 <- read.csv(link)
    viperObs365 <- viperObs365[, -1]
    print("Individual observations of each reader reading each case. Truth labels are based on cancer at 365 days.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/viperData")

    return(viperObs365)
  }

  if(dataset == "viperObs455") {
    link <- "https://raw.githubusercontent.com/DIDSR/viperData/master/inst/data-raw/viperObs455.csv"
    viperObs455 <- read.csv(link)
    viperObs455 <- viperObs455[, -1]
    print("Individual observations of each reader reading each case. Truth labels are based on cancer at 455 days.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/viperData")

    return(viperObs455)
  }

  if(dataset == "MFcounts_dfClassify") {
    link <- "https://raw.githubusercontent.com/DIDSR/mitoticFigureCounts/master/data/dfClassify20180627.csv"
    MFcounts_dfClassify <- read.csv(link)
    print("A single data frame of the study data. Each row corresponds to a candidate mitotic figure and modality (155 candidates x 5 modalities = 775 rows). There is a column for each observer and the truth.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/mitoticFigureCounts/tree/master")

    return(MFcounts_dfClassify)
  }

  if(dataset == "MFcounts_dfCountROI") {
    link <- "https://raw.githubusercontent.com/DIDSR/mitoticFigureCounts/master/data/dfCountROI20180627.csv"
    MFcounts_dfCountROI <- read.csv(link)
    print("A data frame of the mitotic figure counts per ROI and modality (40 ROIs x 5 modalities = 200 rows). There is a column for each observer and the truth.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/mitoticFigureCounts/tree/master")

    return(MFcounts_dfCountROI)
  }

  if(dataset == "MFcounts_dfCountWSI") {
    link <- "https://raw.githubusercontent.com/DIDSR/mitoticFigureCounts/master/data/dfCountWSI20180627.csv"
    MFcounts_dfCountWSI <- read.csv(link)
    print("A single data frame of the mitotic figure counts per WSI and modality (4 WSIs x 5 modalities = 20 rows). There is a column for each observer and the truth. ")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/mitoticFigureCounts/tree/master")

    return(MFcounts_dfCountWSI)
  }

  if(dataset == "cardioStudyTruth") {
    link <- "https://raw.githubusercontent.com/DIDSR/colorScaleStudyData/master/data-raw/Cardio%20Study_truth.csv"
    cardioStudyTruth <- read.csv(link)
    print("Truth of each case in study. From left to right each column corresponds to case number and truth. 1 is assigned to cases with lesion (positive cases) and 0 to cases without lesion (negative cases).")
    print("You may also want to get the `cardioStudyRawData` dataset.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/colorScaleStudyData")

    names(cardioStudyTruth) <- c("caseID", "score")
    cardioStudyTruth$readerID <- "truth"
    cardioStudyTruth$modalityID <- "truth"

    return(cardioStudyTruth)
  }

  if(dataset == "cardioStudyRawData") {
    link <- "https://raw.githubusercontent.com/DIDSR/colorScaleStudyData/master/data-raw/CardioCTstudy_3mod_12obs_210cases_rawdata.csv"
    cardioStudyRawData <- read.csv(link)
    print("Observations of each case in study. From left to right each column corresponds to reader, case number, modality (color scale), confidence score assigned. Grayscale was evaluated using GSDF settings and Rainbow and Hotiron using RGB settings.")
    print("You may also want to get the `cardioStudyTruth` dataset.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/colorScaleStudyData")

    names(cardioStudyRawData) <- c("readerID", "caseID", "modalityID", "score")

    return(cardioStudyRawData)
  }

  if(dataset == "prostateTruth") {
    link <- "https://raw.githubusercontent.com/DIDSR/colorScaleStudyData/master/data-raw/Prostate_Truth.csv"
    prostateTruth <- read.csv(link)
    print("Truth of each case in study. From left to right each column corresponds to case number and truth. 1 is assigned to cases with lesion (positive cases) and 0 to cases without lesion (negative cases).")
    print("You may also want to get the `prostateRawData` dataset.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/colorScaleStudyData")

    names(prostateTruth) <- c("caseID", "score")
    prostateTruth$readerID <- "truth"
    prostateTruth$modalityID <- "truth"

    return(prostateTruth)
  }

  if(dataset == "prostateRawData") {
    link <- "https://raw.githubusercontent.com/DIDSR/colorScaleStudyData/master/data-raw/Prostate_5mod_9obs_165cases_wGSDFrawdata_Allreaders.csv"
    prostateRawData <- read.csv(link)
    print("Observations of each case in the study. From left to right each column corresponds to reader, case number, modality (color scale), confidence score assigned. Unless otherwise stated in the modality name Grayscale was evaluated using GSDF settings and Rainbow and Hotiron using RGB settings.")
    print("You may also want to get the `prostateTruth` dataset.")
    print("Repository where you can find more information about the data: https://github.com/DIDSR/colorScaleStudyData")

    names(prostateRawData) <- c("readerID", "caseID", "modalityID", "score")
    return(prostateRawData)
  }

    print("The given data name is not an MRMC data object from
          https://github.com/DIDSR/iMRMC/wiki/iMRMC-Datasets.")
    print("Please input one of the specified data names.")

}
