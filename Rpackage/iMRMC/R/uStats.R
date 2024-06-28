#---- uStat11.jointD ----
#' @title Analysis of U-statistics degree 1,1
#'
#' @description
#' These two functions calculate the mean and variance of a user-specified U-statistic kernel,
#' which is a function of cross-correlated scores.
#'
#' The motivation for this analysis is data collected in imaging studies
#' where multiple readers read multiple cases in different modes or modalities.
#' The goal is to evaluate the variance of a reader- and case-averaged endpoint,
#' accounting for cross-correlated data arising from two random effects:
#' the random reader skill and the random case difficulty.
#' This analysis is sometimes referred to as an MRMC analysis.
#' Of course, the random effects can be from sources other than readers and cases.
#'
#' @details
#'
#' \code{uStat11.conditionalD} is identical to \code{uStat11.jointD} when the study is fully-crossed:
#' when every reader readers all the cases in both modalities. For arbitrary study designs
#' the two functions differ according to how the components of variance are estimated.
#' \itemize{
#'   \item\code{uStat11.conditionalD} follows Gallas2007_J-Opt-Soc-Am-A_v24pB70
#'   <doi:10.1364/JOSAA.24.000B70> and estimates the components of variance
#'   (which isolate combinations of different random effects) with nested conditional means.
#'   \item\code{uStat11.jointD} is analogous to the method in Gallas2008_Neural-Networks_v21p387
#'   <doi:10.1016/j.neunet.2007.12.013> and estimates the components of variance
#'   (which isolate combinations of different random effects) with a joint distribution over all
#'   the observations giving equal weight to each one.
#' }
#'
#' Both functions yield unbiased variance estimates.
#' Our simulations find that \code{uStat11.conditionalD} is statistically more efficient than
#' \code{uStat11.jointD} (its variance estimate is more precise), but it is slower.
#'
#' Please refer to the tests/testthat folder of the package for examples using these functions.
#'
#' @name uStat11
#'
#' @param df.input an iMRMC formatted data frame, see \link{dfMRMC_example}
#' 
#'
#' @param keyColumns
#' Identify the factors corresponding to the readerID, caseID, modalityID, and score
#' (or alternative random and fixed effects).
#'
#' @param modalitiesToCompare
#' The factors identifying the modalities to compare.
#'
#' @param kernelFlag This determines the kernel function
#' \itemize{
#'   \item\code{kernelFlag} = 1 == identity kernel: requires two modalities: A,B.
#'   \item\code{kernelFlag} = 2 == kernel of the difference in modalities: requires four modalities: A,B,C,D.
#' }
#'
#' @return
#' This function calculates the mean and variance of the indicated U-statistic kernel,
#' which is a function of the scores. For the identity kernel, we simply return the mean
#' and variance of the scores.
#'
#' The function returns a list of outputs. Many of these outputs have three elements.
#' \itemize{
#'   \item If \code{kernelFlag} = 1 == identity kernel, the first element corresponds to the mean score of
#' modality A, the second corresponds to mean score of modality B,
#' and the third corresponds to the mean of the difference in scores from modality A and B.
#'   \item  If \code{kernelFlag} = 2 == difference kernel, the first element corresponds to the
#' mean difference in scores from modalities A and B, the second element corresponds to
#' the mean difference in scores from modalities C and D, and the third elements corresponds
#' to the difference of the just-mentioned differences.
#' }
#'
#' There are 16 outputs:
#' \itemize{
#'   \item\code{mean:} See description above.
#'   \item\code{var:} The variance of the mean.
#'   \item\code{var.1obs:} The variance of one reader-case-modality observation.
#'   \item\code{meanPerR} The reader-specific means.
#'   \item\code{nR} The number of readers in the study.
#'   \item\code{nC} The number of cases in the study.
#'   \item\code{nCperR} The number of cases evaluated by each reader for each modality.
#'   \item\code{moments} The second order moments of the problem.
#'   \item\code{coeff} The coefficients corresponding to the second-order moments such that
#'     the scalar product between the moments and coefficients yields the variance.
#'   \item\code{kernel.A} A matrix showing the kernel evaluated for each combination
#'     of each reader and case for modality A (or AB).
#'   \item\code{design.A} A matrix showing the what data exists for each combination
#'     of each reader and case for modality A (or AB).
#'   \item\code{kernel.B} A matrix showing the kernel evaluated for each combination
#'     of each reader and case for modality B (or CD).
#'   \item\code{design.B} A matrix showing the what data exists for each combination
#'     of each reader and case for modality B (or CD).
#' }
#'
#' @export
#'
#' @examples
#' # Create an MRMC data frame
#' # Refer to Gallas2014_J-Med-Img_v1p031006
#' simRoeMetz.config <- sim.gRoeMetz.config()
#'
#' # Simulate data
#' df.MRMC <- sim.gRoeMetz(simRoeMetz.config)
#'
#' # Reformat data
#' df <- undoIMRMCdf(df.MRMC)
#'
#' # Grab part of the data
#' df <- droplevels(df[grepl("pos", df$caseID), ])
#'
#' #### uStat11.jointD.identity ####
#' # Calculate the reader- and case-averaged difference in scores from testA and testB
#' # (kernelFlag = 1 specifies the U-statistics kernel to be the identity)
#' result.jointD.identity <- uStat11.jointD(
#'   df,
#'   kernelFlag = 1,
#'   keyColumns = c("readerID", "caseID", "modalityID", "score"),
#'   modalitiesToCompare = c("testA", "testB"))
#'
#' cat("\n")
#' cat("uStat11.jointD.identity \n")
#' print(result.jointD.identity[1:2])

