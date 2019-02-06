# Initialize the l'Ecuyer random number generator ####
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

# Simulate an MRMC data set ####
#' Simulate an MRMC data set ####
#'
#' @description
#' This program simulates observations from one set of readers scoring one set of cases.
#' It produces one modality and one truth state
#' of ROC data following Roe1997_Acad-Radiol_v4p298 and Roe1997_Acad-Radiol_v4p587.
#' In order to produce an entire ROC data set, please use sim.gRoeMetz.
#'
#' @details
#' The simulation is a linear model with one fixed effect and three
#' normally distributed independent random effects corresponding to readers,
#' cases, and an interaction between the two.
#'
#' L.rc = mu + readerEffect.r + caseEffect.c + readerXcaseEffect.rc
#'
#' @param simMRMC.config [list] of simulation parameters:
#' \itemize{
#'   \item modalityID [chr] label modalityID
#'   \item readerIDs  [factor] the ID of each reader
#'   \item caseIDs    [factor] the ID of each case
#'   \item mu         [num] mean
#'   \item var_r      [num] variance of random reader effect
#'   \item var_c      [num] variance of random case effect
#'   \item var_rc     [num] variance of random reader by case effect
#' }
#'
#' @return  L [data.frame] with nC*nR rows of 4 variables
#' \itemize{
#'   \item L$modalityID   [factor] determined by input modalityID
#'   \item L$readerID     [factor] determined by input readerIDs
#'   \item L$caseID       [factor] determined by input caseIDs
#'   \item L$score        [num]  R.r + C.c + RC.rc
#'   \itemize{
#'     \item r = 1,2,...,nR
#'     \item c = 1,2,...,nC
#'     \item R.r ~ N(0,var_r)
#'     \item C.c ~ N(0,var_c)
#'     \item RC.rc ~ N(0,var_rc)
#'   }
#' }
#'
#' @export
#'
#' @examples
#' # Create a sample configuration object
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)

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
  L_cases <- stats::rnorm(nC, mean = 0, sd = sqrt(var_c))
  # Replicate random case effects for each reader
  L_cases <- rep(L_cases, nR)
  caseID <- rep(caseIDs, nR)

  # Simulate reader effects
  L_readers <- stats::rnorm(nR, mean = 0, sd = sqrt(var_r))
  # Replicate random reader effects for each case
  # rep(nC, nR) indicates that each of the nR elements of readers
  #   is to be replicated nC times.
  L_readers <- rep(L_readers, rep(nC, nR))
  readerID <- rep(readerIDs, rep(nC, nR))

  # Simulate reader by case interaction
  L_reader.case <- stats::rnorm(nC*nR, mean = 0, sd = sqrt(var_rc))
  modalityID <- rep(modalityID, nC*nR)

  # Put it all together
  L <- data.frame(
    readerID = readerID,
    caseID = caseID,
    modalityID = modalityID,
    score = L_mu + L_cases + L_readers + L_reader.case
  )

}

