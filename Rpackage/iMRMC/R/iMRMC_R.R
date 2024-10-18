#' MRMC analysis for arbitrary design dataset
#'
#'
#'
#' @description Execute a Multi-Reader, Multi-Case (MRMC) analysis
#' of ROC data from imaging studies where clinicians (readers) evaluate patient
#' images (cases). This function is a wrapper that executes 
#' \code{\link{doAUCmrmc}} and formats the output to generally match the output of 
#' \code{doIMRMC} version 1.2.5.
#' An overview of this software, including references presenting
#' details on the methods, can be found \href{https://zenodo.org/record/8383591}{HERE}
#' or as an entry in the FDA/CDRH Regulatory Science Tool Catalog
#' \href{https://cdrh-rst.fda.gov/imrmc-software-do-multi-reader-multi-case-statistical-analysis-reader-studies}{HERE}.
#'
#'
#'
#' @param data an iMRMC formatted data frame, see \link{dfMRMC_example}
#' 
#'
#' @return The MRMC analysis results, below is a quick summary:
#' \itemize{
#'   \item {\code{perReader, data.frame} 
#'     The performance results for each combination of reader and pair of modalities.
#'     Key variables of this data frame are AUCA, AUCB, AUCAminusAUCB
#'     and the corresponding variances.
#'     When the modalities differ, the variance is understood to be the covariance between the modalities.
#'   }
#'   \item {\code{Ustat, data.frame}
#'     Reader-averaged performance results for each pair of modalities.
#'     The analysis results are based on U-statistics.
#'     Key variables of this data frame are AUCA, AUCB, 
#'     AUCAminusAUCB and the corresponding variances, 
#'     confidence intervals, degrees of freedom, and p-values.
#'     When the modalities differ, the variance is understood to be the covariance between the modalities.
#'   }
#'   \item {\code{MLEstat, data.frame}
#'     Reader-average performance results for each pair of modalities.
#'     The analysis results are based on V-statistics, which 
#'     approximates the true distribution with
#'     the empirical distribution. The empirical distribution equals 
#'     the nonparametric MLE
#'     estimate of the true distribution, which is also equivalent 
#'     to the ideal bootstrap estimate.
#'     Key variables of this data frame are AUCA, AUCB, 
#'     AUCAminusAUCB and the corresponding
#'     variances, confidence intervals, degrees of freedom, and p-values.
#'     When the modalities differ, the variance is understood to be the covariance between the modalities.
#'   }
#'   \item {\code{varDecomp, list}
#'     list of data frames of the coefficient and components of 
#'     variance. The analysis includes variance decomposition based off both the 
#'     BDG and BCK MRMC methods, and Ustat and MLE statistical methods. 
#'     Each MRMC and statistical method combination is contained within this 
#'     list of lists. 
#'   }
#'   \item {\code{ROC, list}
#'     each object of this list is an object containing an ROC curve.
#'     There is an ROC curve for every combination of reader and modality.
#'     For every modality, there are also four average ROC curves. 
#'     These are discussed in Chen2014_Br-J-Radiol_v87p20140016.
#'     The diagonal average averages the reader-specific ROC curves 
#'     along y = -x + b for b in (0,1).
#'     The horizontal average averages the reader specific ROC curves 
#'     along y = b for b in (0,1).
#'     The vertical average averages the reader specific ROC curves 
#'     along x = b for b in (0,1).
#'     The pooled average ignores readerID and pools all the scores 
#'     together to create one ROC curve.}
#'  \item {\code{full, list}
#'    This returns the same result as \code{\link{doAUCmrmc}}.
#'  }
#' }
#' 
#' @details
#' Unlike the legacy \code{\link{doIMRMC_java}}, the `varDecomp` results no 
#' longer scale the covariance by a factor of 2. This scaling is needed when 
#' calculating the total variance of the difference in modalities. The user 
#' must scale this covariance by 2 manually now to achieve the total variance
#' of the difference in modalities result. 
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
doIMRMC = function(data){

  result_AUC_MRMC <- doAUCmrmc(data, flagROC = TRUE)

  # Make a perReader object almost look like version 1.2.5 and earlier
  perReader <- origPerReader(result_AUC_MRMC)
  
  # Make a Ustat object almost look like version 1.2.5 and earlier
  Ustat <- origUstat(result_AUC_MRMC)
  
  # Make an MLEstat object almost look like version 1.2.5 and earlier
  MLEstat <- origMLEstat(result_AUC_MRMC)
  
  # Make a varDecomp object a simpler version of like version 1.2.5 and earlier
  varComp <- varDecomp(result_AUC_MRMC)
  
  # Get ROC curves
  ROC <- result_AUC_MRMC$ROC
  
  # Return ####################################################################
  return(list(
    perReader = perReader,
    Ustat = Ustat,
    MLEstat = MLEstat,
    varDecomp = varComp,
    ROC = ROC,
    full = result_AUC_MRMC
  ))

}

