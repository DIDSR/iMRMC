# limitsOfAgreement ##########
#' @title MRMC Analysis of Limits of Agreement using ANOVA
#' @description
#' These four functions calculate four types of Limits of Agreement using ANOVA: 
#' Within-Reader Within-Modality(WRWM), Between-Reader Within-Modality(BRWM),
#' Within-Reader Between-Modality(WRBM), and Between-Reader Between-Modality(BRBM). 
#' The 95\% confidence interval of the mean difference is also provided. If the study is fully crossed, the ANOVA 
#' methods are realized either by applying \code{stats::aov} or by matrix multiplication. Otherwise, the SS in ANOVA are
#' computed as residual sums of squares of linear models. See details below about the model structure
#' and these references.
#' 
#' \itemize{
#'   \item S. Wen and B. D. Gallas,
#'     “Three-Way Mixed Effect ANOVA to Estimate MRMC Limits of Agreement,”
#'     \emph{Statistics in Biopharmaceutical Research}, \strong{14}, pp. 532–541, 2022,
#'     \url{https://www.doi.org/10.1080/19466315.2022.2063169}.
#'   \item S. Wen and B. D. Gallas,
#'     “Expanding to Arbitrary Study Designs: ANOVA to Estimate Limits of Agreement for MRMC Studies,”
#'     \emph{arXiv}, 2023, \url{https://www.doi.org/10.48550/ARXIV.2312.16097}.
#' }
#'
#' @details
#' Suppose the score from a reader j for case k under modality \eqn{i} is\eqn{X_{ijk}}, then the difference score from the
#' same reader for the same case under two different modalities is \eqn{Y_{jk} = X_{1jk} - X_{2jk}}.
#' \itemize{
#'   \item\code{laWRBM} use two-way random effect ANOVA to analyze the difference scores \eqn{Y_{jk}}. The model
#'   is \eqn{Y_{jk}=\mu + R_j + C_k + \epsilon_{jk}}, where \eqn{R_j} and \eqn{C_k} are random effects for readers
#'   and cases. The variances of mean and individual observations are expressed as linear combinations of the MS
#'   given by ANOVA.
#'   \item\code{laBRWM} use two-way random effect ANOVA to analyze the scores \eqn{X_{jk}} for a single modality. 
#'   The model is \eqn{X_{jk}=\mu + R_j + C_k + \epsilon_{jk}}, where \eqn{R_j} and \eqn{C_k} are random effects 
#'   for readers and cases. The variances of mean and individual observations are expressed as linear combinations 
#'   of the MS given by ANOVA.
#'   \item\code{laWRWM} use two-way random effect ANOVA to analyze the difference scores \eqn{Y_{jk}} from the same 
#'   reader for the same cases under the same modality with different replicates \eqn{Y_{jk} = X_{jk1} - X_{jk2}}. 
#'   The model is \eqn{Y_{jk}=\mu + R_j + C_k + \epsilon_{jk}}, where \eqn{R_j} and \eqn{C_k} are random effects for 
#'   readers and cases. The variances of mean and individual observations are expressed as  linear combinations of 
#'   the MS given by ANOVA.
#'   \item\code{laBRBM} use three-way mixed effect ANOVA to analyze the scores \eqn{X_{ijk}}. The model is given by
#'   \eqn{X_{ijk}=\mu + R_j + C_k + m_i + RC_{jk} + mR_{ij} + mC_{ik} + \epsilon_{ijk}}, where \eqn{R_j} and
#'   \eqn{C_k} are random effects for readers and cases, \eqn{m_i} is a fixed effect for modality, and the other terms
#'   are interaction terms. The variances of mean and individual observations are expressed as linear combinations
#'   of the MS given by ANOVA.
#' }
#'
#' @name limitsOfAgreement
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
#'   \item{score}{The number (observation) given by the reader to the case for the modality indicated.}
#' }
#'
#' @param modalitiesToCompare
#' The factors identifying the modalities to compare. It should be length 2.
#' Default = \code{c("testA","testB")}
#' 
#' @param replicatesToCompare
#' The factors identifying the replicates to compare for \code{laWRWM}. It should be length 2.
#' Default = \code{c("testA","testB")}
#' 
#' @param modality
#' The factor identifying the modality for laBRWM. It should be length 1.
#' Default = \code{modality = c("testA")}
#'
#' @param keyColumns
#' Identify the factors corresponding to the readerID (random effect), caseID (random effect),
#' modalityID (fixed effect), and score (observation).
#' Default = \code{c("readerID", "caseID", "modalityID", "score")}
#'
#' @param if.aov
#' Boolean value to determine whether to use the `stats::aov` function or to
#' calculate the ANOVA statistics explicitly. `stats::aov` is only appropriate
#' for fully-crossed study only. This flag permits head-to-head comparisons of
#' the output from `stats::aov` and the explicit calculations.
#' Default = \code{TRUE}
#' 
#' @param is.sparseQR 
#' Boolean value to determine whether the `base::qr` function assumes the input
#' data is sparse or not. 
#' Default = \code{TRUE}
#' 
#' @param type
#' Identify how SS are computed in ANOVA for unbalanced study designs.
#' The possible values are c(1,2,3), corresponding to the approaches
#' introduced in the SAS package(Langsrud2003_Stat-Comput_v13p163).
#' 
#' Default \code{type= 1}
#' 
#' @param reader.first
#' Boolean value to determine whether reader effect is added to the model before the case effect. 
#' Default \code{reader.first = TRUE}
#' 
#'
#' @return
#' A list of two dataframes.
#' 
#' The first dataframe is \code{limits.of.agreement}. It has one row. Each column is as follows:
#' \describe{
#'   \item{meanDiff}{The mean difference score.}
#'   \item{var.MeanDiff}{The variance of the mean difference score.}
#'   \item{var.1obs}{The variance of the difference score.}
#'   \item{ci95meanDiff.bot}{Lower bound of 95\% CI for the mean difference score. \code{meanDiff+
#'   1.96*sqrt(var.MeanDiff)}}
#'   \item{ci95meanDiff.top}{Upper bound of 95\% CI for the mean difference score. \code{meanDiff-
#'   1.96*sqrt(var.MeanDiff)}}
#'   \item{la.bot}{Lower Limit of Agreement for the difference score. \code{meanDiff+1.96*sqrt(var.1obs)}}
#'   \item{la.top}{Upper Limit of Agreement for the difference score. \code{meanDiff-1.96*sqrt(var.1obs)}}
#' }
#' 
#' The second dataframe is \code{two.way.ANOVA} or \code{three.way.ANOVA} shows the degrees of freedom, 
#' sums of squares, and estimates of variance components for each source of variation
#'
#'
#' @importFrom stats aov var qnorm model.matrix
#' @importFrom Matrix Matrix tcrossprod
#' @importFrom utils combn
#'
#' @export
#'
#' @examples
#' # Initialize the simulation configuration parameters
#' config <- sim.NormalIG.Hierarchical.config(modalityID = c("testA", "testB"))
#' 
#' # Initizlize the seed and stream of the random number generator
#' init.lecuyerRNG()
#' 
#' # Simulate an MRMC ROC data set
#' dFrame <- sim.NormalIG.Hierarchical(config)
#'
#' # Compute Limits of Agreement
#' laWRBM_result <- laWRBM(dFrame)
#' print(laWRBM_result)
#' laBRBM_result <- laBRBM(dFrame)
#' print(laBRBM_result)
#'
laWRBM <- function(df, modalitiesToCompare = c("testA","testB"),
                   keyColumns = c("readerID", "caseID", "modalityID", "score"),
                   if.aov = TRUE, type = 1, reader.first = TRUE
) {
  
  if(length(modalitiesToCompare) != 2) {
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
  
  # Compute the difference score between the modalities
  diff.df.all <- merge(df.A,df.B, by = c("readerID","caseID"))
  diff.df.all$score <- diff.df.all$score.x - diff.df.all$score.y
  diff.df <- droplevels(subset(diff.df.all, select = c("readerID","caseID","score")))
  
  # Two-way ANOVA model
  fit <- anova2(diff.df, if.aov = if.aov, type = type, reader.first = reader.first)
  
  # size of study
  N <- nrow(diff.df)
  nCperR <- array(table(diff.df$readerID))
  nRperC <- array(table(diff.df$caseID))
  
  # Limit of agreement result
  meanDiff <- mean(diff.df$score,na.rm = T)
  var.MeanDiff <- fit$VarR * sum(nCperR^2)/N^2 + fit$VarC * sum(nRperC^2)/N^2 + fit$sigma2/N
  var.1obs <- fit$VarR + fit$VarC + fit$sigma2
  
  la.bot <- if(var.1obs < 0) NA else meanDiff - qnorm(.975) *  sqrt(var.1obs)
  la.top <- if(var.1obs < 0) NA else meanDiff + qnorm(.975) *  sqrt(var.1obs)
  
  ci95meanDiff.bot <- if(var.MeanDiff < 0) NA else meanDiff - qnorm(.975) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- if(var.MeanDiff < 0) NA else meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- list(limits.of.agreement = data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, row.names = "WRBM" ),
    two.way.ANOVA = data.frame(row.names = c("readerID", "caseID","Error"), 
                               df = c(fit$dfR, fit$dfC, fit$dfE),
                               SS = c(fit$SSR, fit$SSC, fit$SSE),
                               var = c(fit$VarR, fit$VarC, fit$sigma2)))
  
  return(result2)
}

