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
#' for the iMRMC program (found at https://github.com/DIDSR/iMRMC/releases), it executes the iMRMC
#' program which writes the results to the local files system, it reads the analysis results from
#' the local file system, packs the analysis results into a list object, deletes the data and analysis results
#' from the local file system, and returns the list object.
#'
#' @param data This data.frame contains the following variables:
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
#' @param stdout where output to 'stdout' or 'stderr' should be sent.
#'            Possible values are "", to the R console (the default),
#'            NULL or FALSE (discard output),
#'            TRUE (capture the output in character vector)
#'            or a character string naming a file.
#'
#' @param stderr where output to 'stdout' or 'stderr' should be sent.
#'            Possible values are "", to the R console (the default),
#'            NULL or FALSE (discard output),
#'            TRUE (capture the output in a character vector)
#'            or a character string naming a file.
#'
#' @param stripDatesForTests Since results include a date and time stamp, these need to be
#'            stripped out when doing the package tests. This parameter flags whether or not
#'            the dates should be stripped out.
#'
#' @return iMRMCoutput [list] the objects of this list are described in detail in the iMRMC documentation
#'            which can be found at <http://didsr.github.io/iMRMC/000_iMRMC/userManualHTML/index.htm>
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
#'              Please refer to the papers listed above.
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
#' @examples
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)
#'
#'
doIMRMC <- function(
  data = NULL,
  fileName = NULL,
  workDir = NULL,
  iMRMCjarFullPath = NULL,
  stdout = NULL,
  stderr = NULL,
  stripDatesForTests = FALSE){

  if (is.null(workDir)) {
    workDir <- tempdir()
  }

  if (is.null(data)) {
    if (is.null(fileName)) {
      stop("ERROR: You have not provided an input data frame or an input file.")
    } else {
      flagWriteFile <- FALSE
    }
  } else {
    if (!is.null(fileName)) {
      stop("ERROR: You cannot provide and input data frame AND an input file.")
    }

    flagWriteFile <- TRUE
    fileName <- file.path(workDir, "input.imrmc")

    # Write data frame to iMRMC input file
    writeLines("BEGIN DATA:", con = fileName)
    utils::write.table(data, fileName,
                quote = FALSE, row.names = FALSE, col.names = FALSE,
                append = TRUE, sep = ", ")

  }

  if (is.null(iMRMCjarFullPath)) {
    iMRMCjar <- "iMRMC-v4.0.0.jar"
    pkgPath = path.package("iMRMC", quiet = FALSE)
    iMRMCjarFullPath <- file.path(pkgPath, "java", iMRMCjar)

    # This check is necessary for testthat tests that run in some
    # virtual environment that is not like reality
    if (!file.exists(iMRMCjarFullPath)) {
      iMRMCjarFullPath = paste(pkgPath, "/inst/java/", iMRMCjar, sep = "")
    }
  }

  # Run iMRMC
  system2("java", args = c("-jar",
                           iMRMCjarFullPath,
                           fileName,
                           file.path(workDir, "imrmcDir")),
                           stdout = stdout, stderr = stderr)

  # Retrieve the iMRMC results
  perReader <- utils::read.csv(file.path(workDir, "imrmcDir", "AUCperReader.csv"))
  Ustat <- utils::read.csv(file.path(workDir, "imrmcDir", "statAnalysis.csv"))
  MLEstat <- utils::read.csv(file.path(workDir, "imrmcDir", "statAnalysisMLE.csv"))
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
  if (workDir == tempdir()) {
    unlink(file.path(workDir, "imrmcDir"), recursive = TRUE)

    if (flagWriteFile) {
      unlink(fileName)
    }

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

