library(testthat)
library(iMRMC)

context("simRoeMetz")

# This flag should always be false except when the tests are first created.
flagSave <- FALSE

init.lecuyerRNG()

# Create an MRMC data frame
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
result <- sim.gRoeMetz(config.gRoeMetz)
saveResult <- result

# Split the data into four pieces, not including truth
df.MRMC.Apos <- droplevels(result[grepl("pos", result$caseID) & grepl("A", result$modalityID), ])
df.MRMC.Aneg <- droplevels(result[grepl("neg", result$caseID) & grepl("A", result$modalityID), ])
df.MRMC.Bpos <- droplevels(result[grepl("pos", result$caseID) & grepl("B", result$modalityID), ])
df.MRMC.Bneg <- droplevels(result[grepl("neg", result$caseID) & grepl("B", result$modalityID), ])

print("")
cat("modality A mean shift, neg = ", mean(df.MRMC.Aneg$score), "\n")
cat("modality A mean shift, pos = ", mean(df.MRMC.Apos$score), "\n")
cat("modality B mean shift, neg = ", mean(df.MRMC.Bneg$score), "\n")
cat("modality B mean shift, pos = ", mean(df.MRMC.Bpos$score), "\n")

#### TEST ####

# Save the result to a file for future comparisons
fileName <- "test_simRoeMetz.Rdata"
if (flagSave) {
  save(saveResult, file = file.path("tests", "testthat", fileName))
}

# Recover the expected results
saveResult <- 0
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
load(fileName)

test_that(
  "sim.gRoeMetz does not change", {
    expect_equal(saveResult, result,tolerance=1e-5)
  }
)
