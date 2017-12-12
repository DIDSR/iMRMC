library(testthat)
library(iMRMC)

context("uStat11")

init.lecuyerRNG()

# This flag should always be false except when the tests are first created.
flagSave <- FALSE
if (flagSave) {
  saveResult <- list()
}

# Create an MRMC data frame
# Refer to Gallas2014_J-Med-Img_v1p031006
simRoeMetz.config <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(simRoeMetz.config)

# Reformat data
df <- undoIMRMCdf(df.MRMC)

# Grab part of the data
df <- droplevels(df[grepl("pos", df$caseID), ])

#### uStat11.jointD.identity ####
# Calculate the reader- and case-averaged difference in scores from testA and testB
# (kernelFlag = 1 specifies the U-statistics kernel to be the identity)
result.jointD.identity <- uStat11.jointD(
  df,
  kernelFlag = 1,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("testA", "testB"))

cat("\n")
cat("uStat11.jointD.identity \n")
print(result.jointD.identity[1:2])

if (flagSave) {
  saveResult$jointD.identity <- result.jointD.identity
}

#### uStat11.conditionalD.identity ####
# Calculate the reader- and case-averaged difference in scores from testA and testB
# (kernelFlag = 1 specifies the U-statistics kernel to be the identity)
result.conditionalD.identity <- uStat11.conditionalD(
  df,
  kernelFlag = 1,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("testA", "testB"))

cat("\n")
cat("uStat11.conditionalD.identity \n")
print(result.conditionalD.identity[1:2])

if (flagSave) {
  saveResult$conditionalD.identity <- result.conditionalD.identity
}

#### uStat11.jointD.diff ####
# Calculate the reader- and case-averaged difference in scores from testA and testB
# (kernelFlag = 2 specifies the U-statistics kernel to be the difference in scores)
result.jointD.diff <- uStat11.jointD(
  df,
  kernelFlag = 2,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("testA", "testB", "testB", "testA"))

cat("\n")
cat("uStat11.jointD.diff \n")
print(result.jointD.diff[1:2])

if (flagSave) {
  saveResult$jointD.diff <- result.jointD.diff
}

#### uStat11.conditionalD.diff ####
# Calculate the reader- and case-averaged difference in scores from testA and testB
# (kernelFlag = 2 specifies the U-statistics kernel to be the difference in scores)
result.conditionalD.diff <- uStat11.conditionalD(
  df,
  kernelFlag = 2,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("testA", "testB", "testB", "testA"))

cat("\n")
cat("uStat11.conditionalD.diff \n")
print(result.conditionalD.diff[1:2])

if (flagSave) {
  saveResult$conditionalD.diff <- result.conditionalD.diff
}

#### TESTS ####

# Save the result to a file for future comparisons
fileName <- "test_uStat11.Rdata"
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
  "uStat11.jointD, difference kernel, doesn't change", {
    expect_equal(saveResult$jointD.diff, result.jointD.diff)
  }
)

test_that(
  "uStat11.conditionalD, difference kernel, doesn't change", {
    expect_equal(saveResult$conditionalD.diff, result.conditionalD.diff)
  }
)

test_that(
  "uStat11.jointD, identity kernel, doesn't change", {
    expect_equal(saveResult$jointD.identity, result.jointD.identity)
  }
)

test_that(
  "uStat11.conditionalD, identity kernel, doesn't change", {
    expect_equal(saveResult$conditionalD.identity, result.conditionalD.identity)
  }
)
