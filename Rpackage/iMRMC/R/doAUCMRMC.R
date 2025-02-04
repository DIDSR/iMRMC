## doAUCmrmc ##############################################################
#' MRMC analysis for arbitrary design dataset
#'
#'
#'
#' @description Execute a Multi-Reader, Multi-Case (MRMC) analysis
#' of ROC data from imaging studies where clinicians (readers) evaluate patient
#' images (cases). An overview of this software, including references presenting
#' details on the methods, can be found \href{https://zenodo.org/record/8383591}{HERE}
#' or as an entry in the FDA/CDRH Regulatory Science Tool Catalog
#' \href{https://cdrh-rst.fda.gov/imrmc-software-do-multi-reader-multi-case-statistical-analysis-reader-studies}{HERE}.
#'
#'
#'
#' @param data an iMRMC formatted data frame, see \link{dfMRMC_example}
#'
#'
#' @param flagROC boolean indicating if ROC results should be computed.
#'
#' @return The MRMC analysis results as a list, below is a quick summary:
#' \itemize{
#'   \item {\code{summaryMRMC, list}
#'    a list of summary study design information.
#'     \itemize{
#'       \item {\code{nM, num} number of modalities}
#'       \item {\code{nR, num} number of readers}
#'       \item {\code{nC.neg, num} number of signal-present caeses}
#'       \item {\code{nC.pos, num} number of signal-absent cases}
#'       \item {\code{modalites, char} names of modalities}
#'       \item {\code{readers, char} names of modalities}
#'       \item {\code{cases.neg, char} names of modalities}
#'       \item {\code{cases.pos, char} names of modalities}
#'     }
#'   }
#'   \item {\code{perReader.full, data.frame}
#'    this data frame contains the performance results for each reader and modality comparison.
#'              The analysis returns the final AUC results and the moments, coefficients of those moments. 
#'              Key variables of this data frame are AUC.1 (where '.1' indicates the row's reader and 
#'              modality '.1' pair), AUC.2 ('.2' indicates the '.2' reader and modality pair), and covAUC.}
#'   \item {\code{Ustat.full, data.frame}
#'    this data frame contains the reader-average AUC performance results.
#'              The analysis results are based on U-statistics.
#'              Key variables of this data frame are AUC.1, AUC.2, AUC1minusAUC2 and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.}
#'   \item {\code{ROC, list}
#'    each object of this list is an object containing an ROC curve.
#'              There is an ROC curve for every combination of reader and modality.
#'              For every modality, there are also four average ROC curves. These are discussed in
#'                Chen2014_Br-J-Radiol_v87p20140016.
#'                The diagonal average averages the reader-specific ROC curves along y = -x + b for b in (0,1).
#'                The horizontal average averages the reader specific ROC curves along y = b for b in (0,1).
#'                The vertical average averages the reader specific ROC curves along x = b for b in (0,1).
#'                The pooled average ignores readerID and pools all the scores together to create one ROC curve.
#'   }
#' }
#'
#' @importFrom utils read.csv
#'
#' @export
#' 
#' @examples
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data and compute ROC curves
#' aucResult <- doAUCmrmc(dFrame.imrmc, flagROC = TRUE)
#' 
#'
doAUCmrmc = function(data, flagROC = FALSE){
  
  
  # initialize and check the data #############################################
  
  # If data is character, it is a file name. Read in data.
  if (inherits(data, "character")) {
    
    # Open and read the file.
    conn <- file(data, open = "r")
    desc <- readLines(conn)
    
    # Find the line number of "BEGIN DATA:". The lines following are the data.
    skipIndex <- grep("BEGIN DATA:", desc)
    close(conn)
    
    # This file is expected to be .imrmc format
    data <- utils::read.csv(data, header = FALSE, skip = skipIndex,
                     col.names = c("readerID", "caseID", "modalityID", "score"),
                     colClasses = c("factor", "factor", "factor", "numeric"))
    
  }
  
  
  
  ####
  #### Factorize readerID, caseID, and modalityID
  ####
  data$readerID <- factor(data$readerID)
  data$caseID <- factor(data$caseID)
  data$modalityID <- factor(data$modalityID)
  
  
  
  ####
  #### Check that each reader reads each case only once per modality
  #### This also checks that each case only has one truth
  #### If there are duplicates, throw an error
  ####
  index <- duplicated(data[, c("readerID", "caseID", "modalityID")])
  if (any(index)) {
    desc <- paste(
      "\n Some readers have read a case more than once in some modality.",
      "\n Check these rows:",
      "\n", paste((1:nrow(data))[index], collapse = ", ")
    )
    stop(desc)
  }
  
  
  
  ####
  #### Initialize modalities
  ####
  modalities <- levels(data$modalityID)
  # Trim leading and trailing white space from modalities
  modalities <- trimws(modalities)
  
  
  
  # Check that one of the modalities is "truth".
  # If yes, remove it from the modalities.
  # If no, throw an error.
  if ("truth" %in% modalities) {
    modalities <- modalities[modalities != "truth"]
    nM <- length(modalities)
  } else {
    stop(paste("\n  There is no truth modality."))
  }
  
  
  
  ####
  #### Initialize readers
  ####
  readers = levels(data$readerID)
  # Trim leading and trailing white space from readers
  readers <- trimws(readers)
  
  
  
  # Check that one of the readers is "truth".
  # If yes, remove it from the modalities
  # If no, throw an error.
  if ("truth" %in% readers) {
    readers <- readers[readers != "truth"]
    nR <- length(readers)
  } else {
    stop(paste("\n  There is no truth reader"))
  }
  
  
  
  ####
  #### Split the MRMC data into truth and not truth data frames
  ####
  truthDF <- data[data$modalityID == "truth",]
  truthDF$readerID <- factor(truthDF$readerID)
  truthDF$caseID <- factor(truthDF$caseID)
  truthDF$modalityID <- factor(truthDF$modalityID)
  
  mrmcDF <- data[data$modalityID != "truth",]
  mrmcDF$readerID <- factor(mrmcDF$readerID)
  mrmcDF$caseID <- factor(mrmcDF$caseID)
  mrmcDF$modalityID <- factor(mrmcDF$modalityID)
  
  
  
  ####
  #### Check that all the MRMC data has truth.
  #### If not, throw an error.
  ####
  index.TF <- !(levels(mrmcDF$caseID) %in% levels(truthDF$caseID))
  if (any(index.TF)) {
    print("These cases do not have truth")
    print(levels(mrmcDF$caseID)[index.TF])
    stop("\n  Not all cases have truth")
    
  }
  
  

  ####
  #### Initialize case and counts
  ####
  cases.neg = factor(truthDF[truthDF$score == 0, "caseID"])
  nC.neg = nlevels(cases.neg)
  
  cases.pos = factor(truthDF[truthDF$score == 1, "caseID"])
  nC.pos = length(cases.neg)
  
  
  
  ####
  #### Add truth label to MRMC data
  ####
  mrmcDF$truthLabel <- 0
  mrmcDF$truthLabel[mrmcDF$caseID %in% cases.pos] <- 1
  
  
  
  ####
  #### Pack summaryMRMC structure
  ####
  summaryMRMC <- list(
    nM = nM,
    nR = nR,
    nC.neg = nC.neg,
    nC.pos = nC.pos,
    modalities = modalities,
    readers = readers,
    cases.neg = cases.neg,
    cases.pos = cases.pos
  )

  
  # ROC #####################################################################

  if (flagROC) ROC <- doROCcurveMRMC(mrmcDF)
  
  
  
  # Single modality loop ###########################################################
  
  # Split the data by modality
  mrmcDF.byModality <- split(mrmcDF, mrmcDF$modalityID)
  
  perReader.full <- data.frame()
  Ustat.full <- data.frame()
  for (modality.1 in 1:nM) {
    
    #### 
    #### Select the modality x data
    ####
    modalityID.1 <-  modalities[modality.1]
    mrmcDF.modality.1 <- mrmcDF.byModality[[modality.1]]
    
    
    
    ####
    #### Get basic study design info for modality 1
    #### 
    summaryMRMC.1 <- getStudyBasics(mrmcDF.modality.1)    
    
    
    
    ####
    #### Isolate the number of readers, negative cases, and positive cases
    #### 
    studySize <- data.frame(
      NR = summaryMRMC.1$nR,
      N0 = summaryMRMC.1$nC.neg,
      N1 = summaryMRMC.1$nC.pos)
    
    
    
    ####
    #### Per-reader analysis for each modality
    #### 
    perReaderPair.1 <- doAUCperReader(mrmcDF.modality.1, mrmcDF.modality.1)
    
    
    
    #### 
    #### Ustat MRMC analysis for each modality
    #### 
    Ustat.pair <- doAUCmrmcCov(studySize, perReaderPair.1)
    
    
    
    
    ####
    #### Implement Hypothesis test: Normal Approximation
    #### 
    unbiased.H0.normal <- hypothesisTest_Normal(
      m = Ustat.pair$AUC.1,
      s2 = Ustat.pair$covAUC,
      significance = 0.05,
      H0 = 0.5)
    biased.H0.normal <- hypothesisTest_Normal(
      m = Ustat.pair$AUC.1,
      s2 = Ustat.pair$covAUC.biased,
      significance = 0.05,
      H0 = 0.5)
    biased.H0.normal <- data.frame(
      botCInormal.biased = biased.H0.normal$botCInormal,
      topCInormal.biased = biased.H0.normal$topCInormal,
      pValueNormal.biased = biased.H0.normal$pValueNormal,
      rejectNormal.biased = biased.H0.normal$rejectNormal
    )
    
    
    ####
    #### Implement Hypothesis test: t-test with dfBDG
    #### 
    unbiased.H0.t <- hypothesisTest_t(
      m = Ustat.pair$AUC.1,
      s2 = Ustat.pair$covAUC,
      df = Ustat.pair$dfBDG,
      significance = 0.05,
      H0 = 0.5)
    biased.H0.t <- hypothesisTest_t(
      m = Ustat.pair$AUC.1,
      s2 = Ustat.pair$covAUC.biased,
      df = Ustat.pair$dfBDG.biased,
      significance = 0.05,
      H0 = 0.5)
    biased.H0.t <- data.frame(
      botCIBDG.biased = biased.H0.t$botCIBDG,
      topCIBDG.biased = biased.H0.t$topCIBDG,
      pValueBDG.biased = biased.H0.t$pValueBDG,
      rejectBDG.biased = biased.H0.t$rejectBDG
    )
    
    
    
    ####
    #### Aggregate Ustat.pair results
    #### 
    Ustat.pair <- cbind(studySize, Ustat.pair,
                        unbiased.H0.normal, unbiased.H0.t,
                        biased.H0.normal, biased.H0.t)
    
    
    
    ####
    #### Concatenate results
    #### 
    perReader.full <- rbind(perReader.full, perReaderPair.1)
    Ustat.full <- rbind(Ustat.full, Ustat.pair)
    
  }
  
  
  
  # Paired modalities loops ###################################################
  if (nM > 1) for (modality.1 in 1:(nM - 1)) {
    
    #### 
    #### Select the modality x data
    ####
    modalityID.1 <-  modalities[modality.1]
    mrmcDF.modality.1 <- mrmcDF.byModality[[modality.1]]
    
    for (modality.2 in (modality.1 + 1):nM) {
      
      #### 
      #### Select the modality y data
      ####
      modalityID.2 <-  modalities[modality.2]
      mrmcDF.modality.2 <- mrmcDF.byModality[[modality.2]]
      
      
      
      ####
      #### Get basic study design info for modality 1 + modality 2
      #### 
      summaryMRMC.1 <- getStudyBasics(rbind(mrmcDF.modality.1, mrmcDF.modality.2))
      
      
      
      ####
      #### Isolate the number of readers, negative cases, and positive cases
      #### 
      studySize <- data.frame(
        NR = summaryMRMC.1$nR,
        N0 = summaryMRMC.1$nC.neg,
        N1 = summaryMRMC.1$nC.pos)
      
      
      ####
      #### Per-reader analysis for modality 1 =/= modality 2
      #### 
      perReaderPair.12 <- doAUCperReader(mrmcDF.modality.1, mrmcDF.modality.2)
      
      

      #### 
      #### Ustat MRMC analysis for modality 1 =/= modality 2
      #### 
      Ustat.pair <- doAUCmrmcCov(studySize, perReaderPair.12)

      
      ####
      #### Revise the degrees of freedom for AUC differences 
      #### This is only necessary for two modalities.
      #### 
      Ustat.pair <- doDFdifference(studySize, Ustat.pair, Ustat.full)
      
      
      
      ####
      #### Implement Hypothesis test: Normal Approximation
      #### 
      unbiased.H0.normal <- hypothesisTest_Normal(
        m = Ustat.pair$AUC1minusAUC2,
        s2 = Ustat.pair$varAUC1minusAUC2,
        significance = 0.05,
        H0 = 0.0)
      biased.H0.normal <- hypothesisTest_Normal(
        m = Ustat.pair$AUC1minusAUC2,
        s2 = Ustat.pair$varAUC1minusAUC2.biased,
        significance = 0.05,
        H0 = 0.0)
      biased.H0.normal <- data.frame(
        botCInormal.biased = biased.H0.normal$botCInormal,
        topCInormal.biased = biased.H0.normal$topCInormal,
        pValueNormal.biased = biased.H0.normal$pValueNormal,
        rejectNormal.biased = biased.H0.normal$rejectNormal
      )
      
      
      
      ####
      #### Implement Hypothesis test: t-test with dfBDG
      #### 
      unbiased.H0.t <- hypothesisTest_t(
        m = Ustat.pair$AUC1minusAUC2,
        s2 = Ustat.pair$varAUC1minusAUC2,
        df = Ustat.pair$dfBDG.diff,
        significance = 0.05,
        H0 = 0.0)
      biased.H0.t <- hypothesisTest_t(
        m = Ustat.pair$AUC1minusAUC2,
        s2 = Ustat.pair$varAUC1minusAUC2.biased,
        df = Ustat.pair$dfBDG.biased.diff,
        significance = 0.05,
        H0 = 0.0)
      biased.H0.t <- data.frame(
        botCIBDG.biased = biased.H0.t$botCIBDG,
        topCIBDG.biased = biased.H0.t$topCIBDG,
        pValueBDG.biased = biased.H0.t$pValueBDG,
        rejectBDG.biased = biased.H0.t$rejectBDG
      )
      
      
      
      ####
      #### Aggregate Ustat.pair results
      #### 
      Ustat.pair <- cbind(studySize, Ustat.pair,
                          unbiased.H0.normal, unbiased.H0.t,
                          biased.H0.normal, biased.H0.t)
      
      
      
      ####
      #### Concatenate results
      #### 
      perReader.full <- rbind(perReader.full, perReaderPair.12)
      Ustat.full <- rbind(Ustat.full, Ustat.pair)
      
    }
    
  }
  
  # Return ####################################################################
  result <- list(
    summaryMRMC = summaryMRMC,
    perReader.full = perReader.full,
    Ustat.full = Ustat.full
  )
  if (flagROC) result$ROC <- ROC
  
  return(result)
  
}

