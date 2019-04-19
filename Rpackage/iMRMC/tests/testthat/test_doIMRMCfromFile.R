library(testthat)
library(iMRMC)

context("doIMRMC given fileName")

# Check that the input file exists. This should be created by test_doIMRMC.R
fileName <- "input.imrmc"
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}

# Analyze the input file
result <- doIMRMC(fileName = fileName, stripDatesForTests = TRUE)

# Recover the expected results
fileName <- "test_doIMRMC.Rdata"
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
load(fileName)

test_that(
  "doIMRMC given fileName does not change", {
    expect_equal(saveResult, result,tolerance=1e-5)
  }
)