# Simulate an MRMC data set of an ROC experiment comparing two modalities ####
#' Simulate an MRMC data set of an ROC experiment comparing two modalities
#'
#' @description
#' This procedure simulates an MRMC data set of an ROC experiment comparing two modalities.
#' It is based on Gallas2014_J-Med-Img_v1p031006, which generalizes of the model in
#' Roe1997_Acad-Radiol_v4p298 and Roe1997_Acad-Radiol_v4p587. Specifically, it allows
#' the variance components to depend on the truth and the modality. For the simpler
#' Roe and Metz model, you can enter the smaller set of parameters into
#' sim.gRoeMetz.config and get back the larger set of parameters and then
#' used with this function.
#'
#' @details
#' The simulation is a linear model with six fixed effects related to
#' modality and truth and 18 normally distributed independent random effects
#' for readers, cases, and the interaction between the two. Here is the linear model:
#'
#' L.mrct = mu.t + mu.mt \cr
#'        + reader.rt + case.ct + readerXcase.rct \cr
#'        + modalityXreader.mrt + modalityXcase.mct + modalityXreaderXcase.mrct \cr
#' \itemize{
#'   \item m=modality (levels: A and b)
#'   \item t=truth (levels: neg and Pos)
#'   \item mu.t is the global mean for t=neg and t=pos cases
#'   \item mu.mt is the modality specific fixed effects for t=neg and t=pos cases
#'   \item the remaining terms are the random effects: all independent normal random variables
#' }
#'
#' @param config  [list] of simulation parameters:
#' \itemize{
#'   \item Experiment labels and size
#'   \itemize{
#'     \item modalityID.A: [chr] label modality A
#'     \item modalityID.B: [chr] label modality B
#'     \item nR: [num] number of readers
#'     \item nC.neg: [num] number of signal-absent cases
#'     \item nC.pos: [num] number of signal-present cases
#'   }
#'   \item There are six fixed effects:
#'   \itemize{
#'     \item mu.neg: [num] signal-absent (neg, global mean)
#'     \item mu.pos: [num] signal-present (pos, global mean)
#'     \item mu.Aneg: [num] modality A signal-absent (Aneg, modality effect)
#'     \item mu.Bneg: [num] modality B signal-absent (Bneg, modality effect)
#'     \item mu.Apos: [num] modality A signal-present (Apos, modality effect)
#'     \item mu.Bpos: [num] modality B signal-present (Bpos, modality effect)
#'   }
#'   \item There are six random effects that are independent of modality
#'   \itemize{
#'     \item var_r.neg: [num] variance of random reader effect
#'     \item var_c.neg: [num] variance of random case effect
#'     \item var_rc.neg: [num] variance of random reader by case effect
#'     \item var_r.pos: [num] variance of random reader effect
#'     \item var_c.pos: [num] variance of random case effect
#'     \item var_rc.pos: [num] variance of random reader by case effect
#'   }
#'   \item There are six random effects that are specific to modality A
#'     \itemize{
#'       \item var_r.Aneg: [num] variance of random reader effect
#'       \item var_c.Aneg: [num] variance of random case effect
#'       \item var_rc.Aneg: [num] variance of random reader by case effect
#'       \item var_r.Apos: [num] variance of random reader effect
#'       \item var_c.Apos: [num] variance of random case effect
#'       \item var_rc.Apos: [num] variance of randome reader by case effect
#'   }
#'   \item There are six random effects that are specific to modality B
#'     \itemize{
#'       \item var_r.Bneg: [num] variance of random reader effect
#'       \item var_c.Bneg: [num] variance of random case effect
#'       \item var_rc.Bneg: [num] variance of random reader by case effect
#'       \item var_r.Bpos: [num] variance of random reader effect
#'       \item var_c.Bpos: [num] variance of random case effect
#'       \item var_rc.Bpos: [num] variance of randome reader by case effect
#'   }
#' }
#'
#' @return  dFrame.imrmc   [data.frame] with (nC.neg + nC.pos)*(nR+1) rows including
#' \itemize{
#'   \item readerID: [Factor] w/ nR levels "reader1", "reader2", ...
#'   \item caseID: [Factor] w/ nC levels "case1", "case2", ...
#'   \item modalityID: [Factor] w/ 1 level config$modalityID
#'   \item score: [num] reader score
#' }
#'
#' Note that the first nC.neg + nC.pos rows specify the truth labels for each case.
#' For these rows, the readerID must be "truth" or "-1"
#' and the score must be 0 for negative cases and 1 for postive cases.
#'
#' @export
#'
#' @examples
#' # Create a sample configuration object
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)
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

