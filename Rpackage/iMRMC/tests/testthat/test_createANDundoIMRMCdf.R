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

result <- doIMRMC(data = df.MRMC2, stripDatesForTests = TRUE)

#### TEST ####

# Recover the expected results
saveResult <- 0
fileName <- "test_doIMRMC.Rdata"
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
load(fileName)

test_that(
  "createIMRMCdf and undoIMRMCdf work as expected", {
    expect_equal(saveResult, result)
  }
)