origPerReader <- function(result_AUC_MRMC) {
  
  current <- result_AUC_MRMC$perReader.full
  nM <- result_AUC_MRMC$summaryMRMC$nM
  
  ####
  #### I would like to change the perReader output
  #### in the revised version of doIMRMC.
  #### 
  #### To do the testing,
  #### we need to shape the data frame to reproduce results iMRMC package
  #### We'll do this from the first column to the last
  #### 
  # First, select rows where readerID.1 == readerID.2
  index.TF <- current[, "readerID.1"] == current[, "readerID.2"]
  current <- current[index.TF, ]
  
  # Rename the row names
  rownames(current) <- NULL
  
  # Rename the readerID.1 column to readerID
  current <- renameCol(current, "readerID.1", "readerID")

  # Delete the readerID.2 column 
  current <- deleteCol(current, "readerID.2")

  
  # R doesn't use generally use "integer" types, default is "numeric" type
  # The original code was java and returned "integer" type for N0 and N1
  # Convert the type to achieve agreement
  current$N0 <- as.integer(current$N0)
  current$N1 <- as.integer(current$N1)
  
  # Rename the modalityID columns
  current <- renameCol(current, "modalityID.1", "modalityA")
  current <- renameCol(current, "modalityID.2", "modalityB")
  
  # Rename the AUC.1 and AUC.2 columns to AUCA and AUCB
  current <- renameCol(current, "AUC.1", "AUCA")
  current <- renameCol(current, "AUC.2", "AUCB")
  
  # Create new columns for varAUCA and varAUCB and fill them appropriately
  current$varAUCA <- as.logical(NA)
  current$varAUCB <- as.logical(NA)
  
  # When the modalities are the same, varAUCA = varAUCB = covAUC
  index.TF <- current$modalityA == current$modalityB
  current$varAUCA[index.TF] <- current$covAUC[index.TF]
  current$varAUCB[index.TF] <- current$covAUC[index.TF]
  
  # When the modalities are different, fill them appropriately
  index.TF <- current$modalityA != current$modalityB
  current.12 <- current[index.TF, ]
  if (nM > 1) {
    for (i in 1:sum(index.TF)) {
      current.i <- current[
        (current$readerID == current.12$readerID[i]) &
          (current$modalityA == current.12$modalityA[i]) &
          (current$modalityB == current.12$modalityA[i]), 
      ]
      current.12$varAUCA[i] <- current.i$varAUCA
      current.i <- current[
        (current$readerID == current.12$readerID[i]) &
          (current$modalityA == current.12$modalityB[i]) &
          (current$modalityB == current.12$modalityB[i]), 
      ]
      current.12$varAUCB[i] <- current.i$varAUCB
    }
    current[index.TF, c("varAUCA", "varAUCB")] <- current.12[, c("varAUCA", "varAUCB")]
  }
  
  # Add columns for AUCAminusAUCB and varAUCAminusAUCB
  current$AUCAminusAUCB <- current$AUCA - current$AUCB
  current$varAUCAminusAUCB <- current$varAUCA + current$varAUCB - 2 * current$covAUC
  
  # Keep and reorder only what we need
  current <- current[, c(
    "readerID",
    "N0", "N1",
    "modalityA", "modalityB",
    "AUCA", "varAUCA",
    "AUCB", "varAUCB",
    "AUCAminusAUCB", "varAUCAminusAUCB"
  )]
  
  # Special edits to perfectly match original output
  index.TF <- current$modalityA == current$modalityB
  current$modalityB <- as.character(current$modalityB)
  current$modalityB[index.TF] <- "NO_MOD"
  current$modalityB <- factor(current$modalityB)
  current[index.TF, c("AUCB", "varAUCB", "AUCAminusAUCB", "varAUCAminusAUCB")] <- NA
  
  return(current)
  
}

