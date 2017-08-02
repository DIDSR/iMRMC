library(parallel)
library(LaplacesDemon)

#' @title Initialize the l'Ecuyer random number generator
#'
#' @description See the documentation for the parallel package
#'
#' @param seed This determines the position in each stream
#' @param stream This determines the stream
#'
#' @return Nothing
#'
#' @import parallel
#'
#' @export
#'
# @examples
init.lecuyerRNG <- function(seed = 1, stream = 2){

  # Set the random number generator
  RNGkind("L'Ecuyer-CMRG")

  # Set the seed
  set.seed(seed)

  # Set the streamID
  # Skip to the stream corresponding to the taskID
  # Each taskID will have its own stream for parallel implementation
  # IMPORTANT: .Random.seed must be reassigned in the global environment "<<-" not "<-"
  i <- 1
  while (i < stream) {
    .Random.seed <<- nextRNGStream(.Random.seed)
    i <- i + 1
  }

}

#' simMRMC Simulate an MRMC data set (Roe1997_Acad-Radiol_v4p298, Roe1997_Acad-Radiol_v4p587)
#'   linear model with independent random effects that are normally distributed
#'   L.rc = mu + readerEffect.r + caseEffect.c + readerXcaseEffect.rc
#'
#' @param simMRMC.config [list] of simulation parameters:
#'          modalityID [chr] label modalityID
#'          readerIDs  [factor] the ID of each reader
#'          caseIDs    [factor] the ID of each case
#'          mu         [num] mean
#'          var_r      [num] variance of random reader effect
#'          var_c      [num] variance of random case effect
#'          var_rc     [num] variance of random reader by case effect
#'
#' @return  L [data.frame] with nC*nR rows of 4 variables
#'            L$modalityID   [factor] determined by input modalityID
#'            L$readerID     [factor] determined by input readerIDs
#'            L$caseID       [factor] determined by input caseIDs
#'            L$score        [num]  R.r + C.c + RC.rc
#'                             r = 1,2,...,nR
#'                             c = 1,2,...,nC
#'                             R.r ~ N(0,var_r)
#'                             C.c ~ N(0,var_c)
#'                             RC.rc ~ N(0,var_rc)
#'
#' @export
#'
# @examples
simMRMC <- function(simMRMC.config) {

  modalityID <- simMRMC.config$modalityID
  readerIDs <- simMRMC.config$readerIDs
  caseIDs <- simMRMC.config$caseIDs
  mu <- simMRMC.config$mu
  var_c <- simMRMC.config$var_c
  var_r <- simMRMC.config$var_r
  var_rc <- simMRMC.config$var_rc

  nR <- length(readerIDs)
  nC <- length(caseIDs)

  # Initialize data array with mu (fixed effect: grand mean)
  L_mu <- rep(mu, nC * nR)

  # Simulate case effects
  L_cases <- rnorm(nC, mean = 0, sd = sqrt(var_c))
  # Replicate random case effects for each reader
  L_cases <- rep(L_cases, nR)
  caseID <- rep(caseIDs, nR)

  # Simulate reader effects
  L_readers <- rnorm(nR, mean = 0, sd = sqrt(var_r))
  # Replicate random reader effects for each case
  # rep(nC, nR) indicates that each of the nR elements of readers
  #   is to be replicated nC times.
  L_readers <- rep(L_readers, rep(nC, nR))
  readerID <- rep(readerIDs, rep(nC, nR))

  # Simulate reader by case interaction
  L_reader.case <- rnorm(nC*nR, mean = 0, sd = sqrt(var_rc))
  modalityID <- rep(modalityID, nC*nR)

  # Put it all together
  L <- data.frame(
    readerID = readerID,
    caseID = caseID,
    modalityID = modalityID,
    score = L_mu + L_cases + L_readers + L_reader.case
  )

}