# Create a configuration object for the sim.gRoeMetz program ####
#' Create a configuration object for the sim.gRoeMetz program
#'
#' @description
#' This function creates a configuration object for the Roe & Metz
#' simulation model to be used as input for the sim.gRoeMetz program.
#' The default model returned when there are no arguments given to the function is
#' the "HH" model from Roe1987_Acad-Radiol_v4p298. Following that paper,
#' The user can specify three parameters related to experiment size (nR, nC.neg, nC.pos)
#' and five parameters parameters specifying a linear model that does not
#' depend on modality or truth (mu.neg, mu.pos, var_r, var_c, var_rc).
#'
#' @details If no arguments, this function returns a default simulation
#' configuration for sim.gRoeMetz
#'
#' @param nR Number of readers (default = 5)
#' @param nC.neg Number of signal-absent cases (default = 25)
#' @param nC.pos Number of signal-present cases (default = 25)
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
#' @return config [list] Refer to the sim.gRoeMetz input variable
#'
#' @export
#'
#' @examples
#' # Create a sample configuration object
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)
sim.gRoeMetz.config <- function(
  nR = 5,
  nC.neg = 40,
  nC.pos = 40,
  mu.neg = 0.0,
  mu.pos = 1.0,
  var_r =  0.03,
  var_c =  0.30,
  var_rc = 0.20
) {

  config <- list(
    # Modality labels
    modalityID.A = "testA",
    modalityID.B = "testB",

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

# Simulates a sample MRMC ROC experiment ####
#' Simulates a sample MRMC ROC experiment
#'
#' @return dFrame.imrmc [data.frame] Please refer to the description of the simRoeMetz return variable
#' @export
#'
#' @examples
#' # Simulate a sample MRMC ROC data set
#' dFrame.imrmc <- simRoeMetz.example()
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dFrame.imrmc)
#'
simRoeMetz.example <- function() {

  config <- sim.gRoeMetz.config()

  # Simulate data
  dFrame.imrmc <- sim.gRoeMetz(config)

  tempA <- dFrame.imrmc[dFrame.imrmc$modalityID == "testA", ]
  tempApos <- tempA[grep("pos", tempA$caseID), ]
  tempAneg <- tempA[grep("neg", tempA$caseID), ]
  tempB <- dFrame.imrmc[dFrame.imrmc$modalityID == "testB", ]
  tempBpos <- tempB[grep("pos", tempA$caseID), ]
  tempBneg <- tempB[grep("neg", tempA$caseID), ]

  cat("test A pos: mean, var = ", mean(tempApos$score), ",", stats::var(tempApos$score), "\n")
  cat("test B pos: mean, var = ", mean(tempBpos$score), ",", stats::var(tempBpos$score), "\n")
  cat("test A neg: mean, var = ", mean(tempAneg$score), ",", stats::var(tempAneg$score), "\n")
  cat("test B neg: mean, var = ", mean(tempBneg$score), ",", stats::var(tempBneg$score), "\n")

  return(dFrame.imrmc)

}

## roeMetzConfigs ####
#' @title roeMetzConfigs
#' 
#' @name roeMetzConfigs
#' 
#' @description This is a data frame containing the configuration parameters 
#' used in Roe1997_Acad-Radiol_v4p298. Each row corresponds to one of the twelve configurations
#' appearing in Table 1 of that paper in a format that can be the input to \code{sim.gRoeMetz}.
#' 
#' @details The columns of this data frame are as follows
#' \itemize{
#'   \item Experiment labels and size
#'   \itemize{
#'     \item modalityID.A: [chr] label modality A
#'     \item modalityID.B: [chr] label modality B
#'     \item nR: [num] number of readers
#'     \item nC.neg: [num] number of signal-absent cases
#'     \item nC.pos: [num] number of signal-present cases
#'   }
#'   \item There are six fixed effects:
#'   \itemize{
#'     \item mu.neg: [num] signal-absent (neg, global mean)
#'     \item mu.pos: [num] signal-present (pos, global mean)
#'     \item mu.Aneg: [num] modality A signal-absent (Aneg, modality effect)
#'     \item mu.Bneg: [num] modality B signal-absent (Bneg, modality effect)
#'     \item mu.Apos: [num] modality A signal-present (Apos, modality effect)
#'     \item mu.Bpos: [num] modality B signal-present (Bpos, modality effect)
#'   }
#'   \item There are six random effects that are independent of modality
#'   \itemize{
#'     \item var_r.neg: [num] variance of random reader effect
#'     \item var_c.neg: [num] variance of random case effect
#'     \item var_rc.neg: [num] variance of random reader by case effect
#'     \item var_r.pos: [num] variance of random reader effect
#'     \item var_c.pos: [num] variance of random case effect
#'     \item var_rc.pos: [num] variance of random reader by case effect
#'   }
#'   \item There are six random effects that are specific to modality A
#'     \itemize{
#'       \item var_r.Aneg: [num] variance of random reader effect
#'       \item var_c.Aneg: [num] variance of random case effect
#'       \item var_rc.Aneg: [num] variance of random reader by case effect
#'       \item var_r.Apos: [num] variance of random reader effect
#'       \item var_c.Apos: [num] variance of random case effect
#'       \item var_rc.Apos: [num] variance of randome reader by case effect
#'   }
#'   \item There are six random effects that are specific to modality B
#'     \itemize{
#'       \item var_r.Bneg: [num] variance of random reader effect
#'       \item var_c.Bneg: [num] variance of random case effect
#'       \item var_rc.Bneg: [num] variance of random reader by case effect
#'       \item var_r.Bpos: [num] variance of random reader effect
#'       \item var_c.Bpos: [num] variance of random case effect
#'       \item var_rc.Bpos: [num] variance of randome reader by case effect
#'   }
#' }
#' 
#' @examples 
#' # Extract one configuration
#' config <- roeMetzConfigs[1, ]
#' # Create an MRMC ROC data set
#' df.iMRMC <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data set
#' result <- doIMRMC(df.iMRMC)
NULL

