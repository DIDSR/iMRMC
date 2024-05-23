# In this test we compare doIMRMC_java that uses the java engine to
# to the new doIMRMC that uses only R code.
# This simulation starts with the default configuration. 
# To test the robustness of the software, we change the data as follows:
# We remove modality `testB` from the dataset so it has one modality.
# We delete some data to create data that is not fully crossed.
# We rename and reorder some data.

library(testthat)
library(iMRMC)

testthat::context("doIMRMC_R1")

# Simulate data ###############################################################

# initialize the random number generator
init.lecuyerRNG(stream = 1)

# Create a sample configuration file
config <- sim.gRoeMetz.config()

# Simulate an MRMC ROC data set
dFrame.imrmc <- sim.gRoeMetz(config)
dFrame.imrmc$modalityID <- as.character(dFrame.imrmc$modalityID)
dFrame.imrmc$readerID <- as.character(dFrame.imrmc$readerID)
dFrame.imrmc$caseID <- as.character(dFrame.imrmc$caseID)

# Remove all data from modality testB
dFrame.imrmc <- dFrame.imrmc[dFrame.imrmc$modalityID != "testB", ]

# Delete some data to create data that is not fully crossed.
del = sample(100:880,100)
dFrame.imrmc <- dFrame.imrmc[-del, ]

# Rename some negative cases and resort
# This puts the truth and reader data of the renamed cases
# at the bottom of the data frame
for (i in 1:nrow(dFrame.imrmc)) {
  dFrame.imrmc[i, "caseID"] <-
    sub("negCase1", "x-negCase1", dFrame.imrmc[i, "caseID"])
}
index <- order(dFrame.imrmc[, "caseID"])
dFrame.imrmc <- dFrame.imrmc[index, ]



# Process data ###############################################################

# Analyze the MRMC ROC data using new R code
# Processing time before BDG
# user  system elapsed 
# 3.36    0.81    4.17
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
  targetTest1 <- doIMRMC_java(dFrame.imrmc)
  save(targetTest1,
       file = file.path("tests", "testthat", "test_doIMRMC_R1.rda"))
  
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
    pattern = "test_doIMRMC_R1.rda")

  if (length(temp) > 0) {
    if (temp == "test_doIMRMC_R1.rda")
      load(file.path("tests", "testthat", "test_doIMRMC_R1.rda"))
  }
  
  # The working directory when running in test mode is the tests/testthat directory
  temp <- list.files(
    path = ".",
    pattern = "test_doIMRMC_R1.rda")

  if (length(temp) > 0) {
    if ( temp == "test_doIMRMC_R1.rda")
      load(file.path(".", "test_doIMRMC_R1.rda"))
  }  
  
}
result_doIMRMC_target <- targetTest1



# perReader output full #############################################
# The target result has 28 variables/columns
# Variables 15 through 28 of target results are NA
target <- result_doIMRMC_target$perReader[,4:14]
# We have to remove readers that have less than 2 cases to achieve agreement
index.delete <- target$N0 >= 2
target <- target[index.delete, ]
index.delete <- target$N1 >= 2
target <- target[index.delete, ]
rownames(target) <- NULL
# When there is only one modality, 
# we have to change the data type of these columns to achieve agreement
# (This is not necessary when nM = 2)
target$AUCB <- as.numeric(target$AUCB)
target$varAUCB <- as.numeric(target$varAUCB)
target$AUCAminusAUCB <- as.numeric(target$AUCAminusAUCB)
target$varAUCAminusAUCB <- as.numeric(target$varAUCAminusAUCB)

current <- result_doIMRMC_current$perReader


# Java code has 8 digits significance
# R code has 10 digits significance
# The reason is that the Java function reads and writes files.  
testthat::test_that(
  "doIMRMC perReader output does not change", {
    testthat::expect_equal(target, current,tolerance = 1e-6)
  }
)



# Ustat output ###############
target = result_doIMRMC_target$Ustat[,4:24]
# When there is only one modality, 
# we have to change the data type of these columns to achieve agreement
# (This is not necessary when nM = 2)
target$AUCB <- as.numeric(target$AUCB)
target$varAUCB <- as.numeric(target$varAUCB)

current = result_doIMRMC_current$Ustat

testthat::test_that(
  "doIMRMC Ustat output does not change", {
    testthat::expect_equal(target, current)
  }
)



# MLEstat output ###############
target = result_doIMRMC_target$MLEstat[,4:24]
# When there is only one modality, 
# we have to change the data type of these columns to achieve agreement
# (This is not necessary when nM = 2)
target$AUCB <- as.numeric(target$AUCB)
target$varAUCB <- as.numeric(target$varAUCB)

current = result_doIMRMC_current$MLEstat

testthat::test_that(
  "doIMRMC Ustat output does not change", {
    testthat::expect_equal(target, current)
  }
)



# varDecomp$BDG #############################################################
target <- result_doIMRMC_target$varDecomp$BDG
current <- result_doIMRMC_current$varDecomp$BDG



# Current has new simpler format
# Reduce target to this format
target$Ustat$comp <- target$Ustat$comp$testA.NO_MOD[1, ]
rownames(target$Ustat$comp) <- NULL
target$Ustat$coeff <- target$Ustat$coeff$testA.NO_MOD[1, ]
rownames(target$Ustat$coeff) <- NULL
target$MLE$comp <- target$MLE$comp$testA.NO_MOD[1, ]
rownames(target$MLE$comp) <- NULL
target$MLE$coeff <- target$MLE$coeff$testA.NO_MOD[1, ]
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
target$Ustat$comp <- target$Ustat$comp$testA.NO_MOD[1, ]
rownames(target$Ustat$comp) <- NULL
target$Ustat$coeff <- target$Ustat$coeff$testA.NO_MOD[1, ]
rownames(target$Ustat$coeff) <- NULL
target$MLE$comp <- target$MLE$comp$testA.NO_MOD[1, ]
rownames(target$MLE$comp) <- NULL
target$MLE$coeff <- target$MLE$coeff$testA.NO_MOD[1, ]
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



# R code has 7 digits significance
# Java code has 6 digits significance
# The reason is that the Java function reads and writes files.  
testthat::test_that(
  "doIMRMC varcomp-BDG output does not change", {
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



