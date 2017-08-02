library(testthat)
library(parallel)
library(iMRMCpg)

context("simRoeMetz")

init.lecuyerRNG()

# Create an MRMC data frame
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

# Split the data into four pieces, not including truth
df.MRMC.Apos <- droplevels(df.MRMC[grepl("pos", df.MRMC$caseID) & grepl("A", df.MRMC$modalityID), ])
df.MRMC.Aneg <- droplevels(df.MRMC[grepl("neg", df.MRMC$caseID) & grepl("A", df.MRMC$modalityID), ])
df.MRMC.Bpos <- droplevels(df.MRMC[grepl("pos", df.MRMC$caseID) & grepl("B", df.MRMC$modalityID), ])
df.MRMC.Bneg <- droplevels(df.MRMC[grepl("neg", df.MRMC$caseID) & grepl("B", df.MRMC$modalityID), ])

print("")
cat("modality A mean shift, neg = ", mean(df.MRMC.Aneg$score), "\n")
cat("modality A mean shift, pos = ", mean(df.MRMC.Apos$score), "\n")
cat("modality B mean shift, neg = ", mean(df.MRMC.Bneg$score), "\n")
cat("modality B mean shift, pos = ", mean(df.MRMC.Bpos$score), "\n")

test_that(
  "sim.gRoeMetz does not change", {
    expect_equal(mean(df.MRMC.Aneg$score), -0.1642567, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Apos$score),  1.012758, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Bneg$score), -0.07882189, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Bpos$score),  1.122282, tolerance = 1e-6)
  }
)

