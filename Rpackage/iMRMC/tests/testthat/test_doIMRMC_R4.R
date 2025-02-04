# In this test we compare doIMRMC_java that uses the java engine
# to doIMRMC that uses R code
# This is the default configuration from the simulation software. 
# To test the robustness of the software, we change the data as follows:
# We change the number of readers from nR=5 to nR=10.
# We delete the last five readers from modality A.
# We delete the first five readers from modality B.
# These changes leave readers unpaired across modalities



library(testthat)
library(iMRMC)

testthat::context("doIMRMC_R3")

# Simulate data ###############################################################

# initialize the random number generator
init.lecuyerRNG(stream = 3)

# Create a sample configuration file
config <- sim.gRoeMetz.config()

# Increase the number of readers
config$nR <- 10

# Simulate an MRMC ROC data set
dFrame.imrmc <- sim.gRoeMetz(config)
dFrame.imrmc$modalityID <- as.character(dFrame.imrmc$modalityID)
dFrame.imrmc$readerID <- as.character(dFrame.imrmc$readerID)
dFrame.imrmc$caseID <- as.character(dFrame.imrmc$caseID)

# Delete the last five readers from modality A
lastFiveReaders <- c(
  "reader6", "reader7", "reader8", "reader9", "reader10"
)
index <- (
  dFrame.imrmc$readerID %in% lastFiveReaders &
    dFrame.imrmc$modalityID == "testA"
)
dFrame.imrmc <- dFrame.imrmc[!index, ]

# Delete the first five readers from modality B
firstFiveReaders <- c(
  "reader1", "reader2", "reader3", "reader4", "reader5"
)
index <- (
  dFrame.imrmc$readerID %in% firstFiveReaders &
    dFrame.imrmc$modalityID == "testB"
)
dFrame.imrmc <- dFrame.imrmc[!index, ]



# Process data ###############################################################

# Analyze the MRMC ROC data using new R code
# Processing time before BDG
# user  system elapsed 
# 3.36    0.81    4.17

# source(file.path("R", "iMRMC_R.R"))
start.time <- proc.time()

result_doIMRMC_current <- doIMRMC(dFrame.imrmc)

end.time <- proc.time()
print("Runtime for R code")
print(end.time - start.time)



# Analyze the MRMC ROC data with legacy code
saveData <- FALSE
if (saveData) {
  
  # Results from legacy code are not available or need to be updated
  start.time <- proc.time()
  
  # Processing time
  # user  system elapsed
  # 0.07    0.04    3.17
  targetTest3 <- iMRMC::doIMRMC_java(dFrame.imrmc)
  save(targetTest3,
       file = file.path("tests", "testthat", "test_doIMRMC_R4.rda"))
  
  end.time <- proc.time()
  print("Runtime for legacy code created")
  print(end.time - start.time)
  
  browser()
  
} else {
  
  # Results from legacy code need to be read from the package
  # There are two modes for running: standard and test
  
  # The working directory when running in standard mode is the package root
  temp <- list.files(
    path = file.path("tests", "testthat", "."),
    pattern = "test_doIMRMC_R4.rda")
  
  if (length(temp) > 0) {
    if (temp == "test_doIMRMC_R4.rda")
      load(file.path("tests", "testthat", "test_doIMRMC_R4.rda"))
  }
  
  # The working directory when running in test mode is the tests/testthat directory
  temp <- list.files(
    path = ".",
    pattern = "test_doIMRMC_R4.rda")
  
  if (length(temp) > 0) {
    if ( temp == "test_doIMRMC_R4.rda")
      load(file.path(".", "test_doIMRMC_R4.rda"))
  }
  
}
result_doIMRMC_target <- targetTest3



# perReader output full #############################################
target <- result_doIMRMC_target$perReader
current <- result_doIMRMC_current$perReader

# The target result has 28 variables/columns
# The first three variables are date dependent and not part of the current output - Remove
# Variables 15 through 28 of target results are not part of the current output - Remove
target <- result_doIMRMC_target$perReader[,4:14]

# Remove readers with no observations from target
index.delete <- target$N0 < 2
target <- target[!index.delete, ]
index.delete <- target$N1 < 2
target <- target[!index.delete, ]
rownames(target) <- NULL

# target$modalityB has factor memory: fix
target$modalityB <- factor(target$modalityB)

# target$varAUCAminusAUCB are all NA, but type is logical, change to numeric
target$varAUCAminusAUCB <- as.numeric(target$varAUCAminusAUCB)

# R code has 9 digits significance
# Java code has 8 digits significance
# The reason is that the Java function reads and writes files.
testthat::test_that(
  "doIMRMC perReader output does not change", {
    testthat::expect_equal(target, current,tolerance = 1e-6)
  }
)



# Ustat output ###############
target = result_doIMRMC_target$Ustat[,4:24]
current = result_doIMRMC_current$Ustat

testthat::test_that(
  "doIMRMC Ustat output does not change", {
    testthat::expect_equal(target, current)
  }
)



# MLEstat output ###############
target = result_doIMRMC_target$MLEstat[,4:24]
current = result_doIMRMC_current$MLEstat

testthat::test_that(
  "doIMRMC MLEstat output does not change", {
    testthat::expect_equal(target, current)
  }
)



# varDecomp$BDG #############################################################
target <- result_doIMRMC_target$varDecomp$BDG
current <- result_doIMRMC_current$varDecomp$BDG