uStat11.jointD <- function(
    df.input, modalitiesToCompare, kernelFlag = 1,
    keyColumns = c("readerID", "caseID", "modalityID", "score")
) {
  
  if (!inherits(modalitiesToCompare, "character")) {
    stop(paste(
      "class(modalitiesToCompare) =", class(modalitiesToCompare),
      "... The class should be character.")
    )
  }
  
  if (kernelFlag == 1) {
    
    if (length(modalitiesToCompare) != 2) {
      print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
      stop("ERROR: modalitiesToCompare should have 2 elements.")
    }
    
    CbyR.identity <- uStat11.identity(
      df.input,
      keyColumns = keyColumns,
      modalitiesToCompare = modalitiesToCompare)
    kernel.A = CbyR.identity$kernel.A
    design.A = CbyR.identity$design.A
    kernel.B = CbyR.identity$kernel.B
    design.B = CbyR.identity$design.B
    
    desc <- c("A", "B", "AminusB")
    
  } else if (
    kernelFlag == 2) {
    
    if (length(modalitiesToCompare) != 4) {
      print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
      stop("ERROR: modalities to compare should have 4 elements.")
    }
    
    CbyR.diff <- uStat11.diff(
      df.input,
      keyColumns = keyColumns,
      modalitiesToCompare = modalitiesToCompare)
    kernel.A = CbyR.diff$kernel.AB
    design.A = CbyR.diff$design.AB
    kernel.B = CbyR.diff$kernel.CD
    design.B = CbyR.diff$design.CD
    
    desc <- c("AminusB", "CminusD", "AminusB.minus.CminusD")
    
  }
  
  nC <- nrow(design.A)
  nR <- ncol(design.A)
  
  # Estimate the percent correct for each reader and modality
  sumiS.A <- colSums(kernel.A)
  sumiS.B <- colSums(kernel.B)
  sumiD.A <- colSums(design.A)
  sumiD.B <- colSums(design.B)
  
  meanPerR <- data.frame(sumiS.A / sumiD.A, sumiS.B / sumiD.B, stringsAsFactors = TRUE)
  names(meanPerR) <- c(desc[1], desc[2])
  
  # Declare the constituent parts of the variance decomposition
  # Please refer to Gallas2009_Commun-Stat-A-Theor_v38p2586
  numer.biased <- data.frame(
    c0r0 = c(0, 0, 0),
    c1r0 = c(0, 0, 0),
    c0r1 = c(0, 0, 0),
    c1r1 = c(0, 0, 0),
    row.names = c("AB", "CD", "ABminusCD"),
    stringsAsFactors = TRUE
  )
  denom.biased <- numer.biased
  
  #### M.c0r0 ####
  # Estimate the biased product moment given
  # 2 independent cases and 2 independent readers
  numer.temp.A <- sum(design.A * kernel.A)
  denom.temp.A <- sum(design.A)
  numer.temp.B <- sum(design.B * kernel.B)
  denom.temp.B <- sum(design.B)
  numer.biased$c0r0[1] <- numer.temp.A * numer.temp.A
  denom.biased$c0r0[1] <- denom.temp.A * denom.temp.A
  numer.biased$c0r0[2] <- numer.temp.B * numer.temp.B
  denom.biased$c0r0[2] <- denom.temp.B * denom.temp.B
  numer.biased$c0r0[3] <- numer.temp.A * numer.temp.B
  denom.biased$c0r0[3] <- denom.temp.A * denom.temp.B
  
  # The number of reader-case observations in common across modalities to be compared
  nObs.A <- denom.temp.A
  nObs.B <- denom.temp.B
  
  # The mean of the kernel
  mean.A <- numer.temp.A / nObs.A
  mean.B <- numer.temp.B / nObs.B
  
  #### M.c1r0 ####
  # Estimate the biased product moment given
  # 1 independent case and 2 independent readers
  numer.temp.A <- apply(design.A * kernel.A, 1, sum)
  denom.temp.A <- apply(design.A, 1, sum)
  numer.temp.B <- apply(design.B * kernel.B, 1, sum)
  denom.temp.B <- apply(design.B, 1, sum)
  numer.biased$c1r0[1] <- sum(numer.temp.A * numer.temp.A)
  denom.biased$c1r0[1] <- sum(denom.temp.A * denom.temp.A)
  numer.biased$c1r0[2] <- sum(numer.temp.B * numer.temp.B)
  denom.biased$c1r0[2] <- sum(denom.temp.B * denom.temp.B)
  numer.biased$c1r0[3] <- sum(numer.temp.A * numer.temp.B)
  denom.biased$c1r0[3] <- sum(denom.temp.A * denom.temp.B)
  
  #### M.c0r1 ####
  # Estimate the biased product moment given
  # 2 cases and 1 reader
  numer.temp.A <- apply(design.A * kernel.A, 2, sum)
  denom.temp.A <- apply(design.A, 2, sum)
  numer.temp.B <- apply(design.B * kernel.B, 2, sum)
  denom.temp.B <- apply(design.B, 2, sum)
  numer.biased$c0r1[1] <- sum(numer.temp.A * numer.temp.A)
  denom.biased$c0r1[1] <- sum(denom.temp.A * denom.temp.A)
  numer.biased$c0r1[2] <- sum(numer.temp.B * numer.temp.B)
  denom.biased$c0r1[2] <- sum(denom.temp.B * denom.temp.B)
  numer.biased$c0r1[3] <- sum(numer.temp.A * numer.temp.B)
  denom.biased$c0r1[3] <- sum(denom.temp.A * denom.temp.B)
  
  #### M.c1r1 ####
  # Estimate the biased product moment given
  # 1 case and 1 reader
  numer.biased$c1r1[1] <- sum(design.A * kernel.A * design.A * kernel.A)
  denom.biased$c1r1[1] <- sum(design.A * design.A)
  numer.biased$c1r1[2] <- sum(design.B * kernel.B * design.B * kernel.B)
  denom.biased$c1r1[2] <- sum(design.B * design.B)
  numer.biased$c1r1[3] <- sum(design.A * kernel.A * design.B * kernel.B)
  denom.biased$c1r1[3] <- sum(design.A * design.B)
  
  # Declare and initialize the mapping between biased and unbiased product moments
  bias2unbias <- c(
    1, -1, -1,  1,
    0,  1,  0, -1,
    0,  0,  1, -1,
    0,  0,  0,  1
  )
  dim(bias2unbias) <- c(4, 4)
  
  #### Biased Moments ####
  # Initialize the biased moments
  moments.biased <- numer.biased / denom.biased
  
  #### Unbiased Moments ####
  # Calculate the constituent parts of the unbiased variance estimate
  numer <- as.matrix(numer.biased) %*% bias2unbias
  denom <- as.matrix(denom.biased) %*% bias2unbias
  moments <- denom * 0
  index <- denom > 0
  moments[index] <- numer[index] / denom[index]
  coeff <- denom
  coeff[1, ] <- denom[1, ] / nObs.A / nObs.A
  coeff[2, ] <- denom[2, ] / nObs.B / nObs.B
  coeff[3, ] <- denom[3, ] / nObs.A / nObs.B
  
  # This last operation is equivalent to subtracting the mean squared
  coeff[, 1] <- coeff[, 1] - 1
  
  #### Summary Statistics ####
  # Estimate the variance as a scalar product between the coefficients and the moments
  var.A <- coeff[1, ] %*% moments[1, ]
  var.B <- coeff[2, ] %*% moments[2, ]
  var.AminusB <- var.A + var.B - 2 * coeff[3, ] %*% moments[3, ]
  
  # Turn these matrices into data frames
  moments <- data.frame(moments, stringsAsFactors = TRUE)
  names(moments) <- names(numer.biased)
  coeff <- data.frame(coeff, stringsAsFactors = TRUE)
  names(coeff) <- names(numer.biased)
  
  mean <- c(mean.A, mean.B, mean.A - mean.B)
  names(mean) <- desc
  var <- c(var.A, var.B, var.AminusB)
  names(var) <- desc
  var.1obs <- moments$c1r1 - moments$c0r0
  var.1obs[3] <- var.1obs[1] + var.1obs[2] - 2 * var.1obs[3]
  names(var.1obs) <- desc
  nCperR <- data.frame(sumiD.A, sumiD.B, stringsAsFactors = TRUE)
  names(nCperR) <- desc[1:2]
  
  #### Pack Results ####
  result <- list(
    mean = mean,
    var = var,
    var.1obs = var.1obs,
    meanPerR = meanPerR,
    nR = nR,
    nC = nC,
    nCperR = nCperR,
    moments = moments,
    coeff = coeff,
    desc = desc,
    kernel.A = kernel.A,
    design.A = design.A,
    kernel.B = kernel.B,
    design.b = design.B
  )
  
  return(result)
  
}

