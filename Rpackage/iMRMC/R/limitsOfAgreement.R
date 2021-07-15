#---- laWRBM ----
#' @title MRMC analysis of within-reader between-modality limits of agreement
#'
#' @description  The core analysis is done by ustat11 with the difference kernel (kernelFlag = 2).
#'   This calculation can also be accomplished by ustat11 with the identity kernel (kernelFlag = 1),
#'   and the code to do that is provided after the return statement so it never gets exectuted.
#'
#' @param df
#' Data frame of observations, one per row. Columns identify random effects, fixed effects,
#' and the observation. Namely,
#' \itemize{
#'   \item \code{readerID:} The factor corresponding to the different readers in the study.
#'     The readerID is treated as a random effect.
#'   \item \code{caseID:} The factor corresponding to the different cases in the study.
#'     The caseID is treated as a random effect.
#'   \item \code{modalityID:} The factor corresponding to the different modalities in the study.
#'     The modalityID is treated as a fixed effect.
#'   \item \code{score:} The score given by the reader to the case for the modality indicated.
#' }
#'
#' @param modalitiesToCompare
#' The factors identifying the modalities to compare.
#'
#' @param keyColumns
#' Identify the factors corresponding to the readerID, caseID, modalityID, and score
#' (or alternative random and fixed effects).
#'
#' @importFrom stats qnorm
#'
#' @export
#'
laWRBM <- function(
  df, modalitiesToCompare,
  keyColumns = c("readerID", "caseID", "modalityID", "score")
) {

  if (length(modalitiesToCompare) != 2) {
    print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
    stop("ERROR: modalitiesToCompare should have 2 elements.")
  }
  mToCompare <- c(modalitiesToCompare, modalitiesToCompare)

  # The limits of agreement are determined by the MRMC moments of uStat11
  # when the kernel flag is 2.
  # The limits can also be determined from uStat11 when the kernel flag is 1,
  # but it is a little bit more complicated. Refer to validate laWRBM.R for info.
  result1 <- uStat11.conditionalD(
    df, keyColumns = keyColumns,
    modalitiesToCompare = mToCompare, kernelFlag = 2
  )

  meanDiff <- result1$mean[1]
  var.MeanDiff <- result1$var[1]
  var.1obs <- result1$var.1obs[1]

  la.bot <- meanDiff - 2 * sqrt(var.1obs)
  la.top <- meanDiff + 2 * sqrt(var.1obs)

  ci95meanDiff.bot <- meanDiff + qnorm(.025) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- meanDiff + qnorm(.975) * sqrt(var.MeanDiff)

  result2 <- data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, stringsAsFactors = TRUE)

  return(result2)

  #### Alternate method with identical result ####
  #### Uses uStat11 and the identity kernel
  result <- uStat11.jointD(
    df, keyColumns = keyColumns,
    modalitiesToCompare = modalitiesToCompare, kernelFlag = 1)

  moments <- result$moments

  var.Arc = moments$c1r1[1] - moments$c0r0[1]
  var.Brc = moments$c1r1[2] - moments$c0r0[2]
  cov.Ar1cBr1c <- moments$c1r1[3] - moments$c0r0[3]

  var.Ar1cminusBr1c <- var.Arc + var.Brc - 2 * cov.Ar1cBr1c

  print(result2$var)
  print(var.Ar1cminusBr1c)

  print("You should not reach this point because of a previous return statement.")
  browser()

  return(result2)

}

