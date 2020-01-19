library(testthat)
library(iMRMC)

context("doIMRMC given fileName")

# Check that the input file exists. This should be created by test_doIMRMC.R
fileName <- "input.imrmc"
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}

# Analyze the input file
result <- tryCatch(
  result <- doIMRMC(fileName = fileName, stripDatesForTests = TRUE),
  warning = function(w) {
    print(w)
    result <- list(error = 1)
    return(result)
  },
  error = function(w) {
    print(w)
    result <- list(error = 1)
    return(result)
  }
) 

#### TEST ####

# This test is to verify that the results do not change over time
# If doIMRMC crashes because the CRAN test environment doesn't
# have java or doesn't have the right version of java,
# I don't want the test to fail. That error is expected.
if (!names(result)[1] == "error") {
 
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
  
}