getStudyBasics <- function(mrmcDF) {
  
  # Readers
  readers = as.character(unique(mrmcDF$readerID))
  nR = length(readers)
  
  # Cases
  cases.neg = as.character(unique(
    mrmcDF[mrmcDF$truthLabel == 0, "caseID"]))
  nC.neg = length(cases.neg)
  
  cases.pos = as.character(unique(
    mrmcDF[mrmcDF$truthLabel == 1, "caseID"] ))
  nC.pos = length(cases.pos)
  
  # Cases per reader
  mrmcDF.byReader <- split(mrmcDF, mrmcDF$readerID)
  
  nC.neg.perReader <- unlist(lapply(
    mrmcDF.byReader, function(x) {
      sum(x$truthLabel == 0)
    }))
  
  nC.pos.perReader <- unlist(lapply(
    mrmcDF.byReader, function(x) {
      sum(x$truthLabel == 1)
    }))
  
  result <- list(
    nR = nR,
    nC.neg = nC.neg,
    nC.pos = nC.pos,
    nC.neg.perReader = nC.neg.perReader,
    nC.pos.perReader = nC.pos.perReader,
    readers = readers,
    cases.neg = cases.neg,
    cases.pos = cases.pos
  )
  
}

doAUC <- function(df.1 , df.2) {
  
  ####
  #### Identify the positive and negative cases in the data
  #### 
  cases.neg.1 <- df.1[df.1$truthLabel == 0,
                      c("caseID", "score")]
  
  cases.pos.1 <- df.1[df.1$truthLabel == 1,
                      c("caseID", "score")]
  
  cases.neg.2 <- df.2[df.2$truthLabel == 0,
                      c("caseID", "score")]
  
  cases.pos.2 <- df.2[df.2$truthLabel == 1,
                      c("caseID", "score")]
  
  cases.neg <- merge(
    cases.neg.1, cases.neg.2,
    by.x = "caseID", by.y = "caseID",
    suffixes = c(".1", ".2"),
    all = TRUE
  )
  
  cases.pos <- merge(
    cases.pos.1, cases.pos.2,
    by.x = "caseID", by.y = "caseID",
    suffixes = c(".1", ".2"),
    all = TRUE
  )
  
  
  
  ####
  #### Number of negative and positive cases evaluated by both readers
  #### 
  cases.neg$both <- !is.na(cases.neg$score.1 * cases.neg$score.2)
  N0 <- sum(cases.neg$both)
  
  cases.pos$both <- !is.na(cases.pos$score.1 * cases.pos$score.2)
  N1 <- sum(cases.pos$both)
  
  
  
  ####
  #### Success matrix for reader x
  #### 
  diff_matrix.1 <- -outer(cases.neg[, "score.1"], cases.pos[, "score.1"], "-")
  
  # The paired comparisons that are concordant with truth
  index.C.TF <- (diff_matrix.1 > 0)
  
  # The paired comparisons that are tied
  index.T.TF <- (diff_matrix.1 == 0)
  
  success_matrix.1 <- 0 * diff_matrix.1
  success_matrix.1[index.C.TF] <- 1.0
  success_matrix.1[index.T.TF] <- 0.5
  
  
  
  ####
  #### Success matrix for reader y
  #### 
  diff_matrix.2 <- -outer(cases.neg[, "score.2"], cases.pos[, "score.2"], "-")
  
  # The paired comparisons that are concordant with truth
  index.C.TF <- (diff_matrix.2 > 0)
  
  # The paired comparisons that are tied
  index.T.TF <- (diff_matrix.2 == 0)
  
  success_matrix.2 <- 0 * diff_matrix.2
  success_matrix.2[index.C.TF] <- 1.0
  success_matrix.2[index.T.TF] <- 0.5
  
  
  
  ####
  #### Numerators of the AUCs for readers x and y
  #### 
  numer.AUC.1 <- sum(success_matrix.1, na.rm = TRUE)
  numer.AUC.2 <- sum(success_matrix.2, na.rm = TRUE)
  
  
  
  ####
  #### Numerators of the biased moments
  #### 
  numer <- c(
    sum(success_matrix.1 * success_matrix.2, na.rm = TRUE),
    sum(colSums(success_matrix.1, na.rm = TRUE) *
          colSums(success_matrix.2, na.rm = TRUE)),
    sum(rowSums(success_matrix.1, na.rm = TRUE) *
          rowSums(success_matrix.2, na.rm = TRUE)),
    numer.AUC.1 * numer.AUC.2
  )
  names(numer) <- c("numer1", "numer2", "numer3", "numer4")
  
  
  
  ####
  #### Convert success matrices to design matrices
  #### 
  index.TF <- is.na(success_matrix.1)
  success_matrix.1[!index.TF] <- 1
  index.TF <- is.na(success_matrix.2)
  success_matrix.2[!index.TF] <- 1
  
  
  
  ####
  #### denominators of the AUCs for readers x and y
  #### 
  denom.AUC.1 <- sum(success_matrix.1, na.rm = TRUE)
  denom.AUC.2 <- sum(success_matrix.2, na.rm = TRUE)
  
  
  
  # The number of elements in the biased moment sums
  denom <- c(
    sum(success_matrix.1 * success_matrix.2, na.rm = TRUE),
    sum(colSums(success_matrix.1, na.rm = TRUE) *
          colSums(success_matrix.2, na.rm = TRUE)),
    sum(rowSums(success_matrix.1, na.rm = TRUE) *
          rowSums(success_matrix.2, na.rm = TRUE)),
    denom.AUC.1 * denom.AUC.2
  )
  names(denom) <- c("denom1", "denom2", "denom3", "denom4")
  
  
  
  ####
  #### AUC for readers x and y
  #### 
  AUC.1 <- numer.AUC.1 / denom.AUC.1
  AUC.2 <- numer.AUC.2 / denom.AUC.2
  
  
  
  ####
  #### Transformation matrix: map biased sums to unbiased sums
  #### 
  B <- matrix(c(
    1,  0,  0,  0,
    -1,  1,  0,  0,
    -1,  0,  1,  0,
    1, -1, -1,  1
  ), nrow = 4, ncol = 4, byrow = T)
  
  
  
  ####
  #### Map biased sums to unbiased sums
  ####
  numer.U <- c(B %*% numer)
  denom.U <- c(B %*% denom)
  
  
  
  ####
  #### Unbiased moments
  #### 
  m <- numer.U / denom.U
  
  
  
  ####
  #### The moment coefficients
  #### 
  coeff <- denom.U / denom[4]
  coeff[4] <- coeff[4] - 1
  
  
  
  ####
  #### Covariance of AUC
  #### 
  varAUC = sum(coeff * m)
  
  
  
  #### Return result
  result <- data.frame(
    readerID.1 = df.1$readerID[1],
    readerID.2 = df.2$readerID[1],
    N0 = N0,
    N1 = N1,
    modalityID.1 = df.1$modalityID[1],
    modalityID.2 = df.2$modalityID[1],
    AUC.1 = AUC.1,
    AUC.2 = AUC.2,
    covAUC = varAUC,
    M1 = m[1],
    M2 = m[2],
    M3 = m[3],
    M4 = m[4],
    coeff1 = coeff[1],
    coeff2 = coeff[2],
    coeff3 = coeff[3],
    coeff4 = coeff[4],
    numer.AUC.1 = numer.AUC.1,
    numer.AUC.2 = numer.AUC.2,
    numer1 = numer[1],
    numer2 = numer[2],
    numer3 = numer[3],
    numer4 = numer[4],
    denom.AUC.1 = denom.AUC.1,
    denom.AUC.2 = denom.AUC.2,
    denom1 = denom[1],
    denom2 = denom[2],
    denom3 = denom[3],
    denom4 = denom[4],
    row.names = NULL
  )
  
  
  
  return(result)
  
}