# Current has new simpler format
# Reduce target to this format
target$Ustat$comp <- target$Ustat$comp$testA.testB
rownames(target$Ustat$comp) <- NULL
target$Ustat$coeff <- target$Ustat$coeff$testA.testB
rownames(target$Ustat$coeff) <- NULL
target$MLE$comp <- target$MLE$comp$testA.testB
rownames(target$MLE$comp) <- NULL
target$MLE$coeff <- target$MLE$coeff$testA.testB
rownames(target$MLE$coeff) <- NULL



# Current does not have columns modalityID.1 and modalityID.2
current$Ustat$comp <- deleteCol(current$Ustat$comp, "modalityID.1")
current$Ustat$comp <- deleteCol(current$Ustat$comp, "modalityID.2")
current$Ustat$coeff <- deleteCol(current$Ustat$coeff, "modalityID.1")
current$Ustat$coeff <- deleteCol(current$Ustat$coeff, "modalityID.2")
current$MLE$comp <- deleteCol(current$MLE$comp, "modalityID.1")
current$MLE$comp <- deleteCol(current$MLE$comp, "modalityID.2")
current$MLE$coeff <- deleteCol(current$MLE$coeff, "modalityID.1")
current$MLE$coeff <- deleteCol(current$MLE$coeff, "modalityID.2")



# Current coefficients yield the covariance.
# Target coefficients yield 2*covariance.
current$Ustat$coeff[3, ] <- 2 * current$Ustat$coeff[3, ]
current$MLE$coeff[3, ] <- 2 * current$MLE$coeff[3, ]



# R code has 7 digits significance
# Java code has 6 digits significance
# The reason is that the Java function reads and writes files.
testthat::test_that(
  "doIMRMC varcomp-BDG output does not change", {
    testthat::expect_equal(target, current,tolerance = 1e-4)
  }
)



# varDecomp$BCK #############################################################
target <- result_doIMRMC_target$varDecomp$BCK
current <- result_doIMRMC_current$varDecomp$BCK



# Current has new simpler format
# Reduce target to this format
target$Ustat$comp <- target$Ustat$comp$testA.testB
rownames(target$Ustat$comp) <- NULL
target$Ustat$coeff <- target$Ustat$coeff$testA.testB
rownames(target$Ustat$coeff) <- NULL
target$MLE$comp <- target$MLE$comp$testA.testB
rownames(target$MLE$comp) <- NULL
target$MLE$coeff <- target$MLE$coeff$testA.testB
rownames(target$MLE$coeff) <- NULL



# Current does not have columns modalityID.1 and modalityID.2
current$Ustat$comp <- deleteCol(current$Ustat$comp, "modalityID.1")
current$Ustat$comp <- deleteCol(current$Ustat$comp, "modalityID.2")
current$Ustat$coeff <- deleteCol(current$Ustat$coeff, "modalityID.1")
current$Ustat$coeff <- deleteCol(current$Ustat$coeff, "modalityID.2")
current$MLE$comp <- deleteCol(current$MLE$comp, "modalityID.1")
current$MLE$comp <- deleteCol(current$MLE$comp, "modalityID.2")
current$MLE$coeff <- deleteCol(current$MLE$coeff, "modalityID.1")
current$MLE$coeff <- deleteCol(current$MLE$coeff, "modalityID.2")



# Current coefficients yield the covariance.
# Target coefficients yield 2*covariance.
current$Ustat$coeff[3, ] <- 2 * current$Ustat$coeff[3, ]
current$MLE$coeff[3, ] <- 2 * current$MLE$coeff[3, ]



# R code has 7 digits significance
# Java code has 6 digits significance
# The reason is that the Java function reads and writes files.
testthat::test_that(
  "doIMRMC varcomp-BCK output does not change", {
    testthat::expect_equal(target, current,tolerance = 1e-4)
  }
)



# ROC ###############
target <- result_doIMRMC_target$ROC
current <- result_doIMRMC_current$ROC

# ROC per-reader visual test
plot(current$testA.reader2$fpf,
     current$testA.reader2$tpf, type = "l")
lines(target$`testA: reader2`$fpf,
      target$`testA: reader2`$tpf, lty = 2)

# ROC pooled-readers visual test
plot(current$testA.pooled$fpf,
     current$testA.pooled$tpf, type = "l")
lines(target$`testA: Pooled Average`$fpf,
      target$`testA: Pooled Average`$tpf, lty = 2)

# ROC diagonal average visual test
plot(current$testA.diagonalAvg$fpf,
     current$testA.diagonalAvg$tpf, type = "l")
lines(target$`testA: Diagonal Average`$fpf,
      target$`testA: Diagonal Average`$tpf, lty = 2)

# ROC vertical average visual test
plot(current$testA.verticalAvg$fpf,
     current$testA.verticalAvg$tpf, type = "l")
lines(target$`testA: Vertical Average`$fpf,
      target$`testA: Vertical Average`$tpf, lty = 2)

# ROC horizontal average test
plot(current$testA.horizontalAvg$fpf,
     current$testA.horizontalAvg$tpf, type = "l")
lines(target$`testA: Horizontal Average`$fpf,
      target$`testA: Horizontal Average`$tpf, lty = 2)

# plot(current$testB.reader1$fpf,
#      current$testB.reader1$tpf)
# lines(current$testB.reader2$fpf,
#      current$testB.reader2$tpf)
# lines(current$testB.reader3$fpf,
#       current$testB.reader3$tpf)
# lines(current$testB.reader4$fpf,
#       current$testB.reader4$tpf)
