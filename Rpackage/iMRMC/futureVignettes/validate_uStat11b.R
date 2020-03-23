library(iMRMC)

init.lecuyerRNG()

# Create an MRMC data frame
# refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()
simRoeMetz.config$nC.neg <- 20
simRoeMetz.config$nC.pos <- 20

startTime <- proc.time()[1]
nMC <- 10

df.sim1obs <- data.frame()
df.uStat11.identity <- data.frame()
df.uStat11.diff <- data.frame()
df.uStat21 <- data.frame()
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

      Ar1c1minusBr1c1 = Ar1c1 - Br1c1,
      Ar2c1minusBr2c1 = Ar2c1 - Br2c1,

      Ar1c1minusAr2c1 = Ar1c1 - Ar2c1,

      Br1c1minusBr2c1 = Br1c1 - Br2c1,

      Ar1c1minusBr2c1 = Ar1c1 - Br2c1,
      Ar2c1minusBr1c1 = Ar2c1 - Br1c1,
      Ar1c1minusBr2c1.symmetric = 0.5 * (Ar1c1 - Br2c1 + Ar2c1 - Br1c1),
      stringsAsFactors = TRUE
    )
  )

  result11diff <- uStat11.jointD(
    rbind(df.MRMC$testA.1, df.MRMC$testB.1),
    kernelFlag = 2,
    keyColumns = c("readerID", "caseID", "modalityID", "score"),
    modalitiesToCompare = c("testA", "testB", "testA", "testB"))

  df.uStat11.diff <- rbind(
    df.uStat11.diff,
    data.frame(
      var.Ar1cminusBr1c = result11diff$var.1obs[1], stringsAsFactors = TRUE
    )
  )

  # result21 <- uStat21(
  #   scores.Apos, scores.Bpos, scores.Apos, scores.Bpos,
  #   design.Apos, design.Bpos, design.Apos, design.Bpos,
  #   kernelFlag = 100)
  #
  # df.uStat21 <- rbind(
  #   df.uStat21,
  #   data.frame(
  #     var.Ar1cminusBr2c.symmetric = result21$var.1obs[1], stringsAsFactors = TRUE
  #   )
  # )

  result11.identity <- uStat11.jointD(
    rbind(df.MRMC$testA.1, df.MRMC$testB.1),
    kernelFlag = 1,
    keyColumns = c("readerID", "caseID", "modalityID", "score"),
    modalitiesToCompare = c("testA", "testB"))

  moments <- result11.identity$moments

  var.Arc = moments$c1r1[1] - moments$c0r0[1]
  var.Brc = moments$c1r1[2] - moments$c0r0[2]
  cov.Ar1cBr1c <- moments$c1r1[3] - moments$c0r0[3]

  cov.Ar1cAr2c <- moments$c1r0[1] - moments$c0r0[1]
  cov.Br1cBr2c <- moments$c1r0[2] - moments$c0r0[2]
  cov.Ar1cBr2c <- moments$c1r0[3] - moments$c0r0[3]

  var.Ar1cminusBr1c <- var.Arc + var.Brc - 2 * cov.Ar1cBr1c

  var.Ar1cminusAr2c <- var.Arc + var.Arc - 2 * cov.Ar1cAr2c
  var.Br1cminusBr2c <- var.Brc + var.Brc - 2 * cov.Br1cBr2c

  var.Ar1cminusBr2c <- var.Arc + var.Brc - 2 * cov.Ar1cBr2c

  var.Ar1cminusBr2c.symmetric <-
    0.5 * moments$c1r1[1] + 0.5 * moments$c1r0[1] -   moments$c0r0[1] +
    0.5 * moments$c1r1[2] + 0.5 * moments$c1r0[2] -   moments$c0r0[2] +
    -moments$c1r1[3] +      -moments$c1r0[3] + 2*moments$c0r0[3]

  df.uStat11.identity <- rbind(
    df.uStat11.identity,
    data.frame(
      var.Arc = var.Arc,
      var.Brc = var.Brc,

      var.Ar1cminusBr1c = var.Ar1cminusBr1c,

      var.Ar1cminusAr2c = var.Ar1cminusAr2c,
      var.Br1cminusBr2c = var.Br1cminusBr2c,

      var.Ar1cminusBr2c = var.Ar1cminusBr2c,

      var.Ar1cminusBr2c.symmetric = var.Ar1cminusBr2c.symmetric,
      stringsAsFactors = TRUE
    )
  )

}

