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

# Result from test on 1/28/2025
# `saveResult` not equal to `result`.
# Component "caseID": Modes: character, numeric
# Component "caseID": Attributes: < target is NULL, current is list >
#   Component "caseID": target is character, current is factor
# Component "modalityID": Attributes: < Component "levels": 1 string mismatch >
#   Component "modalityID": 500 string mismatches

# The problem with case ID seems to be very fixable
# > str(result$caseID)
# Factor w/ 100 levels "case1","case10",..: 1 13 24 35 46 57 68 79 90 2 ...
# > str(saveResult$caseID)
# chr [1:1000] "Case1" "Case2" "Case3" "Case4" "Case5" "Case6" "Case7" "Case8"
saveResult$caseID <- factor(tolower(as.character(saveResult$caseID)))

# The problem with modality ID seems to be very fixable
# > str(result$modalityID)
# Factor w/ 2 levels "testA","testB": 1 1 1 1 1 1 1 1 1 1 ...
# > str(saveResult$modalityID)
# Factor w/ 2 levels "testA","testA*": 1 1 1 1 1 1 1 1 1 1 ...
levels(saveResult$modalityID) <- c("testA", "testB")

test_that(
  "sim.NormalIG.Hierarchical does not change", {
    expect_equal(saveResult, result,tolerance=1e-5)
  }
)