#---- uStat11.conditionalD ----
#' @title Analysis of U-statistics degree 1,1
#'
#' @export
#'
#' @rdname uStat11
uStat11.conditionalD <- function(
    df.input, modalitiesToCompare, kernelFlag = 1,
    keyColumns = c("readerID", "caseID", "modalityID", "score")
) {
  
  if (!inherits(modalitiesToCompare, "character")) {
    stop(paste(
      "class(modalitiesToCompare) =", class(modalitiesToCompare),
      "... The class should be character.")
    )
  }
  
  # Initialize kernel and design matrices ####
  if (kernelFlag == 1) {
    
    if (length(modalitiesToCompare) != 2) {
      print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
      stop("ERROR: modalitiesToCompare should have 2 elements.")
    }
    
    CbyR.identity <- uStat11.identity(
      df.input,
      keyColumns = keyColumns,
      modalitiesToCompare = modalitiesToCompare)
    kernel.A = CbyR.identity$kernel.A
    design.A = CbyR.identity$design.A
    kernel.B = CbyR.identity$kernel.B
    design.B = CbyR.identity$design.B
    
    desc <- c("A", "B", "AminusB")
    
  } else if (
    kernelFlag == 2) {
    
    if (length(modalitiesToCompare) != 4) {
      print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
      stop("ERROR: modalities to compare should have 4 elements.")
    }
    
    CbyR.diff <- uStat11.diff(
      df.input,
      keyColumns = keyColumns,
      modalitiesToCompare = modalitiesToCompare)
    kernel.A = CbyR.diff$kernel.AB
    design.A = CbyR.diff$design.AB
    kernel.B = CbyR.diff$kernel.CD
    design.B = CbyR.diff$design.CD
    
    desc <- c("AminusB", "CminusD", "AminusB.minus.CminusD")
    
  }
  
  # Determine the number of cases per reader
  sumiD.A <- colSums(design.A)
  sumiD.B <- colSums(design.B)
  
  nC <- nrow(design.A)
  nR <- ncol(design.A)
  
  # Estimate the percent correct for each reader and modality
  sumiS.A <- colSums(kernel.A)
  sumiS.B <- colSums(kernel.B)
  
  meanPerR <- data.frame(sumiS.A / sumiD.A, sumiS.B / sumiD.B, stringsAsFactors = TRUE)
  names(meanPerR) <- c(desc[1], desc[2])
  
  # Reader weights for the reader-averged point estimate
  # Weigh each reader equally
  w.A <- rep(0, nR)
  w.B <- rep(0, nR)
  w.A[sumiD.A > 0] <- 1
  w.B[sumiD.B > 0] <- 1
  w.A <- w.A / sum(w.A)
  w.B <- w.B / sum(w.B)
  # Weigh each case equally
  w.A <- sumiD.A/sum(sumiD.A)
  w.B <- sumiD.B/sum(sumiD.B)
  
  # Estimate the reader averaged percent correct
  mean.A <- sum(meanPerR[1] * w.A, na.rm = TRUE)
  mean.B <- sum(meanPerR[2] * w.B, na.rm = TRUE)
  
  #### M.c1r1 ####
  # w.c1r1.AA <- diag(sumiDD.AA)
  # w.c1r1.BB <- diag(sumiDD.BB)
  # w.c1r1.AB <- diag(sumiDD.AB)
  w.c1r1.AA <- w.A
  w.c1r1.BB <- w.B
  w.c1r1.AB <- sqrt(w.A * w.B)
  
  sumiSS.AA <- t(kernel.A) %*% kernel.A
  sumiSS.BB <- t(kernel.B) %*% kernel.B
  sumiSS.AB <- t(kernel.A) %*% kernel.B
  
  sumiDD.AA <- t(design.A) %*% design.A
  sumiDD.BB <- t(design.B) %*% design.B
  sumiDD.AB <- t(design.A) %*% design.B
  
  sumiDsumiD.AA <- sumiD.A %*% t(sumiD.A)
  sumiDsumiD.BB <- sumiD.B %*% t(sumiD.B)
  sumiDsumiD.AB <- sumiD.A %*% t(sumiD.B)
  
  numer.m.c1r1.AA <- sum(w.c1r1.AA * diag(sumiSS.AA) / diag(sumiDD.AA), na.rm = TRUE)
  numer.m.c1r1.BB <- sum(w.c1r1.BB * diag(sumiSS.BB) / diag(sumiDD.BB), na.rm = TRUE)
  numer.m.c1r1.AB <- sum(w.c1r1.AB * diag(sumiSS.AB) / diag(sumiDD.AB), na.rm = TRUE)
  
  denom.m.c1r1.AA <- sum(w.c1r1.AA)
  denom.m.c1r1.BB <- sum(w.c1r1.BB)
  denom.m.c1r1.AB <- sum(w.c1r1.AB)
  
  m.c1r1.AA <- 0
  m.c1r1.BB <- 0
  m.c1r1.AB <- 0
  if (denom.m.c1r1.AA) m.c1r1.AA <- numer.m.c1r1.AA / denom.m.c1r1.AA
  if (denom.m.c1r1.BB) m.c1r1.BB <- numer.m.c1r1.BB / denom.m.c1r1.BB
  if (denom.m.c1r1.AB) m.c1r1.AB <- numer.m.c1r1.AB / denom.m.c1r1.AB
  
  c.c1r1.AA <- sum(w.A * w.A * diag(sumiDD.AA) / diag(sumiDsumiD.AA), na.rm = TRUE)
  c.c1r1.BB <- sum(w.B * w.B * diag(sumiDD.BB) / diag(sumiDsumiD.BB), na.rm = TRUE)
  c.c1r1.AB <- sum(w.A * w.B * diag(sumiDD.AB) / diag(sumiDsumiD.AB), na.rm = TRUE)
  
  #### M.c0r1 ####
  # w.c0r1.AA <- diag(sumiDsumNOTiD.AA)
  # w.c0r1.BB <- diag(sumiDsumNOTiD.BB)
  # w.c0r1.AB <- diag(sumiDsumNOTiD.AB)
  w.c0r1.AA <- w.A
  w.c0r1.BB <- w.B
  w.c0r1.AB <- sqrt(w.A * w.B)
  
  sumi2NOTiS.A <- t(matrix(sumiS.A, nR, nC)) - kernel.A
  sumi2NOTiS.B <- t(matrix(sumiS.B, nR, nC)) - kernel.B
  
  sumi2NOTiD.A <- t(matrix(sumiD.A, nR, nC)) - design.A
  sumi2NOTiD.B <- t(matrix(sumiD.B, nR, nC)) - design.B
  
  sumi.c0r1.AA <- colSums(kernel.A * sumi2NOTiS.A / sumi2NOTiD.A, na.rm = TRUE)
  sumi.c0r1.BB <- colSums(kernel.B * sumi2NOTiS.B / sumi2NOTiD.B, na.rm = TRUE)
  sumi.c0r1.AB <- colSums(kernel.A * sumi2NOTiS.B / sumi2NOTiD.B, na.rm = TRUE)
  
  numer.m.c0r1.AA <- sum(w.c0r1.AA * sumi.c0r1.AA / sumiD.A, na.rm = TRUE)
  numer.m.c0r1.BB <- sum(w.c0r1.BB * sumi.c0r1.BB / sumiD.B, na.rm = TRUE)
  numer.m.c0r1.AB <- sum(w.c0r1.AB * sumi.c0r1.AB / sumiD.A, na.rm = TRUE)
  
  denom.m.c0r1.AA <- sum(w.c0r1.AA)
  denom.m.c0r1.BB <- sum(w.c0r1.BB)
  denom.m.c0r1.AB <- sum(w.c0r1.AB)
  
  m.c0r1.AA <- 0
  m.c0r1.BB <- 0
  m.c0r1.AB <- 0
  if (denom.m.c0r1.AA) m.c0r1.AA <- numer.m.c0r1.AA / denom.m.c0r1.AA
  if (denom.m.c0r1.BB) m.c0r1.BB <- numer.m.c0r1.BB / denom.m.c0r1.BB
  if (denom.m.c0r1.AB) m.c0r1.AB <- numer.m.c0r1.AB / denom.m.c0r1.AB
  
  c.c0r1.AA <- sum(w.A * w.A * (1 - diag(sumiDD.AA) / diag(sumiDsumiD.AA)), na.rm = TRUE)
  c.c0r1.BB <- sum(w.B * w.B * (1 - diag(sumiDD.BB) / diag(sumiDsumiD.BB)), na.rm = TRUE)
  c.c0r1.AB <- sum(w.A * w.B * (1 - diag(sumiDD.AB) / diag(sumiDsumiD.AB)), na.rm = TRUE)
  
  #### M.c1r0 ####
  # w.c1r0.AA <- sumiDD.AA
  # w.c1r0.BB <- sumiDD.BB
  # w.c1r0.AB <- sumiDD.AB
  ww.AA <- w.A %*% t(w.A)
  ww.BB <- w.B %*% t(w.B)
  ww.AB <- w.A %*% t(w.B)
  w.c1r0.AA <- ww.AA
  w.c1r0.BB <- ww.BB
  w.c1r0.AB <- ww.AB
  
  numer.m.c1r0.AA <- w.c1r0.AA * sumiSS.AA / sumiDD.AA
  numer.m.c1r0.BB <- w.c1r0.BB * sumiSS.BB / sumiDD.BB
  numer.m.c1r0.AB <- w.c1r0.AB * sumiSS.AB / sumiDD.AB
  
  numer.m.c1r0.AA <- sum(numer.m.c1r0.AA, na.rm = TRUE) - sum(diag(numer.m.c1r0.AA), na.rm = TRUE)
  numer.m.c1r0.BB <- sum(numer.m.c1r0.BB, na.rm = TRUE) - sum(diag(numer.m.c1r0.BB), na.rm = TRUE)
  numer.m.c1r0.AB <- sum(numer.m.c1r0.AB, na.rm = TRUE) - sum(diag(numer.m.c1r0.AB), na.rm = TRUE)
  
  denom.m.c1r0.AA <- sum(w.c1r0.AA) - sum(diag(w.c1r0.AA))
  denom.m.c1r0.BB <- sum(w.c1r0.BB) - sum(diag(w.c1r0.BB))
  denom.m.c1r0.AB <- sum(w.c1r0.AB) - sum(diag(w.c1r0.AB))
  
  m.c1r0.AA <- 0
  m.c1r0.BB <- 0
  m.c1r0.AB <- 0
  if (denom.m.c1r0.AA) m.c1r0.AA <- numer.m.c1r0.AA / denom.m.c1r0.AA
  if (denom.m.c1r0.BB) m.c1r0.BB <- numer.m.c1r0.BB / denom.m.c1r0.BB
  if (denom.m.c1r0.AB) m.c1r0.AB <- numer.m.c1r0.AB / denom.m.c1r0.AB
  
  c.c1r0.AA <- ww.AA * sumiDD.AA / sumiDsumiD.AA
  c.c1r0.BB <- ww.BB * sumiDD.BB / sumiDsumiD.BB
  c.c1r0.AB <- ww.AB * sumiDD.AB / sumiDsumiD.AB
  
  c.c1r0.AA <- sum(c.c1r0.AA, na.rm = TRUE) - sum(diag(c.c1r0.AA), na.rm = TRUE)
  c.c1r0.BB <- sum(c.c1r0.BB, na.rm = TRUE) - sum(diag(c.c1r0.BB), na.rm = TRUE)
  c.c1r0.AB <- sum(c.c1r0.AB, na.rm = TRUE) - sum(diag(c.c1r0.AB), na.rm = TRUE)
  
  #### M.c0r0 ####
  # w.c0r0.AA <- sumiDsumNOTiD.AA
  # w.c0r0.BB <- sumiDsumNOTiD.BB
  # w.c0r0.AB <- sumiDsumNOTiD.AB
  w.c0r0.AA <- ww.AA
  w.c0r0.BB <- ww.BB
  w.c0r0.AB <- ww.AB
  
  avgRR.c0r0.AA <- t(kernel.A / t(matrix(sumiD.A, nR, nC))) %*% (sumi2NOTiS.A / sumi2NOTiD.A)
  avgRR.c0r0.BB <- t(kernel.B / t(matrix(sumiD.B, nR, nC))) %*% (sumi2NOTiS.B / sumi2NOTiD.B)
  avgRR.c0r0.AB <- t(kernel.A / t(matrix(sumiD.A, nR, nC))) %*% (sumi2NOTiS.B / sumi2NOTiD.B)
  
  numer.m.c0r0.AA <- w.c0r0.AA * avgRR.c0r0.AA
  numer.m.c0r0.BB <- w.c0r0.BB * avgRR.c0r0.BB
  numer.m.c0r0.AB <- w.c0r0.AB * avgRR.c0r0.AB
  
  numer.m.c0r0.AA <- sum(numer.m.c0r0.AA, na.rm = TRUE) - sum(diag(numer.m.c0r0.AA), na.rm = TRUE)
  numer.m.c0r0.BB <- sum(numer.m.c0r0.BB, na.rm = TRUE) - sum(diag(numer.m.c0r0.BB), na.rm = TRUE)
  numer.m.c0r0.AB <- sum(numer.m.c0r0.AB, na.rm = TRUE) - sum(diag(numer.m.c0r0.AB), na.rm = TRUE)
  
  denom.m.c0r0.AA <- sum(w.c0r0.AA) - sum(diag(w.c0r0.AA))
  denom.m.c0r0.BB <- sum(w.c0r0.BB) - sum(diag(w.c0r0.BB))
  denom.m.c0r0.AB <- sum(w.c0r0.AB) - sum(diag(w.c0r0.AB))
  
  m.c0r0.AA <- 0
  m.c0r0.BB <- 0
  m.c0r0.AB <- 0
  if (denom.m.c0r0.AA) m.c0r0.AA <- numer.m.c0r0.AA / denom.m.c0r0.AA
  if (denom.m.c0r0.BB) m.c0r0.BB <- numer.m.c0r0.BB / denom.m.c0r0.BB
  if (denom.m.c0r0.AB) m.c0r0.AB <- numer.m.c0r0.AB / denom.m.c0r0.AB
  
  c.c0r0.AA <- ww.AA * (1 - sumiDD.AA / sumiDsumiD.AA)
  c.c0r0.BB <- ww.BB * (1 - sumiDD.BB / sumiDsumiD.BB)
  c.c0r0.AB <- ww.AB * (1 - sumiDD.AB / sumiDsumiD.AB)
  
  c.c0r0.AA <- sum(c.c0r0.AA, na.rm = TRUE) - sum(diag(c.c0r0.AA), na.rm = TRUE) - 1
  c.c0r0.BB <- sum(c.c0r0.BB, na.rm = TRUE) - sum(diag(c.c0r0.BB), na.rm = TRUE) - 1
  c.c0r0.AB <- sum(c.c0r0.AB, na.rm = TRUE) - sum(diag(c.c0r0.AB), na.rm = TRUE) - 1
  
  #### Summary stats ####
  var.A <-
    c.c1r1.AA * m.c1r1.AA +
    c.c1r0.AA * m.c1r0.AA +
    c.c0r1.AA * m.c0r1.AA +
    c.c0r0.AA * m.c0r0.AA
  var.B <-
    c.c1r1.BB * m.c1r1.BB +
    c.c1r0.BB * m.c1r0.BB +
    c.c0r1.BB * m.c0r1.BB +
    c.c0r0.BB * m.c0r0.BB
  var.AminusB <-
    c.c1r1.AB * m.c1r1.AB +
    c.c1r0.AB * m.c1r0.AB +
    c.c0r1.AB * m.c0r1.AB +
    c.c0r0.AB * m.c0r0.AB
  var.AminusB <- var.A + var.B - 2 * var.AminusB
  
  #### Pack results ####
  
  moments <- data.frame(
    c0r0 = c(m.c0r0.AA, m.c0r0.BB, m.c0r0.AB),
    c0r1 = c(m.c0r1.AA, m.c0r1.BB, m.c0r1.AB),
    c1r0 = c(m.c1r0.AA, m.c1r0.BB, m.c1r0.AB),
    c1r1 = c(m.c1r1.AA, m.c1r1.BB, m.c1r1.AB),
    stringsAsFactors = TRUE
  )
  
  coeff <- data.frame(
    c0r0 = c(c.c0r0.AA, c.c0r0.BB, c.c0r0.AB),
    c0r1 = c(c.c0r1.AA, c.c0r1.BB, c.c0r1.AB),
    c1r0 = c(c.c1r0.AA, c.c1r0.BB, c.c1r0.AB),
    c1r1 = c(c.c1r1.AA, c.c1r1.BB, c.c1r1.AB),
    stringsAsFactors = TRUE
  )
  
  mean <- c(mean.A, mean.B, mean.A - mean.B)
  names(mean) <- desc
  var <- c(var.A, var.B, var.AminusB)
  names(var) <- desc
  var.1obs <- moments$c1r1 - moments$c0r0
  var.1obs[3] <- var.1obs[1] + var.1obs[2] - 2 * var.1obs[3]
  names(var.1obs) <- desc
  nCperR <- data.frame(sumiD.A, sumiD.B, stringsAsFactors = TRUE)
  names(nCperR) <- desc[1:2]
  
  result <- list(
    mean = mean,
    var = var,
    var.1obs = var.1obs,
    meanPerR = meanPerR,
    nR = nR,
    nC = nC,
    nCperR = nCperR,
    moments = moments,
    coeff = coeff,
    desc = c(
      modalitiesToCompare,
      paste(modalitiesToCompare[1],"minus", modalitiesToCompare[2], sep = ".")),
    kernel.A = kernel.A,
    design.A = design.A,
    kernel.B = kernel.B,
    design.b = design.B
  )
  
  return(result)
  
}