#' sim.ModelA: Simulate binary data according to model A in Yuvika Paliwal's dissertation
#'
#' @description This function first simulates an MRMC data set according to a
#' linear model: y_rc = mu + R.r + C.c + RC.rc,
#' where mu is a fixed and R_r, C_c, RC_rc are random effects
#' (Gaussian with mean zero and variances var_r, var_c, var_rc).
#' The data from the linear model is transformed by the inverse logit.
#' The result is understood to be the probabilities of a Bernoulli random
#' variable. Finally, Bernoulli random variables are generated
#' for each probability.
#'
#' @param config [list] of simulation parameters:
#'          modalityID [chr] label modalityID
#'          readerIDs  [factor vector] the ID of each reader
#'          caseIDs    [factor vector] the ID of each case
#'          mu         [num] mean
#'          var_r      [num] variance of random reader effect
#'          var_c      [num] variance of random case effect
#'          var_rc     [num] variance of random reader by case effect
#'
#'
#' @return [data.frame] with nC*nR rows of 6 variables
#'            L$modalityID   [factor] determined by input modalityID
#'            L$readerID     [factor] determined by input readerIDs
#'            L$caseID       [factor] determined by input caseIDs
#'            L$score        [num]  R.r + C.c + RC.rc
#'                             r = 1,2,...,nR
#'                             c = 1,2,...,nC
#'                             R.r ~ N(0,var_r)
#'                             C.c ~ N(0,var_c)
#'                             RC.rc ~ N(0,var_rc)
#'            L$prob         [num] the probability of success for each observation
#'                             equals invlogit(L$score)
#'            L$success      [0 or 1] the success outcome for each observation
#'
#' @export
#'
# @examples
sim.ModelA <- function(config) {

  df.MRMC <- simMRMC(config)
  df.MRMC$prob <- invlogit(df.MRMC$score)
  df.MRMC$success <- rbern(rep(1, nrow(df.MRMC)), df.MRMC$prob)

  return(df.MRMC)
}

#' Convert an MRMC data frame to a score matrix, dropping readers or cases with no observations
#'
#' @description Convert an MRMC data frame to a score matrix, dropping readers or cases with no observations
#'
#' @param dfMRMC An MRMC data frame
#'
#' @return scores
#' @export
#'
convertDFtoScoreMatrix <- function(dfMRMC) {

  dfMRMC <- droplevels(dfMRMC)

  caseIDs <- levels(dfMRMC$caseID)
  readerIDs <- levels(dfMRMC$readerID)
  nCases <- nlevels(dfMRMC$caseID)
  nReaders <- nlevels(dfMRMC$readerID)

  scores <- array(-1, c(nCases, nReaders), dimnames = list(caseIDs, readerIDs))

  index <- dfMRMC[ , c("caseID","readerID")]
  index <- data.matrix(index)

  scores[index] <- dfMRMC$score
  return(scores)

}

#' Convert an MRMC data frame to a design matrix, dropping readers or cases with no observations
#'
#' @description Convert an MRMC data frame to a design matrix, dropping readers or cases with no observations
#'
#' @param dfMRMC An MRMC data frame
#'
#' @return design
#' @export
#'
convertDFtoDesignMatrix <- function(dfMRMC) {

  dfMRMC <- droplevels(dfMRMC)

  caseIDs <- levels(dfMRMC$caseID)
  readerIDs <- levels(dfMRMC$readerID)
  nCases <- nlevels(dfMRMC$caseID)
  nReaders <- nlevels(dfMRMC$readerID)

  design <- array(0, c(nCases, nReaders), dimnames = list(caseIDs, readerIDs))

  index <- dfMRMC[ , c("caseID","readerID")]
  index <- data.matrix(index)

  design[index] <- 1

  return(design)

}

