library(testthat)
library(parallel)
library(iMRMCpg)
library(LaplacesDemon)

init.lecuyerRNG()

# Size of Monte Carlo Experiment
nMC <- 100

# Experiment size
nR <- 10
nC <- 100

# Default model parameter probability space
p <- 0.1

# Default model parameters logit space
mu <- logit(p)
var_c <-  2.00
var_r <-  0.70
var_rc <- 0.0

simModelAconfig <- list(
  Nr = nR,
  Nc = nC,
  modalityID = "modalityA",
  readerIDs = factor(paste("reader",1:nR, sep = "")),
  caseIDs = factor(paste("case", 1:nC, sep = "")),
  mu = mu,
  p = p,
  var_c = var_c,
  var_r = var_r,
  var_rc = var_rc
)

empiricalP.bdg1 <- NULL
empiricalP.bdg2 <- NULL
for (i in 1:nMC) {

  df.MRMC <- simMRMC(simModelAconfig)
  df.MRMC$prob <- invlogit(df.MRMC$score)
  df.MRMC$success <- rbern(rep(1, nrow(df.MRMC)), df.MRMC$prob)
  empiricalP.bdg1[i] <- mean(df.MRMC$success)

  df.MRMC.modelA <- sim.ModelA(simModelAconfig)
  empiricalP.bdg2[i] <- mean(df.MRMC.modelA$success)

}

print("")
cat("first empirical reader-averaged performance \n",
    empiricalP.bdg1[1], empiricalP.bdg2[1], "\n")
cat("mean of empirical reader-averaged performance \n",
    mean(empiricalP.bdg1), mean(empiricalP.bdg2), "\n")
cat("variance of empirical reader-averaged performance \n",
    var(empiricalP.bdg1), var(empiricalP.bdg2), "\n")
cat("numerical reader-averaged performance \n", meanModelA(simModelAconfig), "\n")

test_that(
  "sim.ModelA doesn't change", {
    expect_equal(empiricalP.bdg1[1], 0.147, tolerance = 1e-3)
    expect_equal(empiricalP.bdg2[1], 0.179, tolerance = 1e-3)
  }
)

test_that(
  "meanModelA doesn't change", {
    expect_equal(meanModelA(simModelAconfig), 0.1781831, tolerance = 1e-6)
  }
)
