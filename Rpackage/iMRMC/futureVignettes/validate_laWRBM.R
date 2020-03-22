#### Simulation set up ####
library(iMRMC)
init.lecuyerRNG()

# Create an MRMC data frame
# refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()
simRoeMetz.config$nC.neg <- 20
simRoeMetz.config$nC.pos <- 20

startTime <- proc.time()[1]
nMC <- 100

#### Loop over MC trials ####
df.sim1obs <- data.frame()
df.laWRBM <- data.frame()
for (i in 1:nMC) {

  # Simulate data
  df.MRMC <- sim.gRoeMetz(simRoeMetz.config)
  df.MRMC <- undoIMRMCdf(df.MRMC)
  df.MRMC <- split(df.MRMC, list(df.MRMC$modalityID, df.MRMC$truth))

  # For each monte carlo trial, the observations are the scores
  # from two readers reading the same case and their difference.
  # This simulation attempts to find the variance of these scores and their difference.
  # This is a little different from other work where each monte carlo trial yields
  # a reader and case averaged quantity.
  Ar1c1 <- df.MRMC$testA.1[
    df.MRMC$testA.1$readerID == "reader1" &
      df.MRMC$testA.1$caseID == "posCase1", "score"]
  Ar2c1 <- df.MRMC$testA.1[
    df.MRMC$testA.1$readerID == "reader2" &
      df.MRMC$testA.1$caseID == "posCase1", "score"]
  Br1c1 <- df.MRMC$testB.1[
    df.MRMC$testB.1$readerID == "reader1" &
      df.MRMC$testB.1$caseID == "posCase1", "score"]
  Br2c1 <- df.MRMC$testB.1[
    df.MRMC$testB.1$readerID == "reader2" &
      df.MRMC$testB.1$caseID == "posCase1", "score"]

  df.sim1obs <- rbind(
    df.sim1obs,
    data.frame(
      Ar1c1 = Ar1c1,
      Ar2c1 = Ar2c1,
      Br1c1 = Br1c1,
      Br2c1 = Br2c1,

      # WRBM
      Ar1c1minusBr1c1 = Ar1c1 - Br1c1,
      Ar2c1minusBr2c1 = Ar2c1 - Br2c1,

      # BRWM
      Ar1c1minusAr2c1 = Ar1c1 - Ar2c1,
      Br1c1minusBr2c1 = Br1c1 - Br2c1,

      # BRBM
      Ar1c1minusBr2c1 = Ar1c1 - Br2c1,
      Ar2c1minusBr1c1 = Ar2c1 - Br1c1,

      # BRBM symmetric
      Ar1c1minusBr2c1.symmetric = 0.5 * (Ar1c1 - Br2c1 + Ar2c1 - Br1c1),
      stringsAsFactors = TRUE
    )
  )

  # Estimate the WRBM limits of agreement
  result.laWRBM <- laWRBM(
    rbind(df.MRMC$testA.1, df.MRMC$testB.1), modalitiesToCompare = c("testA", "testB"))

  # Aggregate the results of all the MC trials into one data frame.
  df.laWRBM <- rbind(df.laWRBM, result.laWRBM)

}

#### Summarize MC simulation ####
# Take the mean and the variance of the observations, including WRBM differences
df.sim1obs.mcMean <- data.frame(t(colMeans(df.sim1obs)), stringsAsFactors = TRUE)
names(df.sim1obs.mcMean) <- paste(names(df.sim1obs.mcMean), ".", "mcMean", sep = "")
df.sim1obs.mcVar <- data.frame(t(diag(cov(df.sim1obs))), stringsAsFactors = TRUE)
names(df.sim1obs.mcVar) <- paste(names(df.sim1obs.mcVar), ".", "mcVar", sep = "")

# Estimate the variance of the limits of aggreement from an MRMC data set
df.laWRBM.mcMean <- data.frame(t(colMeans(df.laWRBM)), stringsAsFactors = TRUE)
names(df.laWRBM.mcMean) <- paste(names(df.laWRBM.mcMean), ".", "mcMean", sep = "")
df.laWRBM.mcVar <- data.frame(t(diag(cov(df.laWRBM))), stringsAsFactors = TRUE)
names(df.laWRBM.mcVar) <- paste(names(df.laWRBM.mcVar), ".", "mcVar", sep = "")

#### Print Results ####
# Print the number of MC trials and the experimental size of each MC trial
print("")
desc <- data.frame(nMC = nMC, nR = simRoeMetz.config$nR, nC = simRoeMetz.config$nC.pos, stringsAsFactors = TRUE)
print(paste("nMC = ", nMC))

print("")
print("MCmean of single observations")
print(df.sim1obs.mcMean[1:4])

print("")
print("MCmean of within-reader between-modality differences")
print(df.sim1obs.mcMean[5:6])

print("")
print("MCvar of within-reader and between-modality differences")
print("df.sim1obs.mcVar[5:6]")
print(df.sim1obs.mcVar[5:6])
print("df.laWRBM.mcMean$var.mcMean")
print(df.laWRBM.mcMean)
print("df.laWRBM.mcMean$var.mcVar")
print(df.laWRBM.mcVar)

hist(df.sim1obs$Ar1c1minusBr1c1)
print("MC confidence interval")
print(sort(df.sim1obs$Ar1c1minusBr1c1)[c(0.025, .975)*nMC])

# Print the time to complete this MC simulation
endTime <- proc.time()[1]
print(paste("Time to complete", nMC, "observations = ", endTime - startTime))

