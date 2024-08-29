library(testthat)
library(iMRMC)

context("simNormalIGHierarchical")

# This flag should always be false except when the tests are first created.
flagSave <- FALSE

init.lecuyerRNG()

# Create an MRMC data frame
config.NormalIG <- sim.NormalIG.Hierarchical.config()

# Simulate data
result <- sim.NormalIG.Hierarchical(config.NormalIG)

str(result)

#### TEST ####

# Save the result to a file for future comparisons
fileName <- "test_simNormalIGHierarchical.rda"
if (flagSave) {
  saveResult <- result
  save(saveResult, file = file.path("tests", "testthat", fileName))
}

# Recover the expected results
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
load(fileName)

test_that(
  "sim.NormalIG.Hierarchical does not change", {
    expect_equal(saveResult, result,tolerance=1e-5)
  }
)
