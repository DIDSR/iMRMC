# In this test we create a simulated iMRMC data frame and analyze it (result.target).
# Then we convert the iMRMC format data frame to a different format with the
# function undoIMRMCdf (truth is added as a new column instead of truth in rows).
# Then we convert the data frame back to the iMRMC format data frame and 
# analyze it again (result.current).

library(testthat)
library(parallel)
library(iMRMC)

context("createIMRMCdf and undoIMRMCdf")

init.lecuyerRNG()

# Create an MRMC configuration file
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

# Convert the data into new format (truth in new column instead of truth in rows)
df.ALT <- undoIMRMCdf(df.MRMC)

keyColumns <- list(
  readerID = "readerID",
  caseID = "caseID",
  modalityID = "modalityID",
  score = "score",
  truth = "truth"
)

# Convert data back to original iMRMC format
df.MRMC2 <- createIMRMCdf(df.ALT, keyColumns, "1")

# Sort the data frames by readerID, caseID, modalityID, score
index <- order(df.MRMC$readerID, df.MRMC$caseID, df.MRMC$modalityID, df.MRMC$score)
df.MRMC <- df.MRMC[index, ]
rownames(df.MRMC) <- NULL

index <- order(df.MRMC2$readerID, df.MRMC2$caseID, df.MRMC2$modalityID, df.MRMC2$score)
df.MRMC2 <- df.MRMC2[index, ]
rownames(df.MRMC2) <- NULL



#### TEST ####

test_that(
  "createIMRMCdf and undoIMRMCdf work as expected", {
    expect_equal(df.MRMC, df.MRMC2)
  }
) 