#---- uStat11.identity ----
#' @title Create the kernel and design matrices for uStat11
#'
#' @description The kernel is the identity kernel
#'
#' @param df.input Data frame of observations, one per row. Columns also identify random and fixed effects.
#'
#' @param keyColumns The required columns
#' @param modalitiesToCompare The factors identifying the modalities to compare
#'
# @export
#'
uStat11.identity <- function(
    df.input, modalitiesToCompare,
    keyColumns = c("readerID", "caseID", "modalityID", "score")
) {
  
  # Structure the data frame: one row for each observation
  df <- data.frame(readerID = factor(df.input[[keyColumns[1]]]),
                   caseID = factor(df.input[[keyColumns[[2]]]]),
                   modalityID = factor(df.input[[keyColumns[[3]]]]),
                   score = df.input[[keyColumns[[4]]]],
                   stringsAsFactors = TRUE
  )
  
  # Parse out data frames for each modality
  df.A <- df[df$modalityID == modalitiesToCompare[1], ]
  df.B <- df[df$modalityID == modalitiesToCompare[2], ]
  
  # Determine readers involved in the comparisons
  readers <- levels(df$readerID)
  nReaders <- nlevels(df$readerID)
  
  # Determine readers involved in the comparisons
  cases <- levels(df$caseID)
  nCases <- nlevels(df$caseID)
  
  # Declare the reader-by-case score arrays for each modality
  scores.A <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  scores.B <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  
  # Declare the reader-by-case design arrays for each modality
  design.A <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  design.B <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  
  # Initilize the reader-by-case score and design arrays for each modality
  index <- data.matrix(df.A[ , c("caseID", "readerID")])
  scores.A[index] <- df.A$score
  design.A[index] <- 1
  index <- data.matrix(df.B[ , c("caseID", "readerID")])
  scores.B[index] <- df.B$score
  design.B[index] <- 1
  
  return(list(
    design.A = design.A,
    design.B = design.B,
    kernel.A = scores.A * design.A,
    kernel.B = scores.B * design.B
  ))
}

