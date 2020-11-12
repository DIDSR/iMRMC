#' @title MRMC Analysis of Limits of Agreement using ANOVA 
#' @description
#' These two functions calculate two types of Limits of Agreement using ANOVA: Within-Reader Between-Modality(WRBM)
#' and Between-Reader Between-Modality(BRBM). The 95\% confidence interval of the mean difference is also provided.
#' 
#' @details 
#' Suppose the score from reader j for case k under modality i is\eqn{X_{ijk}}, then the difference score from the 
#' same reader for the same cases under two different modalities is \eqn{Y_{jk} = X_{1jk} - X_{2jk}}.
#' \itemize{
#'   \item\code{laWRBM} use two-way random effect ANOVA to analyze the difference scores \eqn{Y_{jk}}. The model 
#'   is \eqn{Y_{jk}=\mu + R_j + C_k + \epsilon_{jk}}, where \eqn{R_j} and \eqn{C_k} are random effects for readers 
#'   and cases. The variance of mean and individual observation is expressed as the linear combination of the MS 
#'   given by ANOVA.
#'   \item\code{laBRBM} use three-way mixed effect ANOVA to analyze the scores \eqn{X_{ijk}}. The model is given by
#'   \eqn{X_{ijk}=\mu + R_j + C_k + m_i + RC_{jk} + mR_{ij} + mC_{ik} + \epsilon_{ijk}}, where \eqn{R_j} and 
#'   \eqn{C_k} are random effects for readers and cases and \eqn{m_i} is a fixed effect for modality. The variance 
#'   of mean and individual observation is expressed as the linear combination of the MS given by ANOVA.
#' }
#' 
#' @name la.anova
#' 
#' @param df 
#' Data frame of observations, one per row. Columns identify random effects, fixed effects,
#' and the observation. Namely,
#' \describe{
#'   \item{readerID}{The factor corresponding to the different readers in the study.
#'     The readerID is treated as a random effect.}
#'   \item{caseID}{The factor corresponding to the different cases in the study.
#'     The caseID is treated as a random effect.}
#'   \item{modalityID}{The factor corresponding to the different modalities in the study.
#'     The modalityID is treated as a fixed effect.}
#'   \item{score}{The score given by the reader to the case for the modality indicated.}
#' }
#' 
#' @param modalitiesToCompare 
#' The factors identifying the modalities to compare. It should be at length 2. Default 
#' \code{modalitiesToCompare = c("testA","testB")}
#' 
#' @param keyColumns 
#' Identify the factors corresponding to the readerID, caseID, modalityID, and score
#' (or alternative random and fixed effects). Default \code{keyColumns = c("readerID", "caseID", 
#' "modalityID", "score")}
#' 
#' @param if.aov 
#' Boolean value to determine whether using aov function to do ANOVA. Default \code{if.aov = TRUE}
#'
#' @return
#' 
#' A dataframe with one row. Each column is as following:
#' \describe{
#'   \item{meanDiff}{The mean of difference score.}  
#'   \item{var.MeanDiff}{The variance of mean difference score}
#'   \item{var.1obs}{The variance of a single WRBM/BRBM difference score}
#'   \item{ci95meanDiff.bot}{Lower bound of 95\% CI for the mean difference score. \code{meanDiff+
#'   1.96*sqrt(var.MeanDiff)}}
#'   \item{ci95meanDiff.top}{Upper bound of 95\% CI for the mean difference score. \code{meanDiff-
#'   1.96*sqrt(var.MeanDiff)}}
#'   \item{la.bot}{Lower bound of WRBM/BRBM Limits of Agreement. \code{meanDiff+2*sqrt(var.1obs)}}
#'   \item{la.top}{Upper bound of WRBM/BRBM Limits of Agreement. \code{meanDiff-2*sqrt(var.1obs)}}
#' }
#' 
#' The two function shows the same 95\% CI for the mean difference score, but difference Limits of Agreements.
#' 
#' @import reshape2
#' 
#' @export
#'
#' @examples
#' library(iMRMC)
#' # Create an MRMC data frame
#' # Refer to Gallas2014_J-Med-Img_v1p031006
#' simRoeMetz.config <- sim.gRoeMetz.config()
#'
#' # Simulate data
#' df.MRMC <- sim.gRoeMetz(simRoeMetz.config)
#' 
#' # Compute Limits of Agreement
#' laWRBM_result <- laWRBM.anova(df.MRMC)
#' print(laWRBM_result)
#' laBRBM_result <- laBRBM.anova(df.MRMC)
#' print(laBRBM_result)
#' 
laWRBM.anova <- function(df, modalitiesToCompare = c("testA","testB"),
                     keyColumns = c("readerID", "caseID", "modalityID", "score"),
                     if.aov = TRUE
) {
  
  if (length(modalitiesToCompare) != 2) {
    print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
    stop("ERROR: modalitiesToCompare should have 2 elements.")
  }
  
  df <- data.frame(readerID = factor(df[[keyColumns[1]]]),
                   caseID = factor(unclass(df[[keyColumns[2]]])), #unclass for changing Ord.factor to unordered
                   modalityID = factor(df[[keyColumns[3]]]),
                   score = df[[keyColumns[4]]])

  
  # Parse out data frames for each modality
  df.A <- df[df$modalityID == modalitiesToCompare[1], ]
  df.B <- df[df$modalityID == modalitiesToCompare[2], ]
  
  nReader <- length(unique(df.A$readerID))
  nCase <- length(unique(df.A$caseID))
  
  # Compute the difference score between the modalities
  diff.df.all <- merge(df.A,df.B, by = c("readerID","caseID"))
  diff.df.all$score <- diff.df.all$score.x - diff.df.all$score.y
  diff.df <- subset(diff.df.all, select = c("readerID","caseID","score"))
  
  if(if.aov){
    # Do ANOVA
    fit <- aov(score ~ readerID + caseID, data = diff.df)
    
    # Extract MS
    MS <- summary(fit)[[1]]$`Mean Sq`
    MSA <- MS[1]
    MSB <- MS[2]
    sigma2 <- MS[3]
  }else{
    diff.mat <- acast(diff.df, readerID ~caseID, value.var = "score")
    
    MSA <- var(rowMeans(diff.mat)) * nCase
    MSB <- var(colMeans(diff.mat)) * nReader
    SST <- var(array(diff.mat)) * (nCase*nReader - 1)
    sigma2 <- (SST - MSA * (nReader - 1) - MSB * (nCase - 1)) / (nReader - 1) / (nCase - 1)
  }
    
  
  # Limit of agreement result
  meanDiff <- mean(diff.df$score)
  var.MeanDiff <- (MSA + MSB - sigma2)/nReader/nCase
  var.1obs <- (nReader * MSA + nCase * MSB + (nReader * nCase - nReader - nCase) * sigma2)/nReader/nCase
  # if use 3-way anova to get var.1obs
  # var.1obs <- 2*(varRM + varCM + sigma2)
  
  la.bot <- meanDiff - 2 * sqrt(var.1obs)
  la.top <- meanDiff + 2 * sqrt(var.1obs)
  
  ci95meanDiff.bot <- meanDiff + qnorm(.025) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top )
  
  return(result2)
  
  
}