#' simRoeMetz Simulate an MRMC data set for an ROC experiment comparing two modalities
#'   references: Roe1997_Acad-Radiol_v4p298, Roe1997_Acad-Radiol_v4p587
#'   references: Gallas2014_J-Med-Img_v1p031006
#'
#'   linear model
#'   L.mrct = mu.t + mu.mt + reader.rt + case.ct + readerXcase.rct
#'                  + modalityXreader.mrt + modalityXcase.mct + modalityXreaderXcase.mrct
#'     mu.t treats is the global fixed
#'     mu.mt treats the modality-specific fixed effects
#'       m=modality (levels: A and b)
#'       t=truth (levels: neg and Pos)
#'     the remaining terms are the random effects: all independent normal random variables
#'
#' @param config  [list] of simulation parameters:
#'          modalityID.A     [chr] label modality A
#'          nR               [num] number of readers
#'          nC.neg            [num] number of signal-absent cases
#'          nC.pos            [num] number of signal-present cases
#'
#'          The model parameters include for six fixed effects:
#'            mu.neg = signal-absent (neg, global mean)
#'            mu.pos = signal-present (pos, global mean)
#'            mu.Aneg = modality A signal-absent (Aneg, modality effect)
#'            mu.Bneg = modality B signal-absent (Bneg, modality effect)
#'            mu.Apos = modality A signal-present (Apos, modality effect)
#'            mu.Bpos = modality B signal-present (Bpos, modality effect)
#'
#'          The model parameters also include 18 random effects.
#'          Six that are independent of modality
#'            var_r.neg         [num] variance of random reader effect
#'            var_c.neg         [num] variance of random case effect
#'            var_rc.neg        [num] variance of randome reader by case effect
#'            var_r.pos         [num] variance of random reader effect
#'            var_c.pos         [num] variance of random case effect
#'            var_rc.pos        [num] variance of randome reader by case effect
#'          Six that are specific to modality A
#'            var_r.Aneg         [num] variance of random reader effect
#'            var_c.Aneg         [num] variance of random case effect
#'            var_rc.Aneg        [num] variance of randome reader by case effect
#'            var_r.Apos         [num] variance of random reader effect
#'            var_c.Apos         [num] variance of random case effect
#'            var_rc.Apos        [num] variance of randome reader by case effect
#'          Six that are specific to modality B
#'            var_r.Bneg         [num] variance of random reader effect
#'            var_c.Bneg         [num] variance of random case effect
#'            var_rc.Bneg        [num] variance of randome reader by case effect
#'            var_r.Bpos         [num] variance of random reader effect
#'            var_c.Bpos         [num] variance of random case effect
#'            var_rc.Bpos        [num] variance of randome reader by case effect
#'
#' @return  dFrame.imrmc   [data.frame] with (nC.neg + nC.pos)*nR rows including
#'            readerID       [Factor] w/ nR levels "reader1", "reader2", ...
#'            caseID         [Factor] w/ nC levels "case1", "case2", ...
#'            modalityID     [Factor] w/ 1 level config$modalityID
#'            score          [num] reader score
#'
#' @export
#'
#' @examples
#' config <- sim.gRoeMetz.config()
#' simRoeMetz.imrmc <- sim.gRoeMetz(config)
#'
sim.gRoeMetz <- function(config) {

  # Unpack modality labels
  modalityID.A <- config$modalityID.A
  modalityID.B <- config$modalityID.B

  # Unpack experiment size
  nR <- config$nR
  nC.neg <- config$nC.neg
  nC.pos <- config$nC.pos

  # Assign readerIDs
  readerIDs <- paste("reader", 1:nR, sep = "")
  readerIDs <- factor(readerIDs, readerIDs, ordered = TRUE)

  # Assign caseIDs
  caseIDs.neg <- paste("negCase", 1:nC.neg, sep = "")
  caseIDs.neg <- factor(caseIDs.neg, caseIDs.neg, ordered = TRUE)
  caseIDs.pos <- paste("posCase", 1:nC.pos, sep = "")
  caseIDs.pos <- factor(caseIDs.pos, caseIDs.pos, ordered = TRUE)
  caseIDs <- c(as.character(caseIDs.neg), as.character(caseIDs.pos))
  caseIDs <- factor(caseIDs, caseIDs, ordered = TRUE)

  # Create data frame of truth
  dFrame.truth <- data.frame(
    readerID = rep("-1", nC.neg + nC.pos),
    caseID = c(as.character(caseIDs.neg), as.character(caseIDs.pos)),
    modalityID = rep("truth", nC.neg + nC.pos),
    score = c(rep(0, nC.neg), rep(1, nC.pos))
  )

  # Simulate the modality independent random effects, negative cases
  neg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = "empty",
    readerIDs = readerIDs,
    caseIDs = caseIDs.neg,
    mu = config$mu.neg,
    var_c = config$var_c.neg,
    var_r = config$var_r.neg,
    var_rc = config$var_rc.neg
  )
  dFrame.neg <- simMRMC(neg.config)

  # Simulate the modality independent random effects, positive cases
  pos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = "empty",
    readerIDs = readerIDs,
    caseIDs = caseIDs.pos,
    mu = config$mu.pos,
    var_r = config$var_r.pos,
    var_c = config$var_c.pos,
    var_rc = config$var_rc.pos
  )
  dFrame.pos <- simMRMC(pos.config)

  # Simulate modality A random effects, negative cases
  modAneg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = modalityID.A,
    readerIDs = readerIDs,
    caseIDs = caseIDs.neg,
    mu = config$mu.Aneg,
    var_r = config$var_r.Aneg,
    var_c = config$var_c.Aneg,
    var_rc = config$var_rc.Aneg
  )
  dFrame.modAneg <- simMRMC(modAneg.config)

  # Simulate modality A random effects, positive cases
  modApos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = modalityID.A,
    readerIDs = readerIDs,
    caseIDs = caseIDs.pos,
    mu = config$mu.Apos,
    var_r = config$var_r.Apos,
    var_c = config$var_c.Apos,
    var_rc = config$var_rc.Apos
  )
  dFrame.modApos <- simMRMC(modApos.config)

  # Simulate modality B random effects, negative cases
  modBneg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = modalityID.B,
    readerIDs = readerIDs,
    caseIDs = caseIDs.neg,
    mu = config$mu.Bneg,
    var_r = config$var_r.Bneg,
    var_c = config$var_c.Bneg,
    var_rc = config$var_rc.Bneg
  )
  dFrame.modBneg <- simMRMC(modBneg.config)

  # Simulate modality B random effects, positive cases
  modBpos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = modalityID.B,
    readerIDs = readerIDs,
    caseIDs = caseIDs.pos,
    mu = config$mu.Bpos,
    var_r = config$var_r.Bpos,
    var_c = config$var_c.Bpos,
    var_rc = config$var_rc.Bpos
  )
  dFrame.modBpos <- simMRMC(modBpos.config)

  dFrame.modAneg$score <- dFrame.neg$score + dFrame.modAneg$score
  dFrame.modApos$score <- dFrame.pos$score + dFrame.modApos$score
  dFrame.modBneg$score <- dFrame.neg$score + dFrame.modBneg$score
  dFrame.modBpos$score <- dFrame.pos$score + dFrame.modBpos$score

  dFrame.imrmc <- rbind(
    dFrame.truth,
    dFrame.modAneg,
    dFrame.modBneg,
    dFrame.modApos,
    dFrame.modBpos
  )

  dFrame.imrmc$caseID <- factor(dFrame.imrmc$caseID, as.character(caseIDs), ordered = TRUE)

  return(dFrame.imrmc)

}