#---- laBRBM ----
#' @title MRMC analysis of between-reader between-modality limits of agreement
#'
#' @description The core analysis is done by ustat11 with the identity kernel (kernelFlag = 1).
#'
#' @param df
#' Data frame of observations, one per row. Columns identify random effects, fixed effects,
#' and the observation. Namely,
#' \itemize{
#'   \item \code{readerID:} The factor corresponding to the different readers in the study.
#'     The readerID is treated as a random effect.
#'   \item \code{caseID:} The factor corresponding to the different cases in the study.
#'     The caseID is treated as a random effect.
#'   \item \code{modalityID:} The factor corresponding to the different modalities in the study.
#'     The modalityID is treated as a fixed effect.
#'   \item \code{score:} The score given by the reader to the case for the modality indicated.
#' }
#'
#' @param modalitiesToCompare
#' The factors identifying the modalities to compare.
#'
#' @param keyColumns
#' Identify the factors corresponding to the readerID, caseID, modalityID, and score
#' (or alternative random and fixed effects).
#'
#' @importFrom stats qnorm
#'
#' @export
#'
laBRBM <- function(
  df, modalitiesToCompare,
  keyColumns = c("readerID", "caseID", "modalityID", "score")
) {

  if (length(modalitiesToCompare) != 2) {
    print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
    stop("ERROR: modalitiesToCompare should have 2 elements.")
  }

  # Estimate the BRBM limits of agreement
  # The limits of agreement are determined by the MRMC moments of uStat11
  # when the kernel flag is 1.
  result1 <- uStat11.conditionalD(
    df, modalitiesToCompare = modalitiesToCompare,
    kernelFlag = 1, keyColumns = keyColumns)

  meanDiff <- result1$mean[3]
  var.MeanDiff <- result1$var[3]

  moments <- result1$moments

  var.Arc = result1$var.1obs[1]
  var.Brc = result1$var.1obs[2]
  cov.Ar1cBr2C <- moments$c1r0[3] - moments$c0r0[3]

  var.Ar1cminusBr2c <- var.Arc + var.Brc - 2 * cov.Ar1cBr2C

  la.bot <- meanDiff - 2 * sqrt(var.Ar1cminusBr2c)
  la.top <- meanDiff + 2 * sqrt(var.Ar1cminusBr2c)

  ci95meanDiff.bot <- meanDiff + qnorm(.025) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- meanDiff + qnorm(.975) * sqrt(var.MeanDiff)

  result2 <- data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.Ar1cminusBr2c,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, stringsAsFactors = TRUE )

return(result2)

}

#---- getWRBM ----
#' @title Get within-reader, between-modality paired data from an MRMC data file
#'
#' @param mcsData A data frame with the following columns: readerID, caseID, modalityID, score
#' @param modality.X The name of one modality
#' @param modality.Y The name of one modality. This should be different from modality.X
#'
#' @return The result of merging the modality.X and modality.Y subsets of mcsData by readerID and caseID
#' 
#' @export
#'
# @examples
getWRBM <- function(mcsData, modality.X, modality.Y) {
  
  # Make an "MRMClist" data frame for modality.X
  df.x <- mcsData[mcsData$modalityID == modality.X,
                  c("readerID", "caseID", "modalityID", "score")]
  # Make an "MRMClist" data frame for modality.Y
  df.y <- mcsData[mcsData$modalityID == modality.Y,
                  c("readerID", "caseID", "modalityID", "score")]
  # Merge the two data frames
  df <- merge(df.x, df.y, by = c("readerID", "caseID"), suffixes = c(".X",".Y"))
  
  return(df)
  
}

#---- getBRBM ----
#' @title Get between-reader, between-modality paired data from an MRMC data file
#'
#' @param mcsData A data frame with the following columns: readerID, caseID, modalityID, score
#' @param modality.X The name of one modality
#' @param modality.Y The name of one modality.
#'
#' @details If modality.Y = modality.X, then the data would be between-reader, within-modality (BRWM).
#' @return The result of merging the modality.X and modality.Y subsets of mcsData by caseID
#'  for every pair of readers
#' 
#' @export
#'
# @examples
getBRBM <- function(mcsData, modality.X, modality.Y) {
  
  # This data frame will hold all the paired observations
  df <- data.frame()
  
  # Split the data by readers
  nReaders <- nlevels(mcsData$readerID)
  mcsData <- split(mcsData, mcsData$readerID)
  for (reader.x in 1:nReaders) {
    for (reader.y in 1:nReaders) {
      
      # Skip the case when the first reader is the same as the second reader.
      if (reader.x == reader.y) next()
      
      # Grab the data frame corresponding to the i'th and j'th readers
      mcsData.x <- mcsData[[reader.x]]
      mcsData.y <- mcsData[[reader.y]]
      
      # Skip the case when a reader has no data
      if (nrow(mcsData.x) == 0) next()
      if (nrow(mcsData.y) == 0) next()
      
      # Make an "MRMClist" data frame for modality.X
      df.x <- mcsData.x[mcsData.x$modalityID == modality.X,
                        c("readerID", "caseID", "modalityID", "score")]
      # Make an "MRMClist" data frame for modality.Y
      df.y <- mcsData.y[mcsData.y$modalityID == modality.Y,
                        c("readerID", "caseID", "modalityID", "score")]
      # Merge the two data frames
      df.temp <- merge(df.x, df.y, by = c("caseID"), suffixes = c(".X",".Y"))
      
      df <- rbind(df, df.temp)
      
    }
    
  }
  
  return(df)
  
}