df.sim1obs.mcMean <- colMeans(df.sim1obs)
names(df.sim1obs.mcMean) <- paste(names(df.sim1obs.mcMean), ".", "mcMean", sep = "")
df.sim1obs.mcVar <- diag(cov(df.sim1obs))
names(df.sim1obs.mcVar) <- paste(names(df.sim1obs.mcVar), ".", "mcVar", sep = "")

df.uStat11.diff.mcMean <- colMeans(df.uStat11.diff)
names(df.uStat11.diff.mcMean) <- paste(names(df.uStat11.diff.mcMean), ".", "mcMean", sep = "")
df.uStat11.diff.mcVar <- diag(cov(df.uStat11.diff))
names(df.uStat11.diff.mcVar) <- paste(names(df.uStat11.diff.mcVar), ".", "mcVar", sep = "")

df.uStat11.identity.mcMean <- colMeans(df.uStat11.identity)
names(df.uStat11.identity.mcMean) <- paste(names(df.uStat11.identity.mcMean), ".", "mcMean", sep = "")
df.uStat11.identity.mcVar <- diag(cov(df.uStat11.identity))
names(df.uStat11.identity.mcVar) <- paste(names(df.uStat11.identity.mcVar), ".", "mcVar", sep = "")

# df.uStat21.mcMean <- colMeans(df.uStat21)
# names(df.uStat21.mcMean) <- paste(names(df.uStat21.mcMean), ".", "mcMean", sep = "")
# df.uStat21.mcVar <- diag(cov(df.uStat21))
# names(df.uStat21.mcVar) <- paste(names(df.uStat21.mcVar), ".", "mcVar", sep = "")

print("")
print(paste("nMC = ", nMC))

print("")
print("MCmean of single observations")
print(df.sim1obs.mcMean[1:4])

print("")
print("MCmean of within-reader between-modality differences")
print(df.sim1obs.mcMean[5:6])

print("")
print("MCmean of between-reader within-modality differences")
print(df.sim1obs.mcMean[7:8])

print("")
print("MCmean of between-reader between-modality differences (asymmetric)")
print(df.sim1obs.mcMean[9:10])

print("")
print("MCmean of between-reader between-modality differences (symmetric)")
print(df.sim1obs.mcMean[11])

print("")
print("MCvar of observations from a random reader and random case")
print(df.sim1obs.mcVar[1:4])
print(df.uStat11.identity.mcMean[1:2])

print("")
print("MCvar of within-reader and between-modality differences")
print("df.sim1obs.mcVar[5:6]")
print(df.sim1obs.mcVar[5:6])
print("df.uStat11.identity.mcMean[3]")
print(df.uStat11.identity.mcMean[3])
print("df.uStat11.diff.mcMean[1]")
print(df.uStat11.diff.mcMean[1])

print("")
print("MCvar of between-reader within-modality differences")
print("df.sim1obs.mcVar[7:8]")
print(df.sim1obs.mcVar[7:8])
print("df.uStat11.identity.mcMean[4:5]")
print(df.uStat11.identity.mcMean[4:5])

print("")
print("MCvar of between-reader between-modality differences (asymmetric)")
print("df.sim1obs.mcVar[9:10]")
print(df.sim1obs.mcVar[9:10])
print("df.uStat11.identity.mcMean[6]")
print(df.uStat11.identity.mcMean[6])

print("")
print("MCvar of between-reader between-modality differences (symmetric)")
print("df.sim1obs.mcVar[11]")
print(df.sim1obs.mcVar[11])
print("df.uStat11.identity.mcMean[7]")
print(df.uStat11.identity.mcMean[7])
# print("df.uStat21.mcMean")
# print(df.uStat21.mcMean)

endTime <- proc.time()[1]

print(paste("Time to complete", nMC, "observations = ", endTime - startTime))
