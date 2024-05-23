# In this test we test the roc2binary function graphically
# This is the default configuration from the simulation software
# of 2 modalities and 5 readers, fully crossed.

library(testthat)

testthat::context("roc2binary")

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


# Convert ROC MRMC data to TPF and FPF data frames
result <- roc2binary(dFrame.imrmc, threshold = 0.2) 

# Analyze all data using doIMRMC
all_result <- doIMRMC(dFrame.imrmc)
 
# Analyze TPF and FPF data using doIMRMC
tpf_result <- doIMRMC(result$df.tpf)
fpf_result <- doIMRMC(result$df.fpf)

# Isolate ROC results
tpf_roc <- tpf_result$ROC
fpf_roc <- fpf_result$ROC

# Create plot 
# Overlay TPF and FPF stepwise curves over pooled ROC 
# All for same modality and reader

hist(dFrame.imrmc$score)

plot(all_result$ROC$testA.reader1$fpf, 
     all_result$ROC$testA.reader1$tpf)
lines(tpf_roc$testA.reader1$fpf,
     tpf_roc$testA.reader1$tpf, type = "l", col = "red")
lines(fpf_roc$testA.reader1$fpf,
      fpf_roc$testA.reader1$tpf, lty = 2, col = "blue")

# For pooled modality results
plot(all_result$ROC$testB.pooled$fpf, 
     all_result$ROC$testB.pooled$tpf)
lines(tpf_roc$testB.pooled$fpf,
      tpf_roc$testB.pooled$tpf, type = "l", col = "red")
lines(fpf_roc$testB.pooled$fpf,
      fpf_roc$testB.pooled$tpf, lty = 2, col = "blue")


# For diagonal average modality results
plot(all_result$ROC$testB.diagonalAvg$fpf, 
     all_result$ROC$testB.diagonalAvg$tpf)
lines(tpf_roc$testB.diagonalAvg$fpf,
      tpf_roc$testB.diagonalAvg$tpf, type = "l", col = "red")
lines(fpf_roc$testB.diagonalAvg$fpf,
      fpf_roc$testB.diagonalAvg$tpf, lty = 2, col = "blue")

