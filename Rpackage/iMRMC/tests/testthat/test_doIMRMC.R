library(testthat)
library(iMRMC)

context("doIMRMC")

# This flag should always be false except when the tests are first created.
flagSave <- FALSE

tic <- proc.time()

init.lecuyerRNG()

# Create an MRMC data frame
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

# While developing, we need to create and save the input file for other tests.
workDir <- file.path("tests", "testthat")

# After developing has stablized, we want to leave the input file unchanged.
workDir <- NULL

# Analyze the input file
result <- doIMRMC(df.MRMC, workDir = workDir, stripDatesForTests = TRUE)
saveResult <- result

cat("\n")
print(result$Ustat[, 1:10])
cat("\n")
print(result$Ustat[, c(1:6, 11:12)])
cat("\n")
print(result$Ustat[, c(1:6, 17:21)])

toc <- proc.time()
cat("\n  Elapsed time: ", toc[1:3] - tic[1:3], "\n")

#### TEST ####

# Save the result to a file for future comparisons
fileName <- "test_doIMRMC.Rdata"
if (flagSave) {
  save(saveResult, file = file.path("tests", "testthat", "test_doIMRMC.Rdata"))
}

# Recover the expected results
saveResult <- 0
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
load(fileName)

test_that(
  "doIMRMC does not change", {
    expect_equal(saveResult, result)
  }
)