origUstat <- function(result_AUC_MRMC) {
  
  current <- result_AUC_MRMC$Ustat.full
  nM <- result_AUC_MRMC$summaryMRMC$nM

  # Rename the modalityID columns
  current <- renameCol(current, "modalityID.1", "modalityA")
  current <- renameCol(current, "modalityID.2", "modalityB")
  
  # Create a Ustat column  
  current$UstatOrMLE <- factor("Ustat")
  
  # Rename the AUC.1 and AUC.2 columns to AUCA and AUCB
  current <- renameCol(current, "AUC.1", "AUCA")
  current <- renameCol(current, "AUC.2", "AUCB")
  
  # Create new columns for varAUCA and varAUCB and fill them appropriately
  current$varAUCA <- NA
  current$varAUCB <- NA
  
  # When the modalities are the same, varAUCA = varAUCB = covAUC
  index.TF <- current$modalityA == current$modalityB
  current$varAUCA[index.TF] <- current$covAUC[index.TF]
  current$varAUCB[index.TF] <- current$covAUC[index.TF]
  
  ####
  #### When the modalities are different, fill them appropriately
  #### 
  
  # Select the rows of corresponding to different modalities
  index.TF <- current$modalityA != current$modalityB
  current.12 <- current[index.TF, ]
  
  # For each row that has different modalities,
  # Fill in varAUCA, varAUCB, and dfBDG
  if (nM > 1) {
    
    for (i in 1:sum(index.TF)) {
      current.i <- current[
        (current$modalityA == current.12$modalityA[i]) &
          (current$modalityB == current.12$modalityA[i]), ]
      current.12$varAUCA[i] <- current.i$varAUCA
      
      current.i <- current[
        (current$modalityA == current.12$modalityB[i]) &
          (current$modalityB == current.12$modalityB[i]), ]
      current.12$varAUCB[i] <- current.i$varAUCB
      
      current.12$dfBDG <- current.12$dfBDG.diff
    }
    current[index.TF, c("varAUCA", "varAUCB", "dfBDG")] <- 
      current.12[, c("varAUCA", "varAUCB", "dfBDG")]
    
  }
  
  # Add columns for AUCAminusAUCB and varAUCAminusAUCB
  current <- renameCol(current, "AUC1minusAUC2", "AUCAminusAUCB")
  current <- renameCol(current, "varAUC1minusAUC2", "varAUCAminusAUCB")
  
  # Keep and reorder only what we need
  current <- current[, c(
    "NR", "N0", "N1",
    "modalityA", "modalityB",
    "UstatOrMLE",
    "AUCA", "varAUCA",
    "AUCB", "varAUCB",
    "AUCAminusAUCB", "varAUCAminusAUCB",
    "pValueNormal",
    "botCInormal", "topCInormal",
    "rejectNormal",
    "dfBDG",
    "pValueBDG",
    "botCIBDG", "topCIBDG",
    "rejectBDG"
  )]

  # Special edits to perfectly match original output
  index.TF <- current$modalityA == current$modalityB
  current$modalityB <- as.character(current$modalityB)
  current$modalityB[index.TF] <- "NO_MOD"
  current$modalityB <- factor(current$modalityB)
  current[index.TF, c("AUCB", "varAUCB", "AUCAminusAUCB", "varAUCAminusAUCB")] <- NA
  current <- renameCol(current, "dfBDG.diff", "dfBDG")
  current <- renameCol(current, "pValueBDG.diff", "pValueBDG")
  current <- renameCol(current, "botCIBDG.diff", "botCIBDG")
  current <- renameCol(current, "topCIBDG.diff", "topCIBDG")
  current <- renameCol(current, "rejectBDG.diff", "rejectBDG")
  
  return(current)
  
}

origMLEstat <- function(result_AUC_MRMC) {
  
  current <- result_AUC_MRMC$Ustat.full
  nM <- result_AUC_MRMC$summaryMRMC$nM
  
  # Rename the modalityID columns
  current <- renameCol(current, "modalityID.1", "modalityA")
  current <- renameCol(current, "modalityID.2", "modalityB")
  
  # Create a Ustat column  
  current$UstatOrMLE <- factor("MLE")
  
  # Rename the AUC.1 and AUC.2 columns to AUCA and AUCB
  current <- renameCol(current, "AUC.1", "AUCA")
  current <- renameCol(current, "AUC.2", "AUCB")
  
  # Create new columns for varAUCA and varAUCB and fill them appropriately
  current$varAUCA <- NA
  current$varAUCB <- NA
  
  # When the modalities are the same, varAUCA = varAUCB = covAUC
  index.TF <- current$modalityA == current$modalityB
  current$varAUCA[index.TF] <- current$covAUC.biased[index.TF]
  current$varAUCB[index.TF] <- current$covAUC.biased[index.TF]

  # Set dfBDG to be the biased versions
  current$dfBDG <- current$dfBDG.biased
  
  ####
  #### When the modalities are different, fill them appropriately
  #### 
  
  # Select the rows of corresponding to different modalities
  index.TF <- current$modalityA != current$modalityB
  current.12 <- current[index.TF, ]
  
  # For each row that has different modalities,
  # Fill in varAUCA, varAUCB 
  if (nM > 1) {
    
    for (i in 1:sum(index.TF)) {
      current.i <- current[
        (current$modalityA == current.12$modalityA[i]) &
          (current$modalityB == current.12$modalityA[i]), ]
      current.12$varAUCA[i] <- current.i$covAUC.biased
      
      current.i <- current[
        (current$modalityA == current.12$modalityB[i]) &
          (current$modalityB == current.12$modalityB[i]), ]
      current.12$varAUCB[i] <- current.i$covAUC.biased
    }
    current[index.TF, c("varAUCA", "varAUCB", "dfBDG")] <- 
      current.12[, c("varAUCA", "varAUCB", "dfBDG.biased.diff")]
    
  }
  
  # Add columns for AUCAminusAUCB and varAUCAminusAUCB
  current <- renameCol(current, "AUC1minusAUC2", "AUCAminusAUCB")
  current <- renameCol(current, "varAUC1minusAUC2.biased", "varAUCAminusAUCB")
  
  # Replace unbiased hypothesis test results with biased results
  current$pValueNormal <- current$pValueNormal.biased
  current$botCInormal <- current$botCInormal.biased
  current$topCInormal <- current$topCInormal.biased
  current$rejectNormal <- current$rejectNormal.biased
  current$pValueBDG <- current$pValueBDG.biased
  current$botCIBDG <- current$botCIBDG.biased
  current$topCIBDG <- current$topCIBDG.biased
  current$rejectBDG <- current$rejectBDG.biased
  
  # Keep and reorder only what we need
  current <- current[, c(
    "NR", "N0", "N1",
    "modalityA", "modalityB",
    "UstatOrMLE",
    "AUCA", "varAUCA",
    "AUCB", "varAUCB",
    "AUCAminusAUCB", "varAUCAminusAUCB",
    "pValueNormal",
    "botCInormal", "topCInormal",
    "rejectNormal",
    "dfBDG",
    "pValueBDG",
    "botCIBDG", "topCIBDG",
    "rejectBDG"
  )]
  
  # Special edits to perfectly match original output
  index.TF <- current$modalityA == current$modalityB
  current$modalityB <- as.character(current$modalityB)
  current$modalityB[index.TF] <- "NO_MOD"
  current$modalityB <- factor(current$modalityB)
  current[index.TF, c("AUCB", "varAUCB", "AUCAminusAUCB", "varAUCAminusAUCB")] <- NA
  
  return(current)
  
}

