#' doIMRMC takes ROC data as a data frame and runs a multi-reader multi-case analysis
#' based on U-statistics as described in the following papers
#' Gallas2006_Acad-Radiol_v13p353 (single-modality),
#' Gallas2008_Neural-Networks_v21p387 (multiple modalities, arbitrary study designs),
#' Gallas2009_Commun-Stat-A-Theor_v38p2586 (framework paper).
#'
#' In detail, this procedure writes the ROC data frame to the local file system formatted
#' for the iMRMC program (found at https://github.com/DIDSR/iMRMC/releases), it executes the iMRMC
#' program which writes the results to the local files system, it reads the analysis results from
#' the local file system, packs the analysis results into a list object, deletes the data and analysis results
#' from the local file system, and returns the list object.
#'
#' @param dFrame.imrmc [data.frame] the variables contained in this data frame are
#'            readerID       [Factor] w/ nR levels "reader1", "reader2", ...
#'            caseID         [Factor] w/ nC levels "case1", "case2", ...
#'            modalityID     [Factor] w/ 1 level simRoeMetz.config$modalityID
#'            score          [num] reader score
#'            each row of this data frame corresponds to an observation
#'            for every caseID, there must be a row corresponding to the truth observation
#'              the readerID for a truth observation is "truth"
#'              the modalityID for a truth observation is "truth"
#'              the score for a truth observation must be either 0 (signal-absent) or 1 (signal-present)
#'
#' @param iMRMCjarFullPath [char] this string identifies the location of the iMRMC.jar file
#'            this jar file can be downloaded from https://github.com/DIDSR/iMRMC/releases
#'            this R program supports version iMRMC-v3p2.jar
#' @param cleanUp [logi] this logical determines whether or not the iMRMC analysis results
#' @param show.output.on.console [logi] this logical determines whether or iMRMC console output
#'               is written to the console
#'
#' @return iMRMCoutput [list] the objects of this list are described in detail in the iMRMC documentation
#'            which can be found at http://didsr.github.io/iMRMC/000_iMRMC/userManualHTML/index.htm
#'
#'            Here is a quick summary:
#'            perReader [data.frame] this data frame contains the performance results for each reader.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.
#'            Ustat [data.frame] this data frame contains the reader-average performance results.
#'              The analysis results are based on U-statistics and the papers listed above.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.
#'            MLEstat [data.frame] this data frame contains the reader-average performance results.
#'              The analysis results are based on V-statistics, which approximates the true distribution with
#'              the empirical distribution. The empirical distribution equals the nonparametric MLE
#'              estimate of the true distribution, which is also equivalent to the ideal bootstrap estimate.
#'              PLease refer to the papers listed above.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.
#'            ROC [list] each object of this list is an object containing an ROC curve.
#'              There is an ROC curve for every combination of reader and modality.
#'              For every modality, there are also four average ROC curves. These are discussed in
#'                Chen2014_Br-J-Radiol_v87p20140016
#'                The diagonal average averages the reader-specific ROC curves along y = -x + b for b in (0,1)
#'                The horizontal average averages the reader specific ROC curves along y = b for b in (0,1)
#'                The vertical average averages the reader specific ROC curves along x = b for b in (0,1)
#'                The pooled average ignores readerID and pools all the scores together to create one ROC curve.
#'            varDecomp [list] the objects of this list are different decompositions of the total variance
#'              Please refer to Gallas2009_Commun-Stat-A-Theor_v38p2586 (framework paper).
#'              The different decompositions are BCK, BDG, DBM, MS, OR.
#'
#'
#' @export
#'
# @examples
#'
#'
doIMRMC <- function(dFrame.imrmc, iMRMCjarFullPath = NULL, cleanUp = TRUE, show.output.on.console = FALSE){

  iMRMCfolderName <- "imrmcDir"
  iMRMCfileName <- "input.imrmc"

  if (is.null(iMRMCjarFullPath)) {
    iMRMCjar <- "iMRMC-v3p2.jar"
    pkgPath = path.package("iMRMC", quiet = FALSE)
    iMRMCjarFullPath = paste(pkgPath, "/java/", iMRMCjar, sep = "")
  }

  # Clean up files before calling iMRMC
  unlink(iMRMCfolderName, recursive = TRUE)

  # Write data frame to iMRMC input file
  writeLines("BEGIN DATA:", con = iMRMCfileName)
  write.table(dFrame.imrmc, iMRMCfileName,
              quote = FALSE, row.names = FALSE, col.names = FALSE,
              append = TRUE, sep = ", ")

  # Run iMRMC
  system(
    paste("java -jar ", iMRMCjarFullPath, iMRMCfileName, iMRMCfolderName),
    show.output.on.console = show.output.on.console)

  # Retrieve the iMRMC results
  perReader <- read.csv(file.path(iMRMCfolderName, "AUCperReader.csv"))
  Ustat <- read.csv(file.path(iMRMCfolderName, "statAnalysis.csv"))
  MLEstat <- read.csv(file.path(iMRMCfolderName, "statAnalysisMLE.csv"))
  ROCraw <- read.csv(file.path(iMRMCfolderName, "ROCcurves.csv"),
                     header = FALSE, row.names = NULL, skip = 1)
  BCK <- read.csv(file.path(iMRMCfolderName, "BCKtable.csv"), row.names = NULL)
  BDG <- read.csv(file.path(iMRMCfolderName, "BDGtable.csv"), row.names = NULL)
  DBM <- read.csv(file.path(iMRMCfolderName, "DBMtable.csv"), row.names = NULL)
  MS <- read.csv(file.path(iMRMCfolderName, "MStable.csv"), row.names = NULL)
  OR <- read.csv(file.path(iMRMCfolderName, "ORtable.csv"), row.names = NULL)

  ROC <- by(ROCraw, ROCraw[, 1], function(x) {
    list(desc = x[1,1], n = x[1,2],
         fpf = as.numeric(x[x[,3] == "FPF", 4:(3 + x[1,2])]),
         tpf = as.numeric(x[x[,3] == "TPF", 4:(3 + x[1,2])])
      )

  })

  # Delete the content written to disk
  if (cleanUp == TRUE) {
    unlink(iMRMCfolderName, recursive = TRUE)
    unlink(iMRMCfileName)
  }

  varDecomp <- list(
    BCK = BCK,
    BDG = BDG,
    DBM = DBM,
    MS = MS,
    OR = OR
  )

  iMRMCoutput <- list(
    perReader = perReader,
    Ustat = Ustat,
    MLEstat = MLEstat,
    ROC = ROC,
    varDecomp = varDecomp
  )

}

