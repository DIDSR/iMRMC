library(iMRMC)

init.lecuyerRNG()

# Create an MRMC data frame ####
# refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()
simRoeMetz.config$nR <- 8
simRoeMetz.config$nC.neg <- 17
simRoeMetz.config$nC.pos <- 17
nG <- 3

startTime <- proc.time()[1]
nMC <- 10

# Start loop over MC trials ####
df.sim1obs <- data.frame()
df.uStat11.identity <- data.frame()
df.uStat11.diff <- data.frame()
df.uStat21 <- data.frame()
for (i in 1:nMC) {

  # Simulate data -> split-plot ####
  df <- sim.gRoeMetz(simRoeMetz.config)
  df <- undoIMRMCdf(df)

  # Only need the positive cases
  df <- droplevels(subset(df, truth == 1))

  readers <- levels(df$readerID)
  nR <- nlevels(df$readerID)
  cases <- levels(df$caseID)
  nC <- nlevels(df$caseID)

  # Determine reader groups
  readerGroups <- createGroups(readers, nG)
  names(readerGroups) <- c("readerID", "readerGroup")
  df <- merge(df, readerGroups)

  # Determine case groups
  caseGroups <- createGroups(cases, nG)
  names(caseGroups) <- c("caseID", "caseGroup")
  df <- merge(df, caseGroups)

  # Create split-plot data
  df <- df[df$caseGroup == df$readerGroup, ]

  # Remove one reader's data
  df <- df[!(df$readerID == "reader5" & df$modalityID == "testA"), ]

  # Remove one case's data
  df <- df[!(df$caseID == "posCase5" & df$modalityID == "testB"), ]

  # Visualize the design matrix of each modality
  dA <- convertDFtoDesignMatrix(df, modality = "testA", dropFlag = FALSE)
  dB <- convertDFtoDesignMatrix(df, modality = "testB", dropFlag = FALSE)
  sA <- convertDFtoScoreMatrix(df, modality = "testA", dropFlag = FALSE)
  sB <- convertDFtoScoreMatrix(df, modality = "testB", dropFlag = FALSE)

  image(dA)
  image(dB)
  image(sA)
  image(sB)

  # Extract single observations ####
  # For each monte carlo trial, the observations are the scores
  # from two readers reading the same case and their difference.
  # This simulation attempts to find the variance of these scores and their difference.
  # This is a little different from other work where each monte carlo trial yields
  # a reader and case averaged quantity.
  Ar1c1 <- sA["posCase1", "reader1"]
  Ar2c1 <- sA["posCase1", "reader2"]
  Br1c1 <- sB["posCase1", "reader1"]
  Br2c1 <- sB["posCase1", "reader2"]
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

  # Ar1c1 <- df.MRMC$testA.1[
  #   df.MRMC$testA.1$readerID == "reader1" &
  #     df.MRMC$testA.1$caseID == "posCase1", "score"]
  # Ar2c1 <- df.MRMC$testA.1[
  #   df.MRMC$testA.1$readerID == "reader2" &
  #     df.MRMC$testA.1$caseID == "posCase1", "score"]
  # Br1c1 <- df.MRMC$testB.1[
  #   df.MRMC$testB.1$readerID == "reader1" &
  #     df.MRMC$testB.1$caseID == "posCase1", "score"]
  # Br2c1 <- df.MRMC$testB.1[
  #   df.MRMC$testB.1$readerID == "reader2" &
  #     df.MRMC$testB.1$caseID == "posCase1", "score"]
  #
  # df.sim1obs <- rbind(
  #   df.sim1obs,
  #   data.frame(
  #     Ar1c1 = Ar1c1,
  #     Ar2c1 = Ar2c1,
  #     Br1c1 = Br1c1,
  #     Br2c1 = Br2c1,
  #
  #     Ar1c1minusBr1c1 = Ar1c1 - Br1c1,
  #     Ar2c1minusBr2c1 = Ar2c1 - Br2c1,
  #
  #     Ar1c1minusAr2c1 = Ar1c1 - Ar2c1,
  #
  #     Br1c1minusBr2c1 = Br1c1 - Br2c1,
  #
  #     Ar1c1minusBr2c1 = Ar1c1 - Br2c1,
  #     Ar2c1minusBr1c1 = Ar2c1 - Br1c1,
  #     Ar1c1minusBr2c1.symmetric = 0.5 * (Ar1c1 - Br2c1 + Ar2c1 - Br1c1),
  #     stringsAsFactors = TRUE
  #   )
  # )

  # uStat11.conditionalD.diff ####
  result11.diff <- uStat11.conditionalD(
    df, kernelFlag = 2,
    keyColumns = c("readerID", "caseID", "modalityID", "score"),
    modalitiesToCompare = c("testA", "testB", "testA", "testB"))

  df.uStat11.diff <- rbind(
    df.uStat11.diff,
    data.frame(
      var.Ar1cminusBr1c = result11.diff$var.1obs[1],
      stringsAsFactors = TRUE
    )
  )

  # uStat11.conditionalD.identity ####
  result11.identity <- uStat11.conditionalD(
    df, kernelFlag = 1,
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
print("One estimate of variance from uStat11.conditionalD.difference$var[1]")
print(result11.diff$var[1])
print("One estimate of variance from laWRBM$var")
print(laWRBM(df, modalitiesToCompare = c("testA", "testB"))$var)

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
print("df.uStat11.identity.mcMean[3] (SE)")
print(paste(
  round(df.uStat11.identity.mcMean[3], digits = 3),
  " (", round(sqrt(df.uStat11.identity.mcVar[3]), digits = 3), ")",
  collapse = ""
))
print("df.uStat11.diff.mcMean[1] (SE)")
print(paste(
  round(df.uStat11.diff.mcMean[1], digits = 2),
  " (", round(sqrt(df.uStat11.diff.mcVar[1]), digits = 3), ")",
  collapse = ""
))

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
