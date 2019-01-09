#' doViperSummary
#'
#' @description This function estimates the VIPER performance results.
#' Before archiving this on GitHub, the function was called analyzeData.
#'
#' @param viperObs0 A VIPER raw-data file: \code{\link{viperObs365}}, \code{\link{viperObs455}}
#'
#' @return A VIPER summary file: \code{\link{viperSummary365}} or \code{\link{viperSummary455}}
#' @export
#'
#' @examples
#' library("iMRMC")
#' viperSummary <- viperData::doViperSummary(viperData::viperObs455)
doViperSummary <- function(viperObs0) {

  # Split the data by "desc": screeningLowP, screeningMedP, screeningHighP, challengeMedP, challengeHighP
  viperObs <- split(viperObs0, viperObs0$desc)
  desc <- names(viperObs)

  viperSummary <- list()
  for (i in 1:length(viperObs)) {

    # Not all readers read all cases
    viperObs.i <- droplevels(viperObs[[i]])

    # Let user know progress
    print("")
    print(desc[i])

    # Extract basic study design info
    studyDesign <- doViperStudyDesign(viperObs.i)

    # Analyze AUC
    print("AUC")
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "score",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperObs.i, keyColumns, "cancer")
    iMRMC.AUC <- iMRMC::doIMRMC(dfIMRMC)

    # Analyze TPF
    print("TPF")
    viperObs.i$TP = 0.5
    viperObs.i$TP[viperObs.i$Ctype == "cancer" & viperObs.i$recall == 1] = 1.0
    viperObs.i$TP[viperObs.i$Ctype == "cancer" & viperObs.i$recall == 2] = 0.0
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "TP",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperObs.i, keyColumns, "cancer")
    iMRMC.TPF <- iMRMC::doIMRMC(dfIMRMC)

    # Analyze TNF
    print("TNF")
    viperObs.i$TN = 0.5
    viperObs.i$TN[viperObs.i$Ctype != "cancer" & viperObs.i$recall == 1] = 1.0
    viperObs.i$TN[viperObs.i$Ctype != "cancer" & viperObs.i$recall == 2] = 0.0
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "TN",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperObs.i, keyColumns, "cancer")
    iMRMC.TNF <- iMRMC::doIMRMC(dfIMRMC)

    # Consolidate MRMC analysis output into a list
    iMRMCoutput <- list(auc = iMRMC.AUC, tpf = iMRMC.TPF, tnf = iMRMC.TNF)

    viperSummary[[i]] <- list(
      desc = desc[i],
      studyDesign = studyDesign,
      iMRMC = iMRMCoutput
    )

  }
  names(viperSummary) <- names(viperObs)

  return(viperSummary)

}