doAUCperReader <- function(mrmcDF.modality.1, mrmcDF.modality.2) {
  
  mrmcDF.i.byReader <- split(
    mrmcDF.modality.1,
    mrmcDF.modality.1$readerID,
    drop = TRUE
  )
  
  mrmcDF.j.byReader <- split(
    mrmcDF.modality.2,
    mrmcDF.modality.2$readerID,
    drop = TRUE
  )
  
  # Loop over reader.1
  result.byReader <- lapply(
    mrmcDF.i.byReader,
    mrmcDF.j.byReader = mrmcDF.j.byReader,
    function(df.1, mrmcDF.j.byReader) {
      
      # Loop over reader.2
      result.RR <- lapply(
        mrmcDF.j.byReader,
        df.1 = df.1,
        function(df.1, df.2) doAUC(df.1, df.2)
      )
      result.RR <- do.call(rbind, result.RR)
      row.names(result.RR) <- NULL
      
      return(result.RR)
      
    }
  )
  perReader <- do.call(rbind, result.byReader)
  row.names(perReader) <- NULL
  
  return(perReader)
  
}



doAUCmrmcCov <- function(studySize, perReaderPair.1) {
  
  modalityID.1 <- perReaderPair.1$modalityID.1[1]
  modalityID.2 <- perReaderPair.1$modalityID.2[1]
  
  ####
  #### Numerators and denominators for the biased product moments
  #### 
  
  # Identify the rows where readerID.1 == readerID.2
  index.TF <- perReaderPair.1$readerID.1 == perReaderPair.1$readerID.2
  
  # Sum over all the rows where readerID.1 == readerID.2
  numer1234 <- colSums(
    perReaderPair.1[index.TF, c("numer1", "numer2", "numer3", "numer4")])
  denom1234 <- colSums(
    perReaderPair.1[index.TF, c("denom1", "denom2", "denom3", "denom4")])
  
  # Sum over all pairs of readers, including readerID.1 == readerID.2
  numer5678 <- colSums(
    perReaderPair.1[, c("numer1", "numer2", "numer3", "numer4")])
  names(numer5678) <- c("numer5", "numer6", "numer7", "numer8")
  denom5678 <- colSums(
    perReaderPair.1[, c("denom1", "denom2", "denom3", "denom4")])
  names(denom5678) <- c("denom5", "denom6", "denom7", "denom8")
  
  numer <- c(numer1234, numer5678)
  denom <- c(denom1234, denom5678)
  
  
  
  ####
  #### Reader-averaged AUC
  ####
  AUC.1 <- sum(perReaderPair.1$numer.AUC.1) / sum(perReaderPair.1$denom.AUC.1)
  AUC.2 <- sum(perReaderPair.1$numer.AUC.2) / sum(perReaderPair.1$denom.AUC.2)
  
  
  
  ####
  #### Biased moments
  #### 
  index.TF <- denom != 0
  m.biased <- numer
  m.biased[index.TF] <- m.biased[index.TF] / denom[index.TF]
  

  
  ####
  #### Transformation: map biased sums to unbiased sums
  #### 
  B <- matrix(c(
    1,  0,  0,  0,  0,  0,  0,  0,
    -1,  1,  0,  0,  0,  0,  0,  0,
    -1,  0,  1,  0,  0,  0,  0,  0,
    1, -1, -1,  1,  0,  0,  0,  0,
    -1,  0,  0,  0,  1,  0,  0,  0,
    1, -1,  0,  0, -1,  1,  0,  0,
    1,  0, -1,  0, -1,  0,  1,  0,
    -1,  1,  1, -1,  1, -1, -1,  1
  ), nrow = 8, ncol = 8, byrow = T)
  
  
  
  ####
  #### Map biased sums to unbiased sums
  ####
  numer.U <- c(B %*% numer)
  denom.U <- c(B %*% denom)
  
  
  
  ####
  #### Unbiased moments
  #### 
  index.TF <- denom.U != 0
  m <- numer.U
  m[index.TF] <- m[index.TF] / denom.U[index.TF]
  
  
  
  
  ####
  #### The moment coefficients
  #### 
  m.coeff <- denom.U / denom[8]
  m.coeff[8] <- m.coeff[8] - 1
  
  
  
  ####
  #### Unbiased and Biased mrmc variance estimates
  #### 
  covAUC <- sum(m.coeff * m)
  covAUC.biased <- sum(m.coeff * m.biased)
  
  
  
  ####
  #### Calculate BCK components of variance
  #### 
  
  # This matrix transforms BDG moments to BCK components
  # Equation 19 in Gallas2009_Commun-Stat-A-Theor_v38p2586
  B_alpha = matrix(c(0,0,0,0,0,0,1,-1,
                     0,0,0,0,0,1,0,-1,
                     0,0,0,0,1,-1,-1,1,
                     0,0,0,1,0,0,0,-1,
                     0,0,1,-1,0,0,-1,1,
                     0,1,0,-1,0,-1,0,1,
                     1,-1,-1,1,-1,1,1,-1),nrow = 7,ncol = 8,byrow = T)
  BCK <- B_alpha %*% m
  BCK.biased <- B_alpha %*% m.biased
  
  
  
  # This matrix transforms BDG coefficients to BCK coefficients
  B_BDG2BCK = matrix(c(
    1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0 ,
    1.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0 ,
    1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 ,
    1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0 ,
    1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 ,
    1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 ,
    1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    nrow = 7, ncol = 8,byrow = T)
  BCK.coeff <- B_BDG2BCK %*% m.coeff
  
  
  
  ###
  ### Convert data vectors into data frames with column names
  ### 
  numer <- data.frame(t(numer))
  names(numer) <- c(
    "numer1", "numer2", "numer3", "numer4",
    "numer5", "numer6", "numer7", "numer8")
  
  denom <- data.frame(t(denom))
  names(denom) <- c(
    "denom1", "denom2", "denom3", "denom4",
    "denom5", "denom6", "denom7", "denom8")
  
  m <- data.frame(t(m))
  names(m) <- c(
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  m.biased <- data.frame(t(m.biased))
  names(m.biased) <- c(
    "M1.b", "M2.b", "M3.b", "M4.b",
    "M5.b", "M6.b", "M7.b", "M8.b"
  )
  
  m.coeff <- data.frame(t(m.coeff))
  names(m.coeff) <- c(
    "M1.coeff", "M2.coeff", "M3.coeff", "M4.coeff",
    "M5.coeff", "M6.coeff", "M7.coeff", "M8.coeff")
  
  BCK = data.frame(t(BCK))
  names(BCK) <- c("BCK.N", "BCK.D", "BCK.ND", "BCK.R",
                  "BCK.NR", "BCK.DR", "BCK.RND")
  
  BCK.biased = data.frame(t(BCK.biased))
  names(BCK.biased) <- c("BCK.N.b", "BCK.D.b", "BCK.ND.b", "BCK.R.b",
                         "BCK.NR.b", "BCK.DR.b", "BCK.RND.b")
  
  BCK.coeff <- data.frame(t(BCK.coeff))
  names(BCK.coeff) <- c("BCK.N.coeff", "BCK.D.coeff", "BCK.ND.coeff", "BCK.R.coeff",
                        "BCK.NR.coeff", "BCK.DR.coeff", "BCK.RND.coeff")
  
  
  
  # Manage studies that do not pair readers or cases across modalities
  index <- BCK.coeff[1, ] == 0
  BCK[1, index] <- 0
  BCK.biased[1, index] <- 0
  
  
  
  ####
  #### Degrees of freedom
  ####
  # REFER TO
  # https://github.com/DIDSR/iMRMC/blob/4cb4f112db86cc13ca4d242df8dcf19ee5d31d07/src/mrmc/core/StatTest.java
  # Starting on Line 581
  # Saved to iMRMC/Rpackage/iMRMC/inst/extra/000-non-inferiority-by-iMRMC
  #### 
  dfN <- round((1/BCK.coeff$BCK.N.coeff - 1))
  dfD <- round((1/BCK.coeff$BCK.D.coeff - 1))
  dfR <- round((1/BCK.coeff$BCK.R.coeff - 1))

  dfBDG.min <- min(dfN, dfD, dfR)
  
  sb.N <- BCK.biased$BCK.N.b^2 / dfN^3
  sb.D <- BCK.biased$BCK.D.b^2 / dfD^3
  sb.R <- BCK.biased$BCK.R.b^2 / dfR^3
  
  
  
  ####
  #### Check for negative variances. Return NA for dfBDG and dfBDG.biased
  ####
  if (covAUC < 0) {
    dfBDG <- NA
  } else {
    dfBDG <- covAUC^2 / (sb.N + sb.D + sb.R)
    
    # Check if dfBDG is below a minimum.
    # If true, replace with minimum.
    # This follows Gaylor1969_Technometrics_v4p691
    if (dfBDG < dfBDG.min) {
      dfBDG <- dfBDG.min
    }
    
  }
  
  if (covAUC.biased < 0) {
    dfBDG.biased <- NA
  } else {
    dfBDG.biased <- covAUC.biased^2 / (sb.N + sb.D + sb.R)

    # Check if dfBDG is below a minimum.
    # If true, replace with minimum.
    # This follows Gaylor1969_Technometrics_v4p691
    if (dfBDG.biased < dfBDG.min) {
      dfBDG.biased <- dfBDG.min
    }
    
  }
  
  df <- data.frame(
    dfN = dfN, dfD = dfD, dfR = dfR,
    dfBDG = dfBDG, dfBDG.biased = dfBDG.biased)
  
  df.diff <- data.frame(
    AUC1minusAUC2 = NA,
    varAUC1minusAUC2 = NA,
    varAUC1minusAUC2.biased = NA,
    dfN.diff = NA, dfD.diff = NA, dfR.diff = NA,
    dfBDG.diff = NA, dfBDG.biased.diff = NA)
  
  
  
  ####
  #### Ustat: Pack the return result
  #### 
  Ustat <- data.frame(
    modalityID.1 = modalityID.1,
    modalityID.2 = modalityID.2,
    AUC.1 = AUC.1,
    AUC.2 = AUC.2,
    covAUC = covAUC,
    covAUC.biased = covAUC.biased
  )
  
  Ustat <- cbind(Ustat, df, df.diff,
                 m, m.biased, m.coeff,
                 BCK, BCK.biased, BCK.coeff,
                 numer, denom)
  
  
  ####
  #### Return
  #### 
  return(Ustat)
  
}



doDFdifference <- function(studySize, Ustat.pair, Ustat.full) {
  
  ####
  #### Calculate the difference: AUC modality 1 - AUC modality 2
  #### and the variance of the difference
  #### 
  modalityID.1 <- Ustat.pair$modalityID.1
  modalityID.2 <- Ustat.pair$modalityID.2
  
  # Get the Ustat results for each modality
  index.TF <- 
    (Ustat.full$modalityID.1 == modalityID.1) &
    (Ustat.full$modalityID.2 == modalityID.1)
  Ustat.1 <- Ustat.full[index.TF, ]
  index.TF <- 
    (Ustat.full$modalityID.1 == modalityID.2) &
    (Ustat.full$modalityID.2 == modalityID.2)
  Ustat.2 <- Ustat.full[index.TF, ]
  
  AUC1minusAUC2 <- Ustat.1$AUC.1 - Ustat.2$AUC.1
  
  varAUC.1 <- Ustat.1$covAUC
  varAUC.2 <- Ustat.2$covAUC
  covAUC.12 <- Ustat.pair$covAUC
  
  varAUC.1.biased <- Ustat.1$covAUC.biased
  varAUC.2.biased <- Ustat.2$covAUC.biased
  covAUC.12.biased <- Ustat.pair$covAUC.biased
  
  varAUC1minusAUC2 <- c(
    varAUC.1 + varAUC.2 - 2 * covAUC.12)
  varAUC1minusAUC2.biased <- (
    varAUC.1.biased + varAUC.2.biased - 2 * covAUC.12.biased)
  

  
  ####
  #### Calculate dfBDG for AUC1minusAUC2
  # REFER TO
  # https://github.com/DIDSR/iMRMC/blob/4cb4f112db86cc13ca4d242df8dcf19ee5d31d07/src/mrmc/core/StatTest.java
  # Starting on Line 581
  # Saved to iMRMC/Rpackage/iMRMC/inst/extra/000-non-inferiority-by-iMRMC
  #### 
  dfN = min(Ustat.1$dfN, Ustat.2$dfN)
  dfD = min(Ustat.1$dfD, Ustat.2$dfD)
  dfR = min(Ustat.1$dfR, Ustat.2$dfR)

  dfBDG.min <- min(dfN, dfD, dfR)

  # Method depends on whether normal cases are paired across modalities
  if (Ustat.pair$BCK.N.coeff > 0) {
    sb.N <- (Ustat.1$BCK.N.b + Ustat.2$BCK.N.b - 2 * Ustat.pair$BCK.N.b)^2/(dfN)^3
  } else {
    sb.N <- sum(c(
      (Ustat.1$BCK.N.b / Ustat.1$dfN)^2 / Ustat.1$dfN,
      (Ustat.2$BCK.N.b / Ustat.2$dfN)^2 / Ustat.2$dfN
    ))
  }
  
  # Method depends on whether disease cases are paired across modalities
  if (Ustat.pair$BCK.D.coeff > 0) {
    sb.D <- (Ustat.1$BCK.D.b + Ustat.2$BCK.D.b - 2 * Ustat.pair$BCK.D.b)^2/(dfD)^3
  } else {
    sb.D <- sum(c(
      (Ustat.1$BCK.D.b / Ustat.1$dfD)^2 / Ustat.1$dfD
      (Ustat.2$BCK.D.b / Ustat.2$dfD)^2 / Ustat.2$dfD
    ))
  }
  
  # Method depends on whether readers are paired across modalities
  if (Ustat.pair$BCK.R.coeff > 0) {
    sb.R <- (Ustat.1$BCK.R.b + Ustat.2$BCK.R.b - 2 * Ustat.pair$BCK.R.b)^2/(dfR)^3
  } else {
    sb.R <- sum(c(
      (Ustat.1$BCK.R.b / Ustat.1$dfR)^2 / Ustat.1$dfR,
      (Ustat.2$BCK.R.b / Ustat.2$dfR)^2 / Ustat.2$dfR
    ))
  }
  
  ####
  #### Check for negative variances. Return NA for dfBDG and dfBDG.biased
  ####
  if (varAUC1minusAUC2 < 0) {
    dfBDG <- NA
  } else {
    dfBDG <- varAUC1minusAUC2^2 / (sb.N + sb.D + sb.R)

    # Check if dfBDG is below a minimum.
    # If true, replace with minimum.
    # This follows Gaylor1969_Technometrics_v4p691
    if (dfBDG < dfBDG.min) {
      dfBDG <- dfBDG.min
    }

  }

  if (varAUC1minusAUC2.biased < 0) {
    dfBDG.biased <- NA
  } else {
    dfBDG.biased <- varAUC1minusAUC2.biased^2 / (sb.N + sb.D + sb.R)

    # Check if dfBDG is below a minimum.
    # If true, replace with minimum.
    # This follows Gaylor1969_Technometrics_v4p691
    if (dfBDG.biased < dfBDG.min) {
      dfBDG.biased <- dfBDG.min
    }

  }
  
  
  
  #### 
  #### Package into data frame
  ####
  Ustat.pair$AUC1minusAUC2 <- AUC1minusAUC2
  Ustat.pair$varAUC1minusAUC2 <- varAUC1minusAUC2
  Ustat.pair$varAUC1minusAUC2.biased <- varAUC1minusAUC2.biased
  Ustat.pair$dfBDG.diff <- dfBDG
  Ustat.pair$dfBDG.biased.diff <- dfBDG.biased
  Ustat.pair$dfN.diff <- dfN
  Ustat.pair$dfD.diff <- dfD
  Ustat.pair$dfR.diff <- dfR
  
  return(Ustat.pair)
}



hypothesisTest_Normal <- function(m, s2, significance, H0, verbose = FALSE) {
  
  if (s2 < 0) {
    
    if (verbose == TRUE) {
      stop("The input variance is negative.")
    }
    
    result <- data.frame(
      botCInormal = NA,
      topCInormal = NA,
      pValueNormal = NA,
      rejectNormal = NA)
    
    return(result)
    
  }  
  
  botCI = m - stats::qnorm(1 - significance / 2) * sqrt(s2)
  topCI = m + stats::qnorm(1 - significance / 2) * sqrt(s2)
  
  tStat = (m - H0) / sqrt(s2)
  
  pValue = (1 - stats::pnorm(abs(tStat))) * 2
  
  reject <- as.numeric(pValue <= significance)
  
  result <- data.frame(
    botCInormal = botCI,
    topCInormal = topCI,
    pValueNormal = pValue,
    rejectNormal = reject)
  
  return(result)  
}

hypothesisTest_t <- function(m, s2, df, significance, H0, verbose = FALSE) {
  
  if (s2 < 0) {
    
    if (verbose == TRUE) {
      stop("The input variance is negative.")
    }
    
    result <- data.frame(
      botCIBDG = NA,
      topCIBDG = NA,
      pValueBDG = NA,
      rejectBDG = NA)
    
    return(result)
    
  }  
  
  if (df > 50) {
    
    result.H0.normal <- hypothesisTest_Normal(m, s2, significance, H0)
    result.H0.t <- data.frame(
      botCIBDG = result.H0.normal$botCInormal,
      topCIBDG = result.H0.normal$topCInormal,
      pValueBDG = result.H0.normal$pValueNormal,
      rejectBDG = result.H0.normal$rejectNormal
    )
    
    return(result.H0.t)
  }
  
  topCI = (m + stats::qt(1 - significance / 2, df = floor(df)) * sqrt(s2))
  botCI = (m - stats::qt(1 - significance / 2, df = floor(df)) * sqrt(s2))
  
  tStat = (m - H0) / sqrt(s2)
  
  pValue = (1 - stats::pt(abs(tStat),df = floor(df)))*2
  
  reject <- as.numeric(pValue <= significance)
  
  result <- data.frame(
    botCIBDG = botCI,
    topCIBDG = topCI,
    pValueBDG = pValue,
    rejectBDG = reject)
  
}