#' sim.gRoeMetz.config creates a configuration file for the sim.gRoeMetz program
#'
#' @description
#' This function creates a configuration file for the generalized Roe & Metz
#' simulation model as described in Gallas2014_J-Med-Img_v1p031006.
#' The default model returned when there are no arguments given to the function is
#' the HH model from Roe1887_Acad-Radiol_v4p298. Following that paper,
#' The user can also specify three parameters related to experiment size (nR, nC.neg, nC.pos)
#' and five parameters parameters specifying a linear model that do not
#' depend on modality or truth (mu.neg, mu.pos, var_r, var_c, var_rc).
#'
#' @details If no arguements, this function returns a default simulation configuration for sim.gRoeMetz
#'
#' @param nR Number of readers (default = 10)
#' @param nC.neg Number of signal-absent cases (default = 100)
#' @param nC.pos Number of signal-present cases (default = 100)
#' @param mu.neg Mean fixed effect of signal-absent distribution (default = 0.0) \cr
#'               Modality specific parameters are set to zero: mu.Aneg = mu.Bneg = 0
#' @param mu.pos Mean fixed effect of signal-present distribution (default = 1.0) \cr
#'               Modality specific parameters are set to zero: mu.Apos = mu.Bpos = 0
#' @param var_r Variance of reader random effect (default = 0.03) \cr
#'              var_r.neg = var_r.pos = var_r.Aneg = var_r.Apos = var_r.Bneg = var_r.Bpos = var_r \cr
#' @param var_c Variance of case random effect (default = 0.30) \cr
#'              var_c.neg = var_c.pos = var_c.Aneg = var_c.Apos = var_c.Bneg = var_c.Bpos = var_c \cr
#' @param var_rc Variance of reader.by.case random effect (default = 0.20) \cr
#'              var_rc.neg = var_rc.pos = var_rc.Aneg = var_rc.Apos = var_rc.Bneg = var_rc.Bpos = var_rc \cr
#'
#' @return config [list] Refer to the simRoeMetz input variable
#'
#' @export
#'
#' @examples
#' config <- sim.gRoeMetz.config()
sim.gRoeMetz.config <- function(
  nR = 10,
  nC.neg = 100,
  nC.pos = 100,
  mu.neg = 0.0,
  mu.pos = 1.0,
  var_r =  0.03,
  var_c =  0.30,
  var_rc = 0.20
) {

  config <- list(
    # Modality labels
    modalityID.A = "modalityA",
    modalityID.B = "modalityB",

    # Experiment size
    nR = nR,
    nC.neg = nC.neg,
    nC.pos = nC.pos,

    # Model parameters invariant to modality
    # Signal-absent global mean
    mu.neg = mu.neg,
    # Signal-absent variance components
    var_r.neg =  var_r,
    var_c.neg =  var_c,
    var_rc.neg = var_rc,
    # Signal-present global mean
    mu.pos = mu.pos,
    # Signal-present variance components
    var_r.pos =  var_r,
    var_c.pos =  var_c,
    var_rc.pos = var_rc,

    # Model parameters for modality A
    # Signal-present modality effect
    mu.Aneg = 0,
    # Signal-absent variance components
    var_r.Aneg =  var_r,
    var_c.Aneg =  var_c,
    var_rc.Aneg = var_rc,
    # Signal-absent modality effect
    mu.Apos = 0,
    # Signal-present variance components
    var_r.Apos =  var_r,
    var_c.Apos =  var_c,
    var_rc.Apos = var_rc,

    # Model parameters for modality B
    # Signal-present modality effect
    mu.Bneg = 0,
    # Signal-absent variance components
    var_r.Bneg =  var_r,
    var_c.Bneg =  var_c,
    var_rc.Bneg = var_rc,
    # Signal-absent modality effect
    mu.Bpos = 0,
    # Signal-present variance components
    var_r.Bpos =  var_r,
    var_c.Bpos =  var_c,
    var_rc.Bpos = var_rc
  )

  return(config)

}

