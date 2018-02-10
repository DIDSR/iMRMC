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
df.laBRWM <- data.frame()
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
      Ar1c1minusBr2c1.symmetric = 0.5 * (Ar1c1 - Br2c1 + Ar2c1 - Br1c1)
    )
  )

  df <- rbind(df.MRMC$testA.1, df.MRMC$testB.1)
  keyColumns <- c("readerID", "caseID", "modalityID", "score")
  modalitiesToCompare <- c("testA", "testB")

  # Estimate the BRWM limits of agreement
  result <- uStat11.conditionalD(
    df,
    kernelFlag = 1,
    keyColumns = keyColumns,
    modalitiesToCompare = modalitiesToCompare)

  moments <- result$moments

  var.Arc = moments$c1r1[1] - moments$c0r0[1]
  var.Brc = moments$c1r1[2] - moments$c0r0[2]

  cov.Ar1cAr2c <- moments$c1r0[1] - moments$c0r0[1]
  cov.Br1cBr2c <- moments$c1r0[2] - moments$c0r0[2]

  var.Ar1cminusAr2c <- var.Arc + var.Arc - 2 * cov.Ar1cAr2c
  var.Br1cminusBr2c <- var.Brc + var.Brc - 2 * cov.Br1cBr2c

  df.laBRWM <- rbind(
      df.laBRWM,
    data.frame(
      var.Ar1cminusAr2c = var.Ar1cminusAr2c,
      var.Br1cminusBr2c = var.Br1cminusBr2c
    )
  )

}

#### Summarize MC simulation ####
# Take the mean and the variance of the observations, including WRBM differences
df.sim1obs.mcMean <- colMeans(df.sim1obs)
names(df.sim1obs.mcMean) <- paste(names(df.sim1obs.mcMean), ".", "mcMean", sep = "")
df.sim1obs.mcVar <- diag(cov(df.sim1obs))
names(df.sim1obs.mcVar) <- paste(names(df.sim1obs.mcVar), ".", "mcVar", sep = "")

# Estimate the variance of the limits of aggreement from an MRMC data set
df.laBRWM.mcMean <- colMeans(df.laBRWM)
names(df.laBRWM.mcMean) <- paste(names(df.laBRWM.mcMean), ".", "mcMean", sep = "")
df.laBRWM.mcVar <- diag(cov(df.laBRWM))
names(df.laBRWM.mcVar) <- paste(names(df.laBRWM.mcVar), ".", "mcVar", sep = "")

#### Print Results ####
# Print the number of MC trials and the experimental size of each MC trial
print("")
desc <- data.frame(nMC = nMC, nR = simRoeMetz.config$nR, nC = simRoeMetz.config$nC.pos)
print(paste("nMC = ", nMC))

# Print the time to complete this MC simulation
endTime <- proc.time()[1]
print(paste("Time to complete", nMC, "observations = ", endTime - startTime))

print("")
print("MCmean of single observations")
print(df.sim1obs.mcMean[1:4])



print("")
print("MCmean of between-reader within-modality differences")
print(df.sim1obs.mcMean[7:8])

print("")
print("MCvar of between-reader within-modality differences")
print("df.sim1obs.mcVar[7:8]")
print(df.sim1obs.mcVar[7:8])
print("df.laBRWM.mcVar")
print(df.laBRWM.mcMean)