varDecomp <- function(result_AUC_MRMC) {
  
  Ustat.full <- result_AUC_MRMC$Ustat.full
  summaryMRMC <- result_AUC_MRMC$summaryMRMC
  nM <- result_AUC_MRMC$summaryMRMC$nM
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varComp.BDG.Ustat <- varDecomp.BDG.Ustat(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varComp.BDG.MLE <- varDecomp.BDG.MLE(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varComp.BCK.Ustat <- varDecomp.BCK.Ustat(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varComp.BCK.MLE <- varDecomp.BCK.MLE(Ustat.full, summaryMRMC)
  
  varComp.BCK <- list(
    MLE = varComp.BCK.MLE,
    Ustat = varComp.BCK.Ustat)
  
  varComp.BDG <- list(
    MLE = varComp.BDG.MLE,
    Ustat = varComp.BDG.Ustat)
  
  varComp <- list(BCK = varComp.BCK, BDG = varComp.BDG)
  
  return(varComp)
  
}

varDecomp.BDG.Ustat <- function(Ustat.full, summaryMRMC) {

  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  # The BDG components are extracted from these components of Ustat.full
  desc.comp.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")

  comp <- Ustat.full[, desc.comp.BDG]
  comp$modalityID.1 <- factor(comp$modalityID.1)
  comp$modalityID.2 <- factor(comp$modalityID.2)
  
  
  
  # The BDG coefficients are extracted from these components of Ustat.full
  desc.coeff.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1.coeff", "M2.coeff", "M3.coeff", "M4.coeff",
    "M5.coeff", "M6.coeff", "M7.coeff", "M8.coeff")

  coeff <- Ustat.full[, desc.coeff.BDG]
  names(coeff) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  coeff$modalityID.1 <- factor(coeff$modalityID.1)
  coeff$modalityID.2 <- factor(coeff$modalityID.2)
  
  
  
  # Pack the components and coefficients for return
  result <- list(comp = comp, coeff = coeff)
  
  return(result)

}

varDecomp.BDG.MLE <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  # This is the base of the component data frame
  comp.1 <- data.frame(
    modalityID.1 = Ustat.full$modalityID.1,
    modalityID.2 = Ustat.full$modalityID.2
  )
  

  
  # The MLE components are functions of these components of Ustat.full
  desc.numer.BDG <- c(
    "numer1", "numer2", "numer3", "numer4",
    "numer5", "numer6", "numer7", "numer8")
  
  desc.denom.BDG <- c(
    "denom1", "denom2", "denom3", "denom4",
    "denom5", "denom6", "denom7", "denom8")
  
  numer <- Ustat.full[, desc.numer.BDG]
  denom <- Ustat.full[, desc.denom.BDG]
  
  comp.2 <- numer / denom

  
    
  # Aggregate the MLE components with the base
  comp <- cbind(comp.1, comp.2)
  
  names(comp) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  
  
  # The BDG coefficients are extracted from these components of Ustat.full
  desc.coeff.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1.coeff", "M2.coeff", "M3.coeff", "M4.coeff",
    "M5.coeff", "M6.coeff", "M7.coeff", "M8.coeff")
  
  coeff <- Ustat.full[, desc.coeff.BDG]
  names(coeff) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  coeff$modalityID.1 <- factor(coeff$modalityID.1)
  coeff$modalityID.2 <- factor(coeff$modalityID.2)

  
  
  # Pack the components and coefficients for return
  result <- list(comp = comp, coeff = coeff)
  
  return(result)

}

