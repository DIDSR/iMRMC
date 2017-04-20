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
#' @param simRoeMetz.config  [list] of simulation parameters:
#'          modalityID.A     [chr] label modality A
#'          nR               [num] number of readers
#'          nC.neg            [num] number of signal-absent cases
#'          nC.pos            [num] number of signal-present cases
#'
#'          There are also model parameters for six random effects:
#'            signal-absent (neg, global mean)
#'            signal-present (pos, global mean)
#'            modality A signal-absent (Aneg, modality effect)
#'            modality B signal-absent (Bneg, modality effect)
#'            modality A signal-present (Apos, modality effect)
#'            modality B signal-present (Bpos, modality effect)
#'
#'          Each random effect carries its own model parameters
#'          Here are the parameters for the signal-absent cases (not modality specific part)
#'            mu.neg            [num] mean
#'            var_r.neg         [num] variance of random reader effect
#'            var_c.neg         [num] variance of random case effect
#'            var_rc.neg        [num] variance of randome reader by case effect
#'
#' @return  dFrame.imrmc   [data.frame] with (nC.neg + nC.pos)*nR rows including
#'            readerID       [Factor] w/ nR levels "reader1", "reader2", ...
#'            caseID         [Factor] w/ nC levels "case1", "case2", ...
#'            modalityID     [Factor] w/ 1 level simRoeMetz.config$modalityID
#'            score          [num] reader score
#'
#' @export
#'
# @examples
simRoeMetz <- function(simRoeMetz.config) {

  # Unpack modality labels
  modalityID.A <- simRoeMetz.config$modalityID.A
  modalityID.B <- simRoeMetz.config$modalityID.B

  # Unpack experiment size
  nR <- simRoeMetz.config$nR
  nC.neg <- simRoeMetz.config$nC.neg
  nC.pos <- simRoeMetz.config$nC.pos

  # Assign readerIDs
  readerIDs <- factor(paste("reader", 1:nR, sep = ""))

  # Assign caseIDs
  negCaseIDs <- factor(paste("negCase", 1:nC.neg, sep = ""))
  posCaseIDs <- factor(paste("posCase", 1:nC.pos, sep = ""))
  # Create data frame of truth
  dFrame.truth <- data.frame(
    readerID = rep("-1", nC.neg + nC.pos),
    caseID = c(as.character(negCaseIDs), as.character(posCaseIDs)),
    modalityID = rep("truth", nC.neg + nC.pos),
    score = c(rep(0, nC.neg), rep(1, nC.pos))
  )

  # Simulate the modality independent random effects, negative cases
  neg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = "empty",
    readerIDs = readerIDs,
    caseIDs = negCaseIDs,
    mu = simRoeMetz.config$mu.neg,
    var_c = simRoeMetz.config$var_c.neg,
    var_r = simRoeMetz.config$var_r.neg,
    var_rc = simRoeMetz.config$var_rc.neg
  )
  dFrame.neg <- simMRMC(neg.config)

  # Simulate the modality independent random effects, positive cases
  pos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = "empty",
    readerIDs = readerIDs,
    caseIDs = posCaseIDs,
    mu = simRoeMetz.config$mu.pos,
    var_r = simRoeMetz.config$var_r.pos,
    var_c = simRoeMetz.config$var_c.pos,
    var_rc = simRoeMetz.config$var_rc.pos
  )
  dFrame.pos <- simMRMC(pos.config)

  # Simulate modality A random effects, negative cases
  modAneg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = modalityID.A,
    readerIDs = readerIDs,
    caseIDs = negCaseIDs,
    mu = simRoeMetz.config$mu.Aneg,
    var_r = simRoeMetz.config$var_r.Aneg,
    var_c = simRoeMetz.config$var_c.Aneg,
    var_rc = simRoeMetz.config$var_rc.Aneg
  )
  dFrame.modAneg <- simMRMC(modAneg.config)

  # Simulate modality A random effects, positive cases
  modApos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = modalityID.A,
    readerIDs = readerIDs,
    caseIDs = posCaseIDs,
    mu = simRoeMetz.config$mu.Apos,
    var_r = simRoeMetz.config$var_r.Apos,
    var_c = simRoeMetz.config$var_c.Apos,
    var_rc = simRoeMetz.config$var_rc.Apos
  )
  dFrame.modApos <- simMRMC(modApos.config)

  # Simulate modality B random effects, negative cases
  modBneg.config <- list(
    nR = nR,
    nC = nC.neg,
    modalityID = modalityID.B,
    readerIDs = readerIDs,
    caseIDs = negCaseIDs,
    mu = simRoeMetz.config$mu.Bneg,
    var_r = simRoeMetz.config$var_r.Bneg,
    var_c = simRoeMetz.config$var_c.Bneg,
    var_rc = simRoeMetz.config$var_rc.Bneg
  )
  dFrame.modBneg <- simMRMC(modBneg.config)

  # Simulate modality B random effects, positive cases
  modBpos.config <- list(
    nR = nR,
    nC = nC.pos,
    modalityID = modalityID.B,
    readerIDs = readerIDs,
    caseIDs = posCaseIDs,
    mu = simRoeMetz.config$mu.Bpos,
    var_r = simRoeMetz.config$var_r.Bpos,
    var_c = simRoeMetz.config$var_c.Bpos,
    var_rc = simRoeMetz.config$var_rc.Bpos
  )
  dFrame.modBpos <- simMRMC(modBpos.config)

  # cat("modality independent mean, neg = ", mean(dFrame.neg$score), "\n")
  # cat("modality independent mean, pos = ", mean(dFrame.pos$score), "\n")
  # cat("modality A mean shift, neg = ", mean(dFrame.modAneg$score), "\n")
  # cat("modality A mean shift, pos = ", mean(dFrame.modApos$score), "\n")
  # cat("modality B mean shift, neg = ", mean(dFrame.modBneg$score), "\n")
  # cat("modality B mean shift, pos = ", mean(dFrame.modBpos$score), "\n")

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

}

#' simRoeMetz.defaultConfig creates a default configuration file for the simRoeMetz program
#'
#' @return simRoeMetz.config [list] Refer to the simRoeMetz input variable
#' @export
#'
# @examples
simRoeMetz.defaultConfig <- function() {

  # Roe Metz Simulation parameters
  # Global means
  mu.neg = 0.0
  mu.pos = 1.0
  # Variance components
  var_r =  0.03
  var_c =  0.30
  var_rc = 0.20

  simRoeMetz.config <- list(
    # Modality labels
    modalityID.A = "modalityA",
    modalityID.B = "modalityB",

    # Experiment size
    nR = 10,
    nC.neg = 100,
    nC.pos = 100,

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

  return(simRoeMetz.config)

}

#' simRoeMetz.example simulates an MRMC ROC experiment
#'
#' @return dFrame.imrmc [data.frame] Please refer to the description of the simRoeMetz return variable
#' @export
#'
# @examples
simRoeMetz.example <- function() {

  simRoeMetz.config <- simRoeMetz.defaultConfig()

  # Simulate data
  dFrame.imrmc <- simRoeMetz(simRoeMetz.config)

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

# set.seed(0)
# simRoeMetz.imrmc <- simRoeMetz.example()