#---- uStat11.diff ----
#' @title Create the kernel and design matrices for uStat11
#'
#' @description The kernel is the difference kernel
#'
#' @param df.input Data frame of observations, one per row. Columns also identify random and fixed effects.
#'
#' @param keyColumns The required columns
#' @param modalitiesToCompare The factors identifying the modalities to compare
#'
uStat11.diff <- function(
    df.input, modalitiesToCompare,
    keyColumns = c("readerID", "caseID", "modalityID", "score")
) {
  
  # Structure the data frame: one row for each observation
  df <- data.frame(readerID = factor(df.input[[keyColumns[1]]]),
                   caseID = factor(df.input[[keyColumns[[2]]]]),
                   modalityID = factor(df.input[[keyColumns[[3]]]]),
                   score = df.input[[keyColumns[[4]]]],
                   stringsAsFactors = TRUE
  )
  
  # Parse out data frames for each modality
  df.A <- df[df$modalityID == modalitiesToCompare[1], ]
  df.B <- df[df$modalityID == modalitiesToCompare[2], ]
  df.C <- df[df$modalityID == modalitiesToCompare[3], ]
  df.D <- df[df$modalityID == modalitiesToCompare[4], ]
  
  # Determine readers involved in the comparisons
  readers <- levels(df$readerID)
  nReaders <- nlevels(df$readerID)
  
  # Determine cases involved in the comparisons
  cases <- levels(df$caseID)
  nCases <- nlevels(df$caseID)
  
  # Declare the reader-by-case score arrays for each modality
  scores.A <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  scores.B <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  scores.C <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  scores.D <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  
  # Declare the reader-by-case design arrays for each modality
  design.A <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  design.B <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  design.C <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  design.D <- array(0, c(nCases, nReaders), dimnames = list(cases, readers))
  
  # Initilize the reader-by-case score and design arrays for each modality
  index <- data.matrix(df.A[ , c("caseID", "readerID")])
  scores.A[index] <- df.A$score
  design.A[index] <- 1
  index <- data.matrix(df.B[ , c("caseID", "readerID")])
  scores.B[index] <- df.B$score
  design.B[index] <- 1
  index <- data.matrix(df.C[ , c("caseID", "readerID")])
  scores.C[index] <- df.C$score
  design.C[index] <- 1
  index <- data.matrix(df.D[ , c("caseID", "readerID")])
  scores.D[index] <- df.D$score
  design.D[index] <- 1
  
  # Declare and initialize the kernel results
  design.AB <- design.A * design.B
  design.CD <- design.C * design.D
  kernel.AB <- (scores.A - scores.B) * design.AB
  kernel.CD <- (scores.C - scores.D) * design.CD
  
  return(list(
    kernel.AB = kernel.AB,
    design.AB = design.AB,
    kernel.CD = kernel.CD,
    design.CD = design.CD
  ))
  
}