#' simRoeMetz.example simulates an MRMC ROC experiment
#'
#' @return dFrame.imrmc [data.frame] Please refer to the description of the simRoeMetz return variable
#' @export
#'
#' @examples
#' simRoeMetz.imrmc <- simRoeMetz.example()
#'
simRoeMetz.example <- function() {

  config <- sim.gRoeMetz.config()

  # Simulate data
  dFrame.imrmc <- sim.gRoeMetz(config)

  tempA <- dFrame.imrmc[dFrame.imrmc$modalityID == "modalityA", ]
  tempApos <- tempA[grep("pos", tempA$caseID), ]
  tempAneg <- tempA[grep("neg", tempA$caseID), ]
  tempB <- dFrame.imrmc[dFrame.imrmc$modalityID == "modalityB", ]
  tempBpos <- tempB[grep("pos", tempA$caseID), ]
  tempBneg <- tempB[grep("neg", tempA$caseID), ]

  cat("modality A pos: mean, var = ", mean(tempApos$score), ",", var(tempApos$score), "\n")
  cat("modality B pos: mean, var = ", mean(tempBpos$score), ",", var(tempBpos$score), "\n")
  cat("modality A neg: mean, var = ", mean(tempAneg$score), ",", var(tempAneg$score), "\n")
  cat("modality B neg: mean, var = ", mean(tempBneg$score), ",", var(tempBneg$score), "\n")

  return(dFrame.imrmc)

}
