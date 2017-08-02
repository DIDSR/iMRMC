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

  df.MRMC$success1 <- rbern(rep(1, nrow(df.MRMC)), df.MRMC$prob)
  empiricalP.bdg1[i] <- mean(df.MRMC$success1)

  df.MRMC$success2 <- sapply(df.MRMC$prob, function(x) rbern(1,x))
  empiricalP.bdg2[i] <- mean(df.MRMC$success2)

}

cat("mean of empirical reader-averaged performance \n",
    mean(empiricalP.bdg1), mean(empiricalP.bdg2), "\n")
cat("variance of empirical reader-averaged performance \n",
    var(empiricalP.bdg1), var(empiricalP.bdg2), "\n")
cat("numerical reader-averaged performance \n", meanModelA(simModelAconfig), "\n")

test_that(
  "meanModelA doesn't change", {
    expect_equal(meanModelA(simModelAconfig), 0.1781831, tolerance = 1e-6)
  }
)
