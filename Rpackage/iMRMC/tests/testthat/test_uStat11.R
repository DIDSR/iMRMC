library(testthat)
library(iMRMC)

context("uStat11Diff")

init.lecuyerRNG()

# Create an MRMC data frame
# refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()
simRoeMetz.config$nC.neg <- 20
simRoeMetz.config$nC.pos <- 20

startTime <- proc.time()[1]
nMC <- 10

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
    modalitiesToCompare = c("modalityA", "modalityB", "modalityB", "modalityA"))

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
print("intraRdiff: mean and variance from first observation")
print(df.simMean[1, ])

test_that(
  "uStat11, difference kernel, doesn't change", {
    expect_equal(df.simMean$mean.AB[1], -0.224748287)
    expect_equal(df.simMean$var.AB[1], 0.05762784)
  }
)

print("")
print(paste("intraRdiff: mcMean over", nMC, "obs"))
print(colMeans(df.simMean))
print("")
print(paste("intraRdiff: mcVar over", nMC, "obs"))
print(diag(cov(df.simMean)))
