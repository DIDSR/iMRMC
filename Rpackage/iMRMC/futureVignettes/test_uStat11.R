library(iMRMC)

init.lecuyerRNG()

# Create an MRMC data frame
# refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()
simRoeMetz.config$nC.neg <- 20
simRoeMetz.config$nC.pos <- 20

startTime <- proc.time()[1]
nMC <- 100

df.simMean <- data.frame()
for (i in 1:nMC) {

  # Simulate data
  df.MRMC <- sim.gRoeMetz(simRoeMetz.config)

  # Split the data
  df.MRMC.pos <- droplevels(df.MRMC[grepl("pos", df.MRMC$caseID), ])
  df.MRMC.neg <- droplevels(df.MRMC[grepl("neg", df.MRMC$caseID), ])

  result <- uStat11.jointD(
    df.MRMC.pos,
    kernelFlag = 2,
    keyColumns = c("readerID", "caseID", "modalityID", "score"),
    modalitiesToCompare = c("testA", "testB", "testB", "testA"))

  df.simMean <- rbind(
    df.simMean,
    data.frame(
      mean.AB = result$mean[1],
      mean.CD = result$mean[2],
      mean.ABminusCD = result$mean[3],
      var.AB = result$var[1],
      var.CD = result$var[2],
      var.ABminusCD = result$var[3]
    )
  )

}

print("")
print(paste("intraRdiff: mcMean over", nMC, "obs"))
print(colMeans(df.simMean))
print("")
print(paste("intraRdiff: mcVar over", nMC, "obs"))
print(diag(cov(df.simMean)))
stopTime <- proc.time()[1]
cat("\n")
cat("Simulation duration:", stopTime - startTime)