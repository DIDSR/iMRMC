library(testthat)
library(iMRMC)

context("limitsOfAgreement")

# Simulate data ###############################################################

# initialize the random number generator
init.lecuyerRNG(stream = 1)

# Create a sample configuration file
config <- sim.NormalIG.Hierarchical.config(modalityID = c("testA","testB"))

# Simulate an MRMC agreement data set
dFrame.imrmc <- sim.NormalIG.Hierarchical(config)


# Do the analysis ##############################################################

# Do the within-reader between-modality limits of ageement analysis
result_laWRBM <- laWRBM(dFrame.imrmc)

# Do the between-reader between-modality limits of agreement analysis
result_laBRBM <- laBRBM(dFrame.imrmc)

# Do the between-reader within-modality limits of agreement analysis
result_laBRWM <- laBRWM(dFrame.imrmc)

# Do the within-reader within-modality limits of agreement analysis
result_laWRWM <- laWRWM(dFrame.imrmc)


# Test #########################################################################

saveData <- FALSE
fileName <-"test_limitsOfAgreement.rda"

if(saveData){
  #  Save the result to a file for future comparisons
  target_laWRBM <- result_laWRBM
  target_laBRBM <- result_laBRBM
  target_laBRWM <- result_laBRWM
  target_laWRWM <- result_laWRWM
  
  save(target_laWRBM, target_laBRBM, target_laBRWM, target_laWRWM,
       file = file.path("tests", "testthat", fileName))

}else{
  
  # Recover the expected results
  if (!file.exists(fileName)) {
    fileName <- file.path("tests", "testthat", fileName)
  }
  load(fileName)  
  
}

# laWRBM output #####################

test_that(
  "laWRBM output does not change", {
    expect_equal(target_laWRBM, result_laWRBM, tolerance = 1e-6)
  }
)

# laBRBM output #####################

test_that(
  "laBRBM output does not change", {
    expect_equal(target_laBRBM, result_laBRBM, tolerance = 1e-6)
  }
)

# laBRWM output #####################

test_that(
  "laBRWM output does not change", {
    expect_equal(target_laBRWM, result_laBRWM, tolerance = 1e-6)
  }
)

# laWRWM output #####################

test_that(
  "laWRWM output does not change", {
    expect_equal(target_laWRWM, result_laWRWM, tolerance = 1e-6)
  }
)

