library(testthat)
library(iMRMC)

context("limitsOfAgreement")

# Simulate data ###############################################################

# initialize the random number generator
init.lecuyerRNG(stream = 1)

# Create a sample configuration file
config <- sim.NormalIG.Hierarchical.config(nR=9, nC=99, 
                                           modalityID = c("testA","testB"))

# Simulate an MRMC agreement data set
dFrame.imrmc <- sim.NormalIG.Hierarchical(config)

# Create a split-plot data set ################################################
nG <- 3

# Determine reader groups
readerGroups <- createGroups(levels(dFrame.imrmc$readerID), nG)
names(readerGroups) <- c("readerID", "readerGroup")
df <- merge(dFrame.imrmc, readerGroups)

# Determine case groups
caseGroups <- createGroups(levels(factor(dFrame.imrmc$caseID)), nG)
names(caseGroups) <- c("caseID", "caseGroup")
df <- merge(df, caseGroups)

# Create split-plot data
df <- df[df$caseGroup == df$readerGroup, ]
df$caseID <- factor(df$caseID)

# Visualize the design matrix of each modality
dA <- convertDFtoDesignMatrix(df, modality = "testA", dropFlag = FALSE)
dB <- convertDFtoDesignMatrix(df, modality = "testB", dropFlag = FALSE)

image(dA)
image(dB)



# Do the analysis ##############################################################

# Do the within-reader between-modality limits of ageement analysis
result_laWRBM <- laWRBM(df)

# Do the between-reader between-modality limits of agreement analysis
result_laBRBM <- laBRBM(df)

# Do the between-reader within-modality limits of agreement analysis
result_laBRWM <- laBRWM(df)

# Do the within-reader within-modality limits of agreement analysis
result_laWRWM <- laWRWM(df)


# Test #########################################################################

saveData <- FALSE
fileName <-"test_limitsOfAgreement_splitPlot.rda"

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