#' Title
#'
#' @param dFrame This data frame includes columns for readerID, caseID, modalityID, score, and truth.
#'        These columns are not expected to be named as such and other columns may exist.
#' @param dataColumnNames This list identifies the column names of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***", modalityID = "***", score = "***", truth="***")
#' @param truePositiveFactor The true positive label, such as "cancer" or "1"
#'
#' @return
#'
#' @export
#'
# @examples
createIMRMCdf <- function(dFrame, dataColumnNames, truePositiveFactor){

  readerID <- dataColumnNames$readerID
  caseID <- dataColumnNames$caseID
  modalityID <- dataColumnNames$modalityID
  score <- dataColumnNames$score
  truth <- dataColumnNames$truth

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


#' @param dFrame This data frame includes columns for readerID, caseID, modalityID, score, and truth.
#'        These columns are not expected to be named as such and other columns may exist.
#' @param dataColumnNames This list identifies the column names of the data frame to be used for the analysis.
#'        list(readerID = "***", caseID = "***", modalityID = "***", score = "***", truth="***")
#' @param truePositiveFactor The true positive label, such as "cancer" or "1"

#' Title
#'
#' @param df.MRMC This data frame includes columns for readerID, caseID, modalityID, score.
#'        Each row is a reader x case x modality observation from the study
#'        In addition to observations from the study,
#'            this data frame requires rows specifying the truth for each caseID.
#'        For truth specifications, the readerID needs to equal "truth" or "-1",
#'            modalityID can be anything ("truth" is a good choice),
#'            and score should be 0 for signal-absent normal case, 1 for signal-present disease case.
#'
#' @return
#'
#' @export
#'
# @examples
undoIMRMCdf <- function(df.MRMC) {
  df.Truth <- df.MRMC[df.MRMC$readerID == -1, ]
  df.Orig <- df.MRMC[df.MRMC$readerID != -1, ]
  df.Orig <- apply(df.Orig, 1, function(x, df.Truth) {

    truth <- df.Truth$score[df.Truth$readerID == "-1" & df.Truth$caseID == x[2]]
    x[5] <- truth
    names(x)[5] <- "truth"

    return(x)

  }, df.Truth)

  df.Orig <- data.frame(t(df.Orig))
  df.Orig$truth <- factor(df.Orig$truth)
  df.Orig$score <- as.numeric(as.character(df.Orig$score))

  return(df.Orig)

}