#' @rdname limitsOfAgreement
#' 
#' @export
#'

laBRWM <- function(df, modality = c("testA"),
                   keyColumns = c("readerID", "caseID", "modalityID", "score"),
                   if.aov = TRUE, type = 1, reader.first = TRUE
) {
  
  if(length(modality) != 1) {
    print(paste("length(modality) =", length(modality)))
    stop("ERROR: modality should have only 1 element.")
  }
  
  df <- data.frame(readerID = factor(df[[keyColumns[1]]]),
                   caseID = factor(unclass(df[[keyColumns[2]]])), #unclass for changing Ord.factor to unordered
                   modalityID = factor(df[[keyColumns[3]]]),
                   score = df[[keyColumns[4]]])
  
  
  # Extract data for a single modality
  df <- droplevels(df[df$modalityID == modality[1], ])
  
  # Two-way ANOVA model
  fit <- anova2(df, if.aov = if.aov, type = type, reader.first = reader.first)
  
  # size of study
  nRperC <- array(table(df$caseID))
  N <- sum(nRperC*(nRperC-1)) # total number of BRWM diff
  
  df.perR <- lapply(split(df,df$readerID),droplevels)
  nCperpairedR <-  unlist(lapply(
    utils::combn(levels(df$readerID),2, simplify = F), 
    function(x){
    length(intersect(levels(df.perR[[x[1]]]$caseID),levels(df.perR[[x[2]]]$caseID)))
  }))
  nCperpairedR <- c(nCperpairedR,nCperpairedR) # total number of BRWM diff for each pair of readers
  
  # Limit of agreement result
  meanDiff <- 0
  var.MeanDiff <- 0#2* fit$VarR * sum(nCperpairedR^2)/N^2 + 2 * fit$sigma2/N
  var.1obs <- 2 * fit$VarR + 2 * fit$sigma2
  
  la.bot <- if(var.1obs < 0) NA else meanDiff - qnorm(.975) * sqrt(var.1obs)
  la.top <- if(var.1obs < 0) NA else meanDiff + qnorm(.975) * sqrt(var.1obs)
  
  ci95meanDiff.bot <- if(var.MeanDiff < 0) NA else meanDiff - qnorm(.975) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- if(var.MeanDiff < 0) NA else meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- list(limits.of.agreement = data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, row.names = "BRWM" ),
    two.way.ANOVA = data.frame(row.names = c("readerID", "caseID","Error"), 
                               df = c(fit$dfR, fit$dfC, fit$dfE),
                               SS = c(fit$SSR, fit$SSC, fit$SSE),
                               var = c(fit$VarR, fit$VarC, fit$sigma2)))
  
  return(result2)
}

#' @rdname limitsOfAgreement
#'
#' @export
#'

laWRWM <- function(df, replicatesToCompare = c("testA","testB"),
                   keyColumns = c("readerID", "caseID", "modalityID", "score"),
                   if.aov = TRUE, type = 1, reader.first = TRUE
) {
  
  if(length(replicatesToCompare) != 2) {
    print(paste("length(replicatesToCompare) =", length(replicatesToCompare)))
    stop("ERROR: replicatesToCompare should have 2 elements.")
  }
  
  df <- data.frame(readerID = factor(df[[keyColumns[1]]]),
                   caseID = factor(unclass(df[[keyColumns[2]]])), #unclass for changing Ord.factor to unordered
                   modalityID = factor(df[[keyColumns[3]]]),
                   score = df[[keyColumns[4]]])
  
  
  # Parse out data frames for each replicates
  df.A <- df[df$modalityID == replicatesToCompare[1], ]
  df.B <- df[df$modalityID == replicatesToCompare[2], ]
  
  # Compute the difference score between the replicates
  diff.df.all <- merge(df.A,df.B, by = c("readerID","caseID"))
  diff.df.all$score <- diff.df.all$score.x - diff.df.all$score.y
  diff.df <- droplevels(subset(diff.df.all, select = c("readerID","caseID","score")))
  
  # Two-way ANOVA model
  fit <- anova2(diff.df, if.aov = if.aov, type = type, reader.first = reader.first)
  
  # size of study
  N <- nrow(diff.df)
  nCperR <- array(table(diff.df$readerID))
  nRperC <- array(table(diff.df$caseID))
  
  # Limit of agreement result
  meanDiff <- 0 #as the order of the replicates does not matter
  var.MeanDiff <- 0#fit$VarR * sum(nCperR^2)/N^2 + fit$VarC * sum(nRperC^2)/N^2 + fit$sigma2/N
  var.1obs <- fit$VarR + fit$VarC + fit$sigma2
  
  la.bot <- if(var.1obs < 0) NA else meanDiff - qnorm(.975) * sqrt(var.1obs)
  la.top <- if(var.1obs < 0) NA else meanDiff + qnorm(.975) * sqrt(var.1obs)
  
  ci95meanDiff.bot <- if(var.MeanDiff < 0) NA else meanDiff - qnorm(.975) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- if(var.MeanDiff < 0) NA else meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- list(limits.of.agreement = data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, row.names = "WRWM" ),
    two.way.ANOVA = data.frame(row.names = c("readerID", "caseID","Error"), 
                               df = c(fit$dfR, fit$dfC, fit$dfE),
                               SS = c(fit$SSR, fit$SSC, fit$SSE),
                               var = c(fit$VarR, fit$VarC, fit$sigma2)))
  
  return(result2)
}