#' @rdname la.anova
#'
#' @export
#'

laBRBM.anova <- function(df, modalitiesToCompare = c("testA","testB"),
                     keyColumns = c("readerID", "caseID", "modalityID", "score"),
                     if.aov = TRUE
) {

  if (length(modalitiesToCompare) != 2) {
    print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
    stop("ERROR: modalitiesToCompare should have 2 elements.")
  }

  df <- data.frame(readerID = factor(df[[keyColumns[1]]]),
                   caseID = factor(unclass(df[[keyColumns[2]]])), #unclass for changing Ord.factor to unordered
                   modalityID = factor(df[[keyColumns[3]]]),
                   score = df[[keyColumns[4]]])


  # Parse out data frames for each modality
  df.A <- df[df$modalityID == modalitiesToCompare[1], ]
  df.B <- df[df$modalityID == modalitiesToCompare[2], ]
  
  nM <- 2
  nR <- nlevels(droplevels(df.A)$readerID)
  nC <- nlevels(droplevels(df.A)$caseID)
  
  if(if.aov){
    # apply aov function to do 3-way ANOVA
    df_2Modality <- rbind(df.A, df.B)
    
    fit <- aov(score ~ readerID + caseID + modalityID + readerID:caseID + readerID:modalityID + caseID:modalityID,
               data = df_2Modality)
    # Extract MS
    MS <- summary(fit)[[1]]$`Mean Sq`
    MSR <- MS[1]
    MSC <- MS[2]
    MSM <- MS[3]
    MSRC <- MS[4]
    MSRM <- MS[5]
    MSCM <- MS[6]
    sigma2 <- MS[7]
  
  }else{
    # Generate 3-dimentional matrix, reader x case x modality
    rcm_mat <-array(0,dim=c(nR, nC, nM))
    
    rcm_mat[,,1] <- acast(df.A, readerID ~caseID, value.var = "score")
    rcm_mat[,,2] <- acast(df.B, readerID ~caseID, value.var = "score")
    
    MSR <- var(rowMeans(rcm_mat)) * nC * nM
    MSC <- var(rowMeans(colMeans(rcm_mat))) * nR * nM
    MSM <- var(colMeans(rcm_mat, dims = 2)) * nR * nC
    
    SSR <- MSR * (nR - 1)
    SSC <- MSC * (nC - 1)
    SSM <- MSM * (nM - 1)
    
    SSRC <- var(array(rowMeans(rcm_mat, dims=2))) * nM * (nR*nC-1) - SSR - SSC
    SSRM <- var(array(colMeans(aperm(rcm_mat, c(2,1,3))))) * nC * (nR*nM-1) - SSR - SSM
    SSCM <- var(array(colMeans(rcm_mat))) * nR * (nC*nM-1) - SSM - SSC
    
    MSRC <- SSRC / (nR-1) / (nC-1)
    MSRM <- SSRM / (nR-1) / (nM-1)
    MSCM <- SSCM / (nC-1) / (nM-1)
    
    SST <- var(array(rcm_mat)) * (nC*nR*nM - 1)
    sigma2 <- (SST - SSR -SSC - SSM - SSRC - SSRM - SSCM) / (nR-1) / (nC-1) / (nM-1)
  }
  
  varR <- (MSR + sigma2 - MSRC - MSRM) / nC / nM
  varC <- (MSC + sigma2 - MSRC - MSCM) / nR / nM
  #M2 <- (MSM + sigma2 - MSRM - MSCM) / nR / nC *(nM-1) /2 #fixed effect no variance
  varRC <- (MSRC - sigma2) / nM
  varRM <- (MSRM - sigma2) / nC
  varCM <- (MSCM - sigma2) / nR
  
  # Limit of agreement result
  meanDiff <- mean(df.A$score-df.B$score)
  var.MeanDiff <- 2*(varRM/nR+varCM/nC+sigma2/nR/nC)
  var.1obs <- 2*(varR + varRC + varRM + varCM + sigma2)
  
  la.bot <- meanDiff - 2 * sqrt(var.1obs)
  la.top <- meanDiff + 2 * sqrt(var.1obs)
  
  ci95meanDiff.bot <- meanDiff + qnorm(.025) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top )
  
  return(result2)
}


