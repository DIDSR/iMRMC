library(testthat)
library(parallel)
library(iMRMC)

context("createIMRMCdf and undoIMRMCdf")

init.lecuyerRNG()

# Create an MRMC configuration file
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

df <- undoIMRMCdf(df.MRMC)

keyColumns <- list(
  readerID = "readerID",
  caseID = "caseID",
  modalityID = "modalityID",
  score = "score",
  truth = "truth"
)
df.MRMC2 <- createIMRMCdf(df, keyColumns, "1")

# Analyze the input file
result <- tryCatch(
  doIMRMC(data = df.MRMC2, stripDatesForTests = TRUE),
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
  saveResult <- 0
  fileName <- "test_doIMRMC.Rdata"
  if (!file.exists(fileName)) {
    fileName <- file.path("tests", "testthat", fileName)
  }
  load(fileName)
  
  test_that(
    "createIMRMCdf and undoIMRMCdf work as expected", {
      expect_equal(saveResult, result,tolerance=1e-5)
    }
  ) 
}