varDecomp.BCK.Ustat <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  # The BCK components are extracted from these components of Ustat.full
  desc.comp.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N", "BCK.D", "BCK.ND", "BCK.R", "BCK.NR", "BCK.DR", "BCK.RND")

  comp_BCK <- Ustat.full[, desc.comp.BCK]
  names(comp_BCK) <- c("modalityID.1", "modalityID.2",
                        "N", "D", "ND",
                        "R", "NR", "DR", "RND")
  

    
  # The BCK coefficients are extracted from these components of Ustat.full
  desc.coeff.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.coeff", "BCK.D.coeff", "BCK.ND.coeff",
    "BCK.R.coeff", "BCK.NR.coeff", "BCK.DR.coeff", "BCK.RND.coeff")
  
  coeff_BCK <- Ustat.full[, desc.coeff.BCK]
  names(coeff_BCK) <- c("modalityID.1", "modalityID.2",
                        "N", "D", "ND",
                        "R", "NR", "DR", "RND")
  

  # Pack the components and coefficients for return
  result <- list(comp = comp_BCK, coeff = coeff_BCK)
  
  return(result)
  
  
}

varDecomp.BCK.MLE <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  # The BCK components are extracted from these components of Ustat.full
  desc.comp.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.b", "BCK.D.b", "BCK.ND.b", "BCK.R.b", "BCK.NR.b", "BCK.DR.b", "BCK.RND.b")
  
  comp_BCK <- Ustat.full[, desc.comp.BCK]
  names(comp_BCK) <- c("modalityID.1", "modalityID.2",
                       "N", "D", "ND",
                       "R", "NR", "DR", "RND")
  

  
  # The BCK coefficients are extracted from these components of Ustat.full
  desc.coeff.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.coeff", "BCK.D.coeff", "BCK.ND.coeff",
    "BCK.R.coeff", "BCK.NR.coeff", "BCK.DR.coeff", "BCK.RND.coeff")
  
  coeff_BCK <- Ustat.full[, desc.coeff.BCK]
  names(coeff_BCK) <- c("modalityID.1", "modalityID.2",
                        "N", "D", "ND",
                        "R", "NR", "DR", "RND")
  

  
  # Pack the components and coefficients for return
  result <- list(comp = comp_BCK, coeff = coeff_BCK)
  
  return(result)
  
  
}





# Legacy formatting ##########
origVarDecomp <- function(result_AUC_MRMC) {
  
  Ustat.full <- result_AUC_MRMC$Ustat.full
  summaryMRMC <- result_AUC_MRMC$summaryMRMC
  nM <- result_AUC_MRMC$summaryMRMC$nM
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varDecomp.BDG.Ustat <- origVarDecomp.BDG.Ustat(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varDecomp.BDG.MLE <- origVarDecomp.BDG.MLE(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varDecomp.BCK.Ustat <- origVarDecomp.BCK.Ustat(Ustat.full, summaryMRMC)
  
  # Make the varDecomp object look like version 1.2.5 and earlier
  varDecomp.BCK.MLE <- origVarDecomp.BCK.MLE(Ustat.full, summaryMRMC)
  
  varDecomp.BCK <- list(
    MLE = varDecomp.BCK.MLE,
    Ustat = varDecomp.BCK.Ustat)
  
  varDecomp.BDG <- list(
    MLE = varDecomp.BDG.MLE,
    Ustat = varDecomp.BDG.Ustat)
  
  varDecomp <- list(BCK = varDecomp.BCK, BDG = varDecomp.BDG)
  
  return(varDecomp)
  
}

origVarDecomp.BDG.Ustat <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  desc.comp.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  comp <- Ustat.full[, desc.comp.BDG]
  comp$modalityID.1 <- factor(comp$modalityID.1)
  comp$modalityID.2 <- factor(comp$modalityID.2)
  
  desc.coeff.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1.coeff", "M2.coeff", "M3.coeff", "M4.coeff",
    "M5.coeff", "M6.coeff", "M7.coeff", "M8.coeff")
  
  coeff <- Ustat.full[, desc.coeff.BDG]
  names(coeff) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  coeff$modalityID.1 <- factor(coeff$modalityID.1)
  coeff$modalityID.2 <- factor(coeff$modalityID.2)
  
  ####
  #### Single modalities loop
  #### 
  comp.full <- list()
  coeff.full <- list()
  for (modality.1 in 1:nM) {
    
    desc <- c(
      modalities[modality.1],
      "NO_MOD",
      paste(modalities[modality.1], "NO_MOD", sep = ".")
    )
    
    comp.temp <- comp[, 3:10] * 0
    comp.temp[1, ] <- comp[modality.1, 3:10]
    rownames(comp.temp) <- desc
    
    comp.temp <- list(comp.temp)
    names(comp.temp) <- desc[3]
    comp.full <- c(comp.full, comp.temp)
    
    coeff.temp <- coeff[, 3:10] * 0
    coeff.temp[1, ] <- coeff[modality.1, 3:10]
    rownames(coeff.temp) <- desc
    
    coeff.temp <- list(coeff.temp)
    names(coeff.temp) <- desc[3]
    coeff.full <- c(coeff.full, coeff.temp)
    
  }
  
  for (modality.1 in 1:(nM - 1)) {
    for (modality.2 in (modality.1 + 1):nM) {
      
      desc <- c(
        modalities[modality.1],
        modalities[modality.2],
        paste(modalities[modality.1], modalities[modality.2], sep = ".")
      )
      
      comp.temp <- comp[, 3:10] * 0
      comp.temp[1, ] <- comp[modality.1, 3:10]
      comp.temp[2, ] <- comp[modality.2, 3:10]
      comp.temp[3, ] <- comp[
        (as.numeric(comp$modalityID.1) == modality.1) &
          (as.numeric(comp$modalityID.2) == modality.2), 3:10]
      rownames(comp.temp) <- desc
      
      comp.temp <- list(comp.temp)
      names(comp.temp) <- desc[3]
      comp.full <- c(comp.full, comp.temp)
      
      coeff.temp <- coeff[, 3:10] * 0
      coeff.temp[1, ] <- coeff[modality.1, 3:10]
      coeff.temp[2, ] <- coeff[modality.2, 3:10]
      coeff.temp[3, ] <- 2 * coeff[
        (as.numeric(coeff$modalityID.1) == modality.1) &
          (as.numeric(comp$modalityID.2) == modality.2), 3:10]
      rownames(coeff.temp) <- desc
      
      coeff.temp <- list(coeff.temp)
      names(coeff.temp) <- desc[3]
      coeff.full <- c(coeff.full, coeff.temp)
      
    }
  } 
  
  result <- list(comp = comp.full, coeff = coeff.full)
  
  return(result)
  
  
}

origVarDecomp.BDG.MLE <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  comp.1 <- data.frame(
    modalityID.1 = Ustat.full$modalityID.1,
    modalityID.2 = Ustat.full$modalityID.2
  )
  
  desc.numer.BDG <- c(
    "numer1", "numer2", "numer3", "numer4",
    "numer5", "numer6", "numer7", "numer8")
  
  desc.denom.BDG <- c(
    "denom1", "denom2", "denom3", "denom4",
    "denom5", "denom6", "denom7", "denom8")
  
  numer <- Ustat.full[, desc.numer.BDG]
  denom <- Ustat.full[, desc.denom.BDG]
  
  comp.2 <- numer / denom
  
  comp <- cbind(comp.1, comp.2)
  
  names(comp) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  
  
  desc.coeff.BDG <- c(
    "modalityID.1", "modalityID.2",
    "M1.coeff", "M2.coeff", "M3.coeff", "M4.coeff",
    "M5.coeff", "M6.coeff", "M7.coeff", "M8.coeff")
  
  coeff <- Ustat.full[, desc.coeff.BDG]
  names(coeff) <- c(
    "modalityID.1", "modalityID.2",
    "M1", "M2", "M3", "M4",
    "M5", "M6", "M7", "M8")
  
  coeff$modalityID.1 <- factor(coeff$modalityID.1)
  coeff$modalityID.2 <- factor(coeff$modalityID.2)
  
  #### 
  #### Paired modalities loops
  #### 
  comp.full <- list()
  coeff.full <- list()
  for (modality.1 in 1:nM) {
    
    desc <- c(
      modalities[modality.1],
      "NO_MOD",
      paste(modalities[modality.1], "NO_MOD", sep = ".")
    )
    
    comp.temp <- comp[, 3:10] * 0
    comp.temp[1, ] <- comp[modality.1, 3:10]
    rownames(comp.temp) <- desc
    
    comp.temp <- list(comp.temp)
    names(comp.temp) <- desc[3]
    comp.full <- c(comp.full, comp.temp)
    
    coeff.temp <- coeff[, 3:10] * 0
    coeff.temp[1, ] <- coeff[modality.1, 3:10]
    rownames(coeff.temp) <- desc
    
    coeff.temp <- list(coeff.temp)
    names(coeff.temp) <- desc[3]
    coeff.full <- c(coeff.full, coeff.temp)
    
  }
  
  for (modality.1 in 1:(nM - 1)) {
    for (modality.2 in (modality.1 + 1):nM) {
      
      desc <- c(
        modalities[modality.1],
        modalities[modality.2],
        paste(modalities[modality.1], modalities[modality.2], sep = ".")
      )
      
      comp.temp <- comp[, 3:10] * 0
      comp.temp[1, ] <- comp[modality.1, 3:10]
      comp.temp[2, ] <- comp[modality.2, 3:10]
      comp.temp[3, ] <- comp[
        (as.numeric(comp$modalityID.1) == modality.1) &
          (as.numeric(comp$modalityID.2) == modality.2), 3:10]
      rownames(comp.temp) <- desc
      
      comp.temp <- list(comp.temp)
      names(comp.temp) <- desc[3]
      comp.full <- c(comp.full, comp.temp)
      
      coeff.temp <- coeff[, 3:10] * 0
      coeff.temp[1, ] <- coeff[modality.1, 3:10]
      coeff.temp[2, ] <- coeff[modality.2, 3:10]
      coeff.temp[3, ] <- 2 * coeff[
        (as.numeric(coeff$modalityID.1) == modality.1) &
          (as.numeric(comp$modalityID.2) == modality.2), 3:10]
      rownames(coeff.temp) <- desc
      
      coeff.temp <- list(coeff.temp)
      names(coeff.temp) <- desc[3]
      coeff.full <- c(coeff.full, coeff.temp)
      
    }
  } 
  
  result <- list(comp = comp.full, coeff = coeff.full)
  
  return(result)
  
  
}

origVarDecomp.BCK.Ustat <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  desc.comp.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N", "BCK.D", "BCK.ND", "BCK.R", "BCK.NR", "BCK.DR", "BCK.RND")
  
  comp_BCK <- Ustat.full[, desc.comp.BCK]
  names(comp_BCK) <- c("modalityID.1", "modalityID.2",
                       "N", "D", "ND",
                       "R", "NR", "DR", "RND")
  
  desc.coeff.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.coeff", "BCK.D.coeff", "BCK.ND.coeff",
    "BCK.R.coeff", "BCK.NR.coeff", "BCK.DR.coeff", "BCK.RND.coeff")
  
  coeff_BCK <- Ustat.full[, desc.coeff.BCK]
  names(coeff_BCK) <- c("modalityID.1", "modalityID.2",
                        "N", "D", "ND",
                        "R", "NR", "DR", "RND")
  
  ####
  #### Paired modalities loops
  #### 
  comp_BCK.full <- list()
  coeff_BCK.full <- list()
  for (modality.1 in 1:nM) {
    
    desc <- c(
      modalities[modality.1],
      "NO_MOD",
      paste(modalities[modality.1], "NO_MOD", sep = ".")
    )
    
    comp_BCK.temp <- comp_BCK[, 3:ncol(comp_BCK)] * 0
    comp_BCK.temp[1, ] <- comp_BCK[modality.1, 3:ncol(comp_BCK)]
    rownames(comp_BCK.temp) <- desc
    
    comp_BCK.temp <- list(comp_BCK.temp)
    names(comp_BCK.temp) <- desc[3]
    comp_BCK.full <- c(comp_BCK.full, comp_BCK.temp)
    
    coeff_BCK.temp <- coeff_BCK[, 3:ncol(coeff_BCK)] * 0
    coeff_BCK.temp[1, ] <- coeff_BCK[modality.1, 3:ncol(coeff_BCK)]
    rownames(coeff_BCK.temp) <- desc
    
    coeff_BCK.temp <- list(coeff_BCK.temp)
    names(coeff_BCK.temp) <- desc[3]
    coeff_BCK.full <- c(coeff_BCK.full, coeff_BCK.temp)
    
  }
  
  for (modality.1 in 1:(nM - 1)) {
    for (modality.2 in (modality.1 + 1):nM) {
      
      desc <- c(
        modalities[modality.1],
        modalities[modality.2],
        paste(modalities[modality.1], modalities[modality.2], sep = ".")
      )
      
      comp_BCK.temp <- comp_BCK[, 3:ncol(comp_BCK)] * 0
      comp_BCK.temp[1, ] <- comp_BCK[modality.1, 3:ncol(comp_BCK)]
      comp_BCK.temp[2, ] <- comp_BCK[modality.2, 3:ncol(comp_BCK)]
      comp_BCK.temp[3, ] <- comp_BCK[
        (as.numeric(comp_BCK$modalityID.1) == modality.1) &
          (as.numeric(comp_BCK$modalityID.2) == modality.2), 3:ncol(comp_BCK)]
      rownames(comp_BCK.temp) <- desc
      
      comp_BCK.temp <- list(comp_BCK.temp)
      names(comp_BCK.temp) <- desc[3]
      comp_BCK.full <- c(comp_BCK.full, comp_BCK.temp)
      
      coeff_BCK.temp <- coeff_BCK[, 3:ncol(coeff_BCK)] * 0
      coeff_BCK.temp[1, ] <- coeff_BCK[modality.1, 3:ncol(coeff_BCK)]
      coeff_BCK.temp[2, ] <- coeff_BCK[modality.2, 3:ncol(coeff_BCK)]
      coeff_BCK.temp[3, ] <- 2 * coeff_BCK[
        (as.numeric(coeff_BCK$modalityID.1) == modality.1) &
          (as.numeric(comp_BCK$modalityID.2) == modality.2), 3:ncol(coeff_BCK)]
      rownames(coeff_BCK.temp) <- desc
      
      coeff_BCK.temp <- list(coeff_BCK.temp)
      names(coeff_BCK.temp) <- desc[3]
      coeff_BCK.full <- c(coeff_BCK.full, coeff_BCK.temp)
      
    }
  } 
  
  result <- list(comp = comp_BCK.full, coeff = coeff_BCK.full)
  
  return(result)
  
  
}