#' @rdname limitsOfAgreement
#'
#' @export
#'

laBRBM <- function(df, modalitiesToCompare = c("testA","testB"),
                   keyColumns = c("readerID", "caseID", "modalityID", "score"),
                   if.aov = TRUE, type = 1, reader.first = TRUE,
                   is.sparseQR = T
) {
  
  if(length(modalitiesToCompare) != 2) {
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
  df_2Modality <- droplevels(rbind(df.A, df.B))
  
  fit <- anova3(df.A, df.B, if.aov = if.aov, type = type, reader.first = reader.first,
                    is.sparseQR = is.sparseQR)
  
  # Limit of agreement result
  # meanDiff
  nM <- 2
  nR <- nlevels(df_2Modality$readerID)
  nC <- nlevels(df_2Modality$caseID)
  N <- nrow(df_2Modality)
  rcm_mat <-array(NA,dim=c(nR, nC, nM), dimnames = list(levels(df_2Modality$readerID), 
                                                        levels(df_2Modality$caseID)))
  
  index.A <- data.matrix(df_2Modality[df_2Modality$modalityID==modalitiesToCompare[1], c("readerID", "caseID")])
  index.B <- data.matrix(df_2Modality[df_2Modality$modalityID==modalitiesToCompare[2], c("readerID", "caseID")])
  
  rcm_mat[,,1][index.A] <- df.A$score
  rcm_mat[,,2][index.B] <- df.B$score
  
  y <- mapply(function(X,Y) (X-Y), X=rep(lapply(1:nR,function(i) rcm_mat[i,,1]),nR-1),
              Y=rep(lapply(nR:1,function(i) rcm_mat[i,,2]),each=nR-1))
  y <- array(y)
  meanDiff <- mean(y, na.rm = T)
  var.MeanDiff <- fit[["var.MeanDiff"]]
  var.1obs <- sum(c(2,0,0,2,2,2,2) * fit[["var"]], na.rm = T)
  
  la.bot <- if(var.1obs < 0) NA else meanDiff - qnorm(.975) * sqrt(var.1obs)
  la.top <- if(var.1obs < 0) NA else meanDiff + qnorm(.975) * sqrt(var.1obs)
  
  ci95meanDiff.bot <- if(var.MeanDiff < 0) NA else meanDiff - qnorm(.975) * sqrt(var.MeanDiff)
  ci95meanDiff.top <- if(var.MeanDiff < 0) NA else meanDiff + qnorm(.975) * sqrt(var.MeanDiff)
  
  result2 <- list(limits.of.agreement = data.frame(
    meanDiff = meanDiff, var.MeanDiff = var.MeanDiff, var.1obs = var.1obs,
    ci95meanDiff.bot = ci95meanDiff.bot, ci95meanDiff.top = ci95meanDiff.top,
    la.bot = la.bot, la.top = la.top, row.names = "BRBM" ),
    three.way.ANOVA = data.frame(row.names = c("readerID", "caseID", "modalityID",
                                               "readerID:caseID","readerID:modalityID",
                                               "caseID:modalityID","Error"), 
                                 df = fit[['df']], SS = fit[['SS']], var = fit[['var']]))
  
  return(result2)
}


anova2 <- function(df, if.aov = TRUE, type = 1, reader.first = TRUE
){
  df <- droplevels(df)
  is.incomplete <- F # whether the computation is completed
  
  ## Size of the study ####
  nR <- nlevels(df$readerID)
  nC <- nlevels(df$caseID)
  N <- nrow(df)
  
  ## Degree of freedom ####
  dfR <- nR - 1
  dfC <- nC - 1
  dfE <- (nR - 1) * (nC - 1)
  
  ## Check if the reader and case factors have more than 1 level
  ## and if the number of observation is enough
  if (N==0 || nR == 1 || nC == 1 || N < dfR + dfC + 1) {
    print(paste("nR = ", nR, " nC = ", nC, " N = ", N, " Not enough number of
                observations"))
    is.incomplete <- T
    return(data.frame(dfR = dfR, dfC = dfC, dfE = dfE,
                      SSR = 0, SSC = 0, SSE = 0,
                      VarR = 0, VarC = 0, sigma2 = 0,
                      is.incomplete=is.incomplete))
  }
  
  if(N == nR*nC){
    ## fully crossed ####
    # all different types of SS give the same result 
    if(if.aov){
      ##  Do ANOVA
      fit <- aov(score ~ readerID + caseID, data = df)
      
      # Extract SS
      SS <- summary(fit)[[1]]$`Sum Sq`
      SSR <- SS[1]
      SSC <- SS[2]
      SSE <- SS[3]
    }else{
      diff.mat <- array(-1, c(nR, nC), dimnames = list(levels(df$readerID), 
                                                       levels(df$caseID)))
      index <- data.matrix(df[, c("readerID", "caseID")])
      
      diff.mat[index] <- df$score
      
      SSR <- var(rowMeans(diff.mat)) * nC * (nR - 1)
      SSC <- var(colMeans(diff.mat)) * nR * (nC - 1)
      SST <- var(array(diff.mat)) * (nC*nR - 1)
      SSE <- (SST - SSR  - SSC )
    }
    
    # Estimation of variance component
    varR <- (SSR/dfR-SSE/dfE)/nC
    varC <- (SSC/dfC-SSE/dfE)/nR
    sigma2 <- SSE/dfE
    
  }else{
    ## arbitrary study ####
    Y <- df$score
    X_1 <- rep(1,N)
    
    X_r <- array(0,dim =c(N,nR))
    X_r[cbind(1:N,as.numeric(df$readerID))] <-1
    X_r <- as.matrix(X_r)
    X_c <- array(0,dim =c(N,nC))
    X_c[cbind(1:N,as.numeric(df$caseID))] <-1
    X_c <- as.matrix(X_c)
    
    R1 <- RSS_2w(X_1,Y, X_r, X_c)
    RA <- RSS_2w(cbind(X_1, X_r[,2:nR]),Y, Xc = X_c)
    RB <- RSS_2w(cbind(X_1, X_c[,2:nC]),Y, X_r)
    RAB <- RSS_2w(cbind(X_1, X_r[,2:nR], X_c[,2:nC]),Y)
    
    SSE <- RAB$rss
    dfE <- RAB$df
    if(type == 1){
      if(reader.first){
        SSR<- R1$rss-RA$rss
        SSC<- RA$rss-RAB$rss
        
        coef_mat <- rbind(R1$coef_var-RA$coef_var, 
                          RA$coef_var-RAB$coef_var,
                          RAB$coef_var)
        
      }else{
        SSR <- RB$rss-RAB$rss 
        SSC <- R1$rss-RB$rss
        
        coef_mat <- rbind(RB$coef_var-RAB$coef_var, 
                          R1$coef_var-RB$coef_var,
                          RAB$coef_var)
        
      }
    }else if(type == 2 | type == 3){
      SSR <- RB$rss-RAB$rss 
      SSC <- RA$rss-RAB$rss
      
      coef_mat <- rbind(RB$coef_var-RAB$coef_var, 
                        RA$coef_var-RAB$coef_var,
                        RAB$coef_var)
      
    }else{
      stop("ERROR: type should be one of '1', '2', '3'.")
    }
    # Degree of freedom
    dfR <- coef_mat[1,3]
    dfC <- coef_mat[2,3]
    dfE <- coef_mat[3,3]
    
    # Estimation of variance component
    if(class(try(solve(coef_mat), silent = T))[1] == "try-error"){
      varR <- 0
      varC <- 0
      sigma2 <- 0
      is.incomplete <- T
    }else{
      var <- solve(coef_mat) %*% c(SSR, SSC, SSE)
      varR <- var[1]
      varC <- var[2]
      sigma2 <- var[3]
    }
    
  }
  
  return(data.frame(dfR = dfR, dfC = dfC, dfE = dfE,
                    SSR = SSR, SSC = SSC, SSE = SSE,
                    VarR = varR, VarC = varC, sigma2 = sigma2,
                    is.incomplete=is.incomplete))
}

RSS_2w <- function(X, Y, Xr=NULL, Xc=NULL, Xe=diag(length(Y))){
  
  k <- if(is.matrix(X)) ncol(X) else 1
  
  if(class(try(solve(t(X)%*%X), silent = T))[1] == "try-error"){
    decom <- qr(X)
    k <- decom$rank
    q <- qr.Q(decom)[,1:k]
    
    M <- diag(length(Y)) - tcrossprod(q)
    var <- rep(0,6)
  }else{
    M <- diag(length(Y)) - X %*% solve(t(X)%*%X) %*% t(X)
  }
  
  residual <- M %*% Y
  
  coef_r <- 0
  if(!is.null(Xr)) coef_r <- sum(diag(M %*%  Xr %*% t(Xr)))
  
  coef_c <- 0
  if(!is.null(Xc)) coef_c <- sum(diag(M %*%  Xc %*% t(Xc)))
  
  return(list(rss=t(residual)%*%residual, coef_var= c(coef_r, coef_c, length(Y)-k),
              df=length(Y)-k))
}

anova3 <- function(df.A, df.B, if.aov = TRUE, type = 1, reader.first = TRUE,
                       is.sparseQR = T
){
  is.incomplete <- F # whether the computation is completed
  
  df_2Modality <- droplevels(rbind(df.A, df.B))
  
  ## Size of the study ####
  nM <- 2
  nR <- nlevels(df_2Modality$readerID)
  nC <- nlevels(df_2Modality$caseID)
  N <- nrow(df_2Modality)
  
  ## Degree of freedom ####
  dfR <- nR - 1
  dfC <- nC - 1
  dfM <- nM - 1
  
  if(N == nM*nR*nC && type != 3){
    ## fully crossed ####
    # all different types of SS give the same result 
    dfRC <- dfR * dfC
    dfRM <- dfR * dfM
    dfCM <- dfC * dfM
    dfE <- N - nR * nC - nR * nM - nC* nM + nR + nC + nM - 1
    
    if(if.aov){
      # apply aov function to do 3-way ANOVA
      df_2Modality <- rbind(df.A, df.B)
      
      fit <- aov(score ~ readerID + caseID + modalityID + readerID:caseID + readerID:modalityID + caseID:modalityID,
                 data = df_2Modality)
      # Extract SS
      SS <- summary(fit)[[1]]$`Sum Sq`
      SSR <- SS[1]
      SSC <- SS[2]
      SSM <- SS[3]
      SSRC <- SS[4]
      SSRM <- SS[5]
      SSCM <- SS[6]
      SSE <- SS[7]
      
    }else{
      # Generate 3-dimensional matrix, reader x case x modality
      rcm_mat <-array(0,dim=c(nR, nC, nM), dimnames = list(levels(df$readerID), 
                                                           levels(df$caseID)))
      
      index.A <- data.matrix(df.A[, c("readerID", "caseID")])
      index.B <- data.matrix(df.B[, c("readerID", "caseID")])
      
      rcm_mat[,,1][index.A] <- df.A$score
      rcm_mat[,,2][index.B] <- df.B$score
      
      # Compute SS for different sources of variation
      SSR <- var(rowMeans(rcm_mat)) * nC * nM * dfR
      SSC <- var(rowMeans(colMeans(rcm_mat))) * nR * nM * dfC
      SSM <- var(colMeans(rcm_mat, dims = 2)) * nR * nC * dfM
      
      SSRC <- var(array(rowMeans(rcm_mat, dims=2))) * nM * (nR*nC-1) - SSR - SSC
      SSRM <- var(array(colMeans(aperm(rcm_mat, c(2,1,3))))) * nC * (nR*nM-1) - SSR - SSM
      SSCM <- var(array(colMeans(rcm_mat))) * nR * (nC*nM-1) - SSM - SSC
      
      SST <- var(array(rcm_mat)) * (nC*nR*nM - 1)
      SSE <- (SST - SSR -SSC - SSM - SSRC - SSRM - SSCM)
      
      SS <- c(SSR, SSC, SSM, SSRC, SSRM, SSCM, SSE)
    }
    
    # Estimation of variance component
    sigma2 <- SSE/dfE
    varR <- (SSR/dfR + sigma2 - SSRC/dfRC - SSRM/dfRM) / nC / nM
    varC <- (SSC/dfC + sigma2 - SSRC/dfRC - SSCM/dfCM) / nR / nM
    varRC <- (SSRC/dfRC - sigma2) / nM
    varRM <- (SSRM/dfRM - sigma2) / nC
    varCM <- (SSCM/dfCM - sigma2) / nR
    
    # Variance of mean diff
    var.MeanDiff <- 2*(varRM/nR+varCM/nC+sigma2/nR/nC)
    
    # Summarize the result
    var <- c(varR, varC, NA, varRC, varRM, varCM, sigma2)
    df <- c(dfR, dfC, dfM, dfRC, dfRM, dfCM, dfE)
    
  }else{
    ## arbitrary study or type III SS ####
    
    ## Generate Design Matrices
    if(reader.first){
      X<-model.matrix(~ readerID + caseID + modalityID + readerID:caseID + 
                        readerID:modalityID + caseID:modalityID, data = df_2Modality)
    }else(
      X<-model.matrix(~ caseID + readerID + modalityID + readerID:caseID + 
                        readerID:modalityID + caseID:modalityID, data = df_2Modality)
    )
    
    X_r <- model.matrix(~-1+readerID, data=df_2Modality)
    X_c <- model.matrix(~-1+caseID, data=df_2Modality)
    X_m <- model.matrix(~-1+modalityID, data=df_2Modality)
    X_rc <- model.matrix(~-1+readerID:caseID, data=df_2Modality)
    X_rm <- model.matrix(~-1+readerID:modalityID, data=df_2Modality)
    X_cm <- model.matrix(~-1+caseID:modalityID, data=df_2Modality)
    
    Y <- df_2Modality$score
    
    ## Compute SS and coefficient for the variance components
    R.full <- RSS(X,Y,output.q = T,is.sparseQR = is.sparseQR)
    if(is.sparseQR){
      sparseX <- Matrix(X[,R.full$ind],sparse = TRUE)
    }else{
      sparseX <- NULL
    }
    
    R.no_CM <- RSS(seq(1,nR*(nC+nM-1)),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xcm = X_cm)
    
    SSCM <- R.no_CM$rss - R.full$rss
    SSE <- R.full$rss
    
    coef_mat <- rbind(R.no_CM$coef_var - R.full$coef_var, R.full$coef_var)
    
    
    
    if (type == 1){
      
      R.1 <- RSS(seq(1), Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xr= X_r, Xc = X_c, Xrc = X_rc, Xrm = X_rm, Xcm = X_cm)
      
      if(reader.first){
        R.first <- RSS(seq(1,nR),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xc = X_c, Xrc = X_rc, Xrm = X_rm, Xcm = X_cm)
      }else{
        R.first <- RSS(seq(1,nC),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xr = X_r, Xrc = X_rc, Xrm = X_rm, Xcm = X_cm)
      }
      
      R.R_C <- RSS(seq(1,nR+nC-1),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xrc = X_rc, Xrm = X_rm, Xcm = X_cm)
      
      R.R_C_M <- RSS(seq(1,nR+nC+nM-2),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xrc = X_rc, Xrm = X_rm, Xcm = X_cm)
      
      R.RC_M <- RSS(seq(1,nM+nR*nC-1),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xrm = X_rm, Xcm = X_cm)
      
      SSM <- R.R_C$rss - R.R_C_M$rss
      SSRC <- R.R_C_M$rss - R.RC_M$rss
      SSRM <- R.RC_M$rss - R.no_CM$rss
      
      coef_mat <- rbind(R.R_C_M$coef_var - R.RC_M$coef_var,
                        R.RC_M$coef_var - R.no_CM$coef_var, coef_mat)
      
      if(reader.first){
        SSR <- R.1$rss - R.first$rss
        SSC <- R.first$rss - R.R_C$rss
        coef_mat <- rbind(R.1$coef_var - R.first$coef_var, 
                          R.first$coef_var - R.R_C$coef_var, coef_mat)
      }else{
        SSC <- R.1$rss - R.first$rss
        SSR <- R.first$rss - R.R_C$rss
        coef_mat <- rbind(R.first$coef_var - R.R_C$coef_var,
                          R.1$coef_var - R.first$coef_var, coef_mat)
      }
      
      
    }else{
      
      R.no_RM <- RSS(model.matrix(~ readerID + caseID + modalityID + readerID:caseID + caseID:modalityID, 
                                  data = df_2Modality),Y,is.sparseQR=is.sparseQR, Xrm = X_rm)
      
      R.no_RC <- RSS(model.matrix(~ readerID + caseID + modalityID + readerID:modalityID + caseID:modalityID, 
                                  data = df_2Modality),Y,is.sparseQR=is.sparseQR, output.q = T,Xrc = X_rc)
      
      SSRC <- R.no_RC$rss - SSE
      SSRM <- R.no_RM$rss - SSE
      
      coef_mat <- rbind(R.no_RC$coef_var - R.full$coef_var,
                        R.no_RM$coef_var - R.full$coef_var, coef_mat)
      
      if(type == 2){
        R.CM <- RSS(model.matrix(~ caseID + modalityID + caseID:modalityID, 
                                 data = df_2Modality),Y, Xr = X_r, Xrc = X_rc, Xrm = X_rm)
        R.CM_R <- RSS(model.matrix(~ readerID + caseID + modalityID + caseID:modalityID, 
                                   data = df_2Modality),Y, Xrc = X_rc, Xrm = X_rm)
        R.RM <- RSS(model.matrix(~ readerID + modalityID + readerID:modalityID, 
                                 data = df_2Modality),Y, Xc = X_c, Xrc = X_rc, Xcm = X_cm)
        R.RC <- RSS(model.matrix(~ readerID + caseID + readerID:caseID, 
                                 data = df_2Modality),Y, Xrm = X_rm, Xcm = X_cm)
        R.RC_M <- RSS(seq(1,nM+nR*nC-1),Y, R.full$q, R.full$ind,is.sparseQR,sparseX, Xrm = X_rm, Xcm = X_cm)
        if(is.sparseQR){
          X<-model.matrix(~ readerID + caseID + modalityID + readerID:modalityID + caseID:modalityID, 
                          data = df_2Modality)
          sparseX<-Matrix(X[,R.no_RC$ind],sparse=TRUE)
        }
        R.RM_C <- RSS(seq(1,nR*nM+nC-1),Y,R.no_RC$q, R.no_RC$ind,is.sparseQR,sparseX, Xrc = X_rc, Xcm = X_cm)
        
        SSR <- R.CM$rss - R.CM_R$rss
        SSC <- R.RM$rss - R.RM_C$rss
        SSM <- R.RC$rss - R.RC_M$rss
        
        coef_mat <- rbind(R.CM$coef_var - R.CM_R$coef_var, 
                          R.RM$coef_var - R.RM_C$coef_var, coef_mat)
      }else if(type == 3){
        R.no_R <- RSS(X[,c(1,(nR+1):ncol(X))],Y,is.sparseQR=is.sparseQR, Xr = X_r)
        
        R.no_C <- RSS(X[,c(1:nR,(nR+nC):ncol(X))],Y,is.sparseQR=is.sparseQR, Xc = X_c)
        
        R.no_M <- RSS(X[,c(1:(nR+nC-1),(nR+nC+nM-1):ncol(X))],Y,is.sparseQR=is.sparseQR)
        
        SSR <- R.no_R$rss - SSE
        SSC <- R.no_C$rss - SSE
        SSM <- R.no_M$rss - SSE
        
        coef_mat <- rbind(R.no_R$coef_var - R.full$coef_var, 
                          R.no_C$coef_var - R.full$coef_var, coef_mat)
      }else{
        stop("ERROR: type should be one of '1', '2', '3'.")
      }
      
      
    }
    # Degree of freedom
    df <- c(coef_mat[1:2,6],dfM,coef_mat[3:6,6])
    
    # Summarize the result
    SS <- c(SSR, SSC, SSM, SSRC, SSRM, SSCM, SSE)
    
    
    ## Estimation of variance component
    if(class(try(solve(coef_mat), silent = T))[1] == "try-error"){
      var <- rep(0,6)
      is.incomplete = F
    }else{
      var <- solve(coef_mat) %*% c(SSR, SSC, SSRC, SSRM, SSCM, SSE)
    }
    
    ## variance of mean diff
    var.MeanDiff <- c(0,0,0,
                      sum(c(sum(colSums(X_rm)[1:nR]^2),sum(colSums(X_rm)[-(1:nR)]^2))/colSums(X_m)^2),
                      sum(c(sum(colSums(X_cm)[1:nC]^2),sum(colSums(X_cm)[-(1:nC)]^2))/colSums(X_m)^2),
                      sum(c(1,1)/colSums(X_m))) %*% var
    var <- c(var[1:2],NA,var[3:6])
  }
  
  return(list(df=df, SS=SS, var=var, var.MeanDiff = var.MeanDiff,
              is.incomplete=is.incomplete))
}

RSS <- function(X, Y, q=NULL,ind=NULL, is.sparseQR=F, sparseX=NULL, 
                output.q = F, Xr=NULL, Xc=NULL, Xrc=NULL, Xrm=NULL, Xcm=NULL){
  
  ## QR decomposition if X is matrix; extract Q matrix when X is a vector
  if(is.matrix(X)){
    decom <- qr(X)
    k <- decom$rank
    ind <- decom$pivot[1:k]
    
    if(is.sparseQR){
      decom2<- qr(Matrix(X[,ind],sparse=TRUE))
      q <- qr.Q(decom2)
    }else{
      q <- qr.Q(decom)[,1:k]
    }
    
  }else{
    if(is.sparseQR){
      if(is.null(sparseX)){
        stop("Please provide X matrix for sparseQR")
      }else{
        k <- sum(ind %in% X)
        decom2 <- qr(sparseX[,which(ind %in% X)])
        q <- qr.Q(decom2)
      }
    }else{
      if(is.null(q)){
        stop("Please provide decomposed Q matrix, otherwise provide X as a matrix")
      }else{
        k <- sum(ind %in% X)
        q <- q[,1:k]
      }
    }
    
  }
  
  ## residual maker matrix
  M <- diag(length(Y)) - tcrossprod(q)
  
  ## determine # of variance components
  X_res <- list(Xr, Xc, Xrc, Xrm, Xcm)
  
  
  coef_var <- unlist(lapply(X_res, function(X) if(!is.null(X)) sum(diag(as.matrix(M %*%  tcrossprod(Matrix(X,sparse=TRUE))))) else 0))
  if((!output.q) | is.sparseQR){
    q=NULL
  }
  return(list(rss=as.numeric(crossprod(Y,M)%*%Y), 
              coef_var= c(coef_var, length(Y)-k),
              q = q,
              ind = ind))
}



#---- getWRBM ----
#' @title Get within-reader, between-modality paired data from an MRMC data frame
#'
#' @param mrmcDF A data frame with the following columns: readerID, caseID, modalityID, score
#' @param modality.X The name of one modality
#' @param modality.Y The name of one modality. This should be different from modality.X
#'
#' @return The result of merging the modality.X and modality.Y subsets of mrmcDF by readerID and caseID
#' 
#' @export
#'
# @examples
getWRBM <- function(mrmcDF, modality.X, modality.Y) {
  
  # Make an "MRMClist" data frame for modality.X
  df.x <- mrmcDF[mrmcDF$modalityID == modality.X,
                 c("readerID", "caseID", "modalityID", "score")]
  # Make an "MRMClist" data frame for modality.Y
  df.y <- mrmcDF[mrmcDF$modalityID == modality.Y,
                 c("readerID", "caseID", "modalityID", "score")]
  # Merge the two data frames
  df <- merge(df.x, df.y, by = c("readerID", "caseID"), suffixes = c(".X",".Y"))
  
  return(df)
  
}

#---- getBRBM ----
#' @title Get between-reader, between-modality paired data from an MRMC data frame
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
  
  # Make sure fields are factors
  mcsData$readerID <- factor(mcsData$readerID)
  mcsData$caseID <- factor(mcsData$caseID)
  mcsData$modalityID <- factor(mcsData$modalityID)
  
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
