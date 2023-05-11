#### doIMRMC function ####
#' MRMC analysis of the area under the ROC curve
#'
#' @description doIMRMC takes ROC data as a data frame and runs a multi-reader multi-case analysis
#' based on U-statistics as described in the following papers
#' Gallas2006_Acad-Radiol_v13p353 (single-modality),
#' Gallas2008_Neural-Networks_v21p387 (multiple modalities, arbitrary study designs),
#' Gallas2009_Commun-Stat-A-Theor_v38p2586 (framework paper).
#'
#' @details
#' In detail, this procedure reads the name of an input file from the local file system,
#' or takes a data frame and writes it to the local file system formatted
#' for the iMRMC program (found at https://github.com/DIDSR/iMRMC/releases), it executes a java app,
#' the iMRMC engine, which writes the results to the local files system, it reads the analysis results from
#' the local file system, packs the analysis results into a list object, deletes the data and analysis results
#' from the local file system, and returns the list object.
#' 
#' This software requires Java(>=8).
#' 
#' The examples took too long for CRAN to accept. So here is an example: 
#' \preformatted{
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)
#' }
#'
#' @param data This data.frame contains the following variables:
#' \itemize{
#'   \item \code{readerID} Factor with levels like "reader1", "reader2", ...
#'   \item \code{caseID} Factor with levels like "case1", "case2", ...
#'   \item \code{modalityID} Factor with levels like "modality1", "modality2", ...
#'   \item \code{score} num reader score
#' }
#'                      
#' Each row of this data frame corresponds to an observation.
#' For every caseID, there must be a row corresponding to the truth observation.
#' The readerID for a truth observation is "truth".
#' The modalityID for a truth observation is "truth".
#' The score for a truth observation must be either 0 (signal-absent) or 1 (signal-present).
#'
#' @param fileName This character string identifies the location of an iMRMC input file.
#'   The input file is identical to data except there is a free text section to start,
#'   then a line with "BEGIN DATA:", then the data frame info.
#'
#' @param workDir This character string determines the directory where intermediate results
#'            are written. If this parameter is not set, the program writes the intermediate
#'            results to the directory specified by tempdir() and then deletes them.
#'
#' @param iMRMCjarFullPath This character string identifies the location of the iMRMC.jar file
#'            this jar file can be downloaded from https://github.com/DIDSR/iMRMC/releases
#'            this R program supports version iMRMC-v3p2.jar
#'
#' @param stripDatesForTests Since results include a date and time stamp, these need to be
#'            stripped out when doing the package tests. This parameter flags whether or not
#'            the dates should be stripped out.
#'
#' @return [list] 
#'            iMRMC outputs. The objects of this list are described in detail in the iMRMC documentation
#'            which can be found at <http://didsr.github.io/iMRMC/000_iMRMC/userManualHTML/index.htm>
#'
#'            Here is a quick summary:
#' \itemize{
#'   \item {\code{perReader} 
#'   data.frame containing the performance results for each reader.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.}
#'   \item {\code{Ustat}
#'   data.frame containing the reader-average performance results.
#'              The analysis results are based on U-statistics and the papers listed above.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.}
#'   \item {\code{MLEstat}
#'   data.frame containing the reader-average performance results.
#'              The analysis results are based on V-statistics, which approximates the true distribution with
#'              the empirical distribution. The empirical distribution equals the nonparametric MLE
#'              estimate of the true distribution, which is also equivalent to the ideal bootstrap estimate.
#'              Please refer to the papers listed above.
#'              Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB and the corresponding
#'              variances, confidence intervals, degrees of freedom and p-values.
#'   }
#'   \item {\code{ROC}
#'   list containing ROC curves
#'              There is an ROC curve for every combination of reader and modality.
#'              For every modality, there are also four average ROC curves. These are discussed in
#'                Chen2014_Br-J-Radiol_v87p20140016.
#'                The diagonal average averages the reader-specific ROC curves along y = -x + b for b in (0,1).
#'                The horizontal average averages the reader specific ROC curves along y = b for b in (0,1).
#'                The vertical average averages the reader specific ROC curves along x = b for b in (0,1).
#'                The pooled average ignores readerID and pools all the scores together to create one ROC curve.}
#'   \item {\code{varDecomp}
#'   list containing different decompositions of the total variance.
#'              Please refer to Gallas2009_Commun-Stat-A-Theor_v38p2586 (framework paper).
#'              The different decompositions are BCK, BDG, DBM, MS, OR.
#'   }
#' }
#'
#'
#' @export
#'
# @examples
# # Create a sample configuration file
# config <- sim.gRoeMetz.config()
# # Simulate an MRMC ROC data set
# dFrame.imrmc <- sim.gRoeMetz(config)
# # Analyze the MRMC ROC data
# result <- doIMRMC(dFrame.imrmc)
#
#
doIMRMC <- function(
  data = NULL,
  fileName = NULL,
  workDir = NULL,
  iMRMCjarFullPath = NULL,
  stripDatesForTests = FALSE){

  # If workDir is not specified, it will be the R temp directory.
  # If workDir is specified, create it.
  if (is.null(workDir))
    workDir <- normalizePath(tempdir(), winslash = "/")
  else
    dir.create(workDir, showWarnings = FALSE)
  
  # The inputFile will hold the data for the java program
  inputFile <- file.path(workDir, "input.imrmc")
  
  # Check that data is provided
  if (is.null(data) & is.null(fileName)) {
    stop("You have not provided an input data frame or an input file.")
  }
  # Check that data is not provided twice.
  if (!is.null(data) & !is.null(fileName)) {
    stop("You cannot provide an input data frame AND an input file.")
  }
  
  # If fileName is specified make sure it exists.
  # Copy the fileName to the inputFile in the workDir.
  if (!is.null(fileName)) {
    if (!file.exists(fileName)) {
      print(paste("fileName = ", fileName))
      stop("The full-path fileName provided does not exist.")
    } 
    if (fileName == inputFile) {
      print(paste("fileName = ", fileName))
      stop("fileName equals the name of the temp inputFile. This is not allowed.")
    } 
    file.copy(fileName, inputFile)
  }

  # Check the input data and write it to the inputFile in the workDir  
  if (!is.null(data)) {

    # Check that the input data has key columns: readerID, caseID, modalityID, score
    if (length(setdiff(c("readerID", "caseID", "modalityID", "score"), names(data)))) {
      stop("The data frame does not include the key columns: readerID, caseID, modalityID, score.")
    }
    
    # Write data frame to the inputFile
    writeLines("BEGIN DATA:", con = inputFile)
    utils::write.table(data[ , c("readerID", "caseID", "modalityID", "score")], inputFile,
                quote = FALSE, row.names = FALSE, col.names = FALSE,
                append = TRUE, sep = ", ")

  }
  
  if (is.null(iMRMCjarFullPath)) {
    iMRMCjar <- "iMRMC-v4.0.3.jar"
    pkgPath <- path.package("iMRMC", quiet = FALSE)
    iMRMCjarFullPath <- file.path(pkgPath, "java", iMRMCjar)

    # This check is necessary for testthat tests that run in some
    # virtual environment that is not like reality
    if (!file.exists(iMRMCjarFullPath)) {
      iMRMCjarFullPath = file.path(pkgPath, "inst","java", iMRMCjar)
    }
  }
  
  # Run iMRMC
  desc <- tryCatch(

    system2(
      "java",
      args = c(
        "-jar",
        paste0('"', iMRMCjarFullPath, '"'),
        paste0('"', inputFile, '"'),
        paste0('"', file.path(workDir, "imrmcDir"), '"')
      ),
      stdout = TRUE, stderr = TRUE
    ),

    warning = function(w) {
      cat("do_IMRMC WARNING\n")
      warning(w)
    },
    
    error = function(e) {
      cat("\ndoIMRMC ERROR\n")
      cat("One possible reason is that you don't have java.\n")
      cat("This software requires Java(>=8).\n")
      stop(e)
    }
    
  )

  if (!file.exists(file.path(workDir, "imrmcDir", "AUCperReader.csv"))) {
    cat(desc, sep = "\n")
    stop()
  }

  # Retrieve the iMRMC results
  perReader <- utils::read.csv(file.path(workDir, "imrmcDir", "AUCperReader.csv"),
                               stringsAsFactors = TRUE) 
  Ustat <- utils::read.csv(file.path(workDir, "imrmcDir", "statAnalysis.csv"),
                           stringsAsFactors = TRUE)
  MLEstat <- utils::read.csv(file.path(workDir, "imrmcDir", "statAnalysisMLE.csv"),
                             stringsAsFactors = TRUE)
  ROCraw <- utils::read.csv(file.path(workDir, "imrmcDir", "ROCcurves.csv"),
                     header = FALSE, row.names = NULL, skip = 1)

  BCK <- readVarDecomp(file.path(workDir, "imrmcDir", "BCKtable.csv"))
  BDG <- readVarDecomp(file.path(workDir, "imrmcDir", "BDGtable.csv"))
  DBM <- readVarDecomp(file.path(workDir, "imrmcDir", "DBMtable.csv"))
  MS <- readVarDecomp(file.path(workDir, "imrmcDir", "MStable.csv"))
  OR <- readVarDecomp(file.path(workDir, "imrmcDir", "ORtable.csv"))

  ROC <- by(ROCraw, ROCraw[, 1], function(x) {
    list(desc = as.character(x[1,1]), n = as.numeric(x[1,2]),
         fpf = as.numeric(x[x[,3] == "FPF", 4:(3 + x[1,2])]),
         tpf = as.numeric(x[x[,3] == "TPF", 4:(3 + x[1,2])])
      )

  })

  # If doing tests, strip out the data that could change (dates etc)
  if (stripDatesForTests) {

    descDrop <- c("inputFile", "date", "iMRMCversion")

    perReader <- perReader[ , !(names(perReader)) %in% descDrop]
    Ustat <- Ustat[ , !(names(Ustat)) %in% descDrop]
    MLEstat <- MLEstat[ , !(names(MLEstat)) %in% descDrop]
    ROC <- 0

  }

  # Delete the content written to disk
  if (workDir == normalizePath(tempdir(), winslash = "/")) {
    unlink(file.path(workDir, "imrmcDir"), recursive = TRUE)
    unlink(inputFile)
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

getComponents <- function(df.varDecomp) {

  nc <- ncol(df.varDecomp)
  result <- df.varDecomp[c(1, 3, 5), 5:nc]
  descA <- as.character(df.varDecomp$modalityA[1])
  descB <- as.character(df.varDecomp$modalityB[1])
  rownames(result) <- c(descA, descB, paste(descA, descB, sep = "."))

  return(result)

}

getCoefficients <- function(df.varDecomp) {

  nc <- ncol(df.varDecomp)
  result <- df.varDecomp[c(2, 4, 6), 5:nc]
  descA <- as.character(df.varDecomp$modalityA[1])
  descB <- as.character(df.varDecomp$modalityB[1])
  rownames(result) <- c(descA, descB, paste(descA, descB, sep = "."))

  return(result)

}

readVarDecomp <- function(fileName) {

  df.csv <- utils::read.csv(fileName, row.names = NULL)

  df.csv <- split(df.csv, df.csv$UstatOrMLE)
  df.csv$Ustat <- split(df.csv$Ustat, list(df.csv$Ustat$modalityA, df.csv$Ustat$modalityB))
  df.csv$Ustat <- df.csv$Ustat[sapply(df.csv$Ustat, nrow) > 0]
  df.csv$MLE <- split(df.csv$MLE, list(df.csv$MLE$modalityA, df.csv$MLE$modalityB))
  df.csv$MLE <- df.csv$MLE[sapply(df.csv$MLE, nrow) > 0]

  MLE.comp <- lapply(df.csv$MLE, getComponents)
  Ustat.comp <- lapply(df.csv$Ustat, getComponents)

  MLE.coeff <- lapply(df.csv$MLE, getCoefficients)
  Ustat.coeff <- lapply(df.csv$Ustat, getCoefficients)

  result <- list(
    MLE = list(comp = MLE.comp, coeff = MLE.coeff),
    Ustat = list(comp = Ustat.comp, coeff = Ustat.coeff)
  )

  return(result)

}