origVarDecomp.BCK.MLE <- function(Ustat.full, summaryMRMC) {
  
  nM <- summaryMRMC$nM
  modalities <- summaryMRMC$modalities
  
  desc.comp.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.b", "BCK.D.b", "BCK.ND.b", "BCK.R.b", "BCK.NR.b", "BCK.DR.b", "BCK.RND.b")
  
  comp_BCK <- Ustat.full[, desc.comp.BCK]
  names(comp_BCK) <- c("modalityID.1", "modalityID.2",
                       "N", "D", "ND",
                       "R", "NR", "DR", "RND")
  
  desc.coeff.BCK <- c(
    "modalityID.1", "modalityID.2",
    "BCK.N.coeff", "BCK.D.coeff", "BCK.ND.coeff",
    "BCK.R.coeff", "BCK.NR.coeff", "BCK.DR.coeff", "BCK.RND.coeff")
  
  coeff_BCK <- Ustat.full[, desc.coeff.BCK]
  names(coeff_BCK) <- c("modalityID.1", "modalityID.2",
                        "N", "D", "ND",
                        "R", "NR", "DR", "RND")
  
  ####
  #### Paired modalities loops
  #### 
  comp_BCK.full <- list()
  coeff_BCK.full <- list()
  for (modality.1 in 1:nM) {
    
    desc <- c(
      modalities[modality.1],
      "NO_MOD",
      paste(modalities[modality.1], "NO_MOD", sep = ".")
    )
    
    comp_BCK.temp <- comp_BCK[, 3:ncol(comp_BCK)] * 0
    comp_BCK.temp[1, ] <- comp_BCK[modality.1, 3:ncol(comp_BCK)]
    rownames(comp_BCK.temp) <- desc
    
    comp_BCK.temp <- list(comp_BCK.temp)
    names(comp_BCK.temp) <- desc[3]
    comp_BCK.full <- c(comp_BCK.full, comp_BCK.temp)
    
    coeff_BCK.temp <- coeff_BCK[, 3:ncol(coeff_BCK)] * 0
    coeff_BCK.temp[1, ] <- coeff_BCK[modality.1, 3:ncol(coeff_BCK)]
    rownames(coeff_BCK.temp) <- desc
    
    coeff_BCK.temp <- list(coeff_BCK.temp)
    names(coeff_BCK.temp) <- desc[3]
    coeff_BCK.full <- c(coeff_BCK.full, coeff_BCK.temp)
    
  }
  
  for (modality.1 in 1:(nM - 1)) {
    for (modality.2 in (modality.1 + 1):nM) {
      
      desc <- c(
        modalities[modality.1],
        modalities[modality.2],
        paste(modalities[modality.1], modalities[modality.2], sep = ".")
      )
      
      comp_BCK.temp <- comp_BCK[, 3:ncol(comp_BCK)] * 0
      comp_BCK.temp[1, ] <- comp_BCK[modality.1, 3:ncol(comp_BCK)]
      comp_BCK.temp[2, ] <- comp_BCK[modality.2, 3:ncol(comp_BCK)]
      comp_BCK.temp[3, ] <- comp_BCK[
        (as.numeric(comp_BCK$modalityID.1) == modality.1) &
          (as.numeric(comp_BCK$modalityID.2) == modality.2), 3:ncol(comp_BCK)]
      rownames(comp_BCK.temp) <- desc
      
      comp_BCK.temp <- list(comp_BCK.temp)
      names(comp_BCK.temp) <- desc[3]
      comp_BCK.full <- c(comp_BCK.full, comp_BCK.temp)
      
      coeff_BCK.temp <- coeff_BCK[, 3:ncol(coeff_BCK)] * 0
      coeff_BCK.temp[1, ] <- coeff_BCK[modality.1, 3:ncol(coeff_BCK)]
      coeff_BCK.temp[2, ] <- coeff_BCK[modality.2, 3:ncol(coeff_BCK)]
      coeff_BCK.temp[3, ] <- 2 * coeff_BCK[
        (as.numeric(coeff_BCK$modalityID.1) == modality.1) &
          (as.numeric(comp_BCK$modalityID.2) == modality.2), 3:ncol(coeff_BCK)]
      rownames(coeff_BCK.temp) <- desc
      
      coeff_BCK.temp <- list(coeff_BCK.temp)
      names(coeff_BCK.temp) <- desc[3]
      coeff_BCK.full <- c(coeff_BCK.full, coeff_BCK.temp)
      
    }
  } 
  
  result <- list(comp = comp_BCK.full, coeff = coeff_BCK.full)
  
  return(result)
  
  
}

