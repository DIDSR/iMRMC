#' doViperSummary
#' 
#' @description This function estimates the VIPER performance results.
#' Before archiving this on GitHub, the function was called analyzeData.
#'
#' @param viperData0 A VIPER raw-data file: \code{\link{viperObs365}}, \code{\link{viperObs455}}
#'
#' @return A VIPER summary file: \code{\link{viperSummary365}} or \code{\link{viperSummary455}}
#' @export
#' 
#' @examples
#' viperSummary <- viperData::doViperSummary(viperData::viperObs455)
doViperSummary <- function(viperData0) {
  
  # Split the data by "desc": screeningLowP, screeningMedP, screeningHighP, challengeMedP, challengeHighP
  viperData <- split(viperData0, viperData0$desc)
  desc <- names(viperData)
  
  viperSummary <- list()
  for (i in 1:length(viperData)) {
    
    # Not all readers read all cases
    viperData.i <- droplevels(viperData[[i]])

    # Let user know progress    
    print("")
    print(desc[i])

    # Extract basic study design info
    studyDesign <- doStudyDesign(viperData.i)

    # Analyze AUC
    print("AUC")
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "score",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperData.i, keyColumns, "cancer")
    iMRMC.AUC <- iMRMC::doIMRMC(dfIMRMC)

    # Analyze TPF
    print("TPF")
    viperData.i$TP = 0.5
    viperData.i$TP[viperData.i$Ctype == "cancer" & viperData.i$recall == 1] = 1.0
    viperData.i$TP[viperData.i$Ctype == "cancer" & viperData.i$recall == 2] = 0.0
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "TP",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperData.i, keyColumns, "cancer")
    iMRMC.TPF <- iMRMC::doIMRMC(dfIMRMC)

    # Analyze TNF
    print("TNF")
    viperData.i$TN = 0.5
    viperData.i$TN[viperData.i$Ctype != "cancer" & viperData.i$recall == 1] = 1.0
    viperData.i$TN[viperData.i$Ctype != "cancer" & viperData.i$recall == 2] = 0.0
    keyColumns <- list(
      readerID = "readerID",
      caseID = "caseID",
      modalityID = "modalityID",
      score = "TN",
      truth = "Ctype"
    )
    dfIMRMC <- iMRMC::createIMRMCdf(viperData.i, keyColumns, "cancer")
    iMRMC.TNF <- iMRMC::doIMRMC(dfIMRMC)
    
    # Consolidate MRMC analysis output into a list
    iMRMCoutput <- list(auc = iMRMC.AUC, tpf = iMRMC.TPF, tnf = iMRMC.TNF)

    viperSummary[[i]] <- list(
      desc = desc[i],
      studyDesign = studyDesign,
      iMRMC = iMRMCoutput
    )
    
  }
  names(viperSummary) <- names(viperData)
  
  return(viperSummary)
  
}

