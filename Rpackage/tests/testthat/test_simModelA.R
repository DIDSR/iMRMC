#'          modalityID [chr] label modalityID
#'          readerIDs  [factor] the ID of each reader
#'          caseIDs    [factor] the ID of each case
#'          mu         [num] mean
#'          var_r      [num] variance of random reader effect
#'          var_c      [num] variance of random case effect
#'          var_rc     [num] variance of random reader by case effect

library(parallel)
library(iMRMCpg)
library("LaplacesDemon")

init.lecuyerRNG(1, 2)

nReaders <- 10
nCases <- 30
mu <- 0
var_r <- 0.7
var_c <- 2.0
var_rc <- 0.0

simMRMC.config <- list(
  modalityID = "modalityA",
  readerIDs = factor(paste("reader",1:nReaders, sep = "")),
  caseIDs = factor(paste("case", 1:nCases, sep = "")),
  mu = mu,
  var_r = var_r,
  var_c = var_c,
  var_rc = var_rc
)

df.MRMC <- simMRMC(simMRMC.config)

df.MRMC$prob <- invlogit(df.MRMC$score)
df.MRMC$success <- rbern(rep(1, nrow(df.MRMC)),df.MRMC$prob)

#=============== RESET ===============

# Create a 2 modality data frame of scores for readers and cases(signal present and signal absent)
df.MRMC <- simRoeMetz(simRoeMetz.defaultConfig())

df.MRMC.A <- df.MRMC[df.MRMC$modalityID == "modalityA",]
scoreMatrix <- convertDFtoScoreMatrix(df.MRMC.A)
desginMatrix <- convertDFtoDesignMatrix(df.MRMC.A)
