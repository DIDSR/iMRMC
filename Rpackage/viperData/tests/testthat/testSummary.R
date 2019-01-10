library("viperData")
library("iMRMC")
library("testthat")

context("viperSummary data")

print(Sys.getlocale(category = "LC_ALL"))

# Re-Analyze the VIPER data
viperObs <- viperData::viperObs455
viperSummary <- viperData::doViperSummary(viperObs)

# Below we will compare the analysis above with the archived data
viperSummaryOrig <- viperData::viperSummary455

for (i in 1:length(viperSummary)) {

  # Compare desc ####
  print(viperSummary[[i]]$desc)
  test_that(
    "we are comparing same studies", {
      expect_equal(viperSummaryOrig[[i]]$desc, viperSummary[[i]]$desc)
    }
  )

  # Compare studyDesign ####
  print("Test that study design info doesn't change")
  test_that(
    "study design info doesn't change", {
      expect_equal(viperSummaryOrig[[i]]$studyDesign, viperSummary[[i]]$studyDesign)
    }
  )

  # Compare MRMC analyses of AUC ####
  print("Test that doIMRMC output doesn't change")
  # Remove columns that should not be compared
  aucOrig <- viperSummaryOrig[[i]]$iMRMC$auc
  aucOrig$perReader <- subset(aucOrig$perReader, select = -c(date, iMRMCversion))
  aucOrig$Ustat <- subset(aucOrig$Ustat, select = -c(date, iMRMCversion))
  aucOrig$MLEstat <- subset(aucOrig$MLEstat, select = -c(date, iMRMCversion))
  # Remove columns that should not be compared
  auc <- viperSummary[[i]]$iMRMC$auc
  auc$perReader <- subset(auc$perReader, select = -c(date, iMRMCversion))
  auc$Ustat <- subset(auc$Ustat, select = -c(date, iMRMCversion))
  auc$MLEstat <- subset(auc$MLEstat, select = -c(date, iMRMCversion))
  test_that(
    "Component ROC: Names: 42 string matches", {
      expect_equal(names(aucOrig$ROC), names(auc$ROC))
    }
  )
  test_that(
    "doIMRMC output doesn't change", {
      expect_equal(aucOrig, auc)
    }
  )

  # Compare MRMC analyses ofTPF ####
  # Remove columns that should not be compared
  tpfOrig <- viperSummaryOrig[[i]]$iMRMC$tpf
  tpfOrig$perReader <- subset(tpfOrig$perReader, select = -c(date, iMRMCversion))
  tpfOrig$Ustat <- subset(tpfOrig$Ustat, select = -c(date, iMRMCversion))
  tpfOrig$MLEstat <- subset(tpfOrig$MLEstat, select = -c(date, iMRMCversion))
  # Remove columns that should not be compared
  tpf <- viperSummary[[i]]$iMRMC$tpf
  tpf$perReader <- subset(tpf$perReader, select = -c(date, iMRMCversion))
  tpf$Ustat <- subset(tpf$Ustat, select = -c(date, iMRMCversion))
  tpf$MLEstat <- subset(tpf$MLEstat, select = -c(date, iMRMCversion))
  test_that(
    "doIMRMC output doesn't change", {
      expect_equal(tpfOrig, tpf)
    }
  )

  # Compare MRMC analyses of TNF ####
  # Remove columns that should not be compared
  tnfOrig <- viperSummaryOrig[[i]]$iMRMC$tnf
  tnfOrig$perReader <- subset(tnfOrig$perReader, select = -c(date, iMRMCversion))
  tnfOrig$Ustat <- subset(tnfOrig$Ustat, select = -c(date, iMRMCversion))
  tnfOrig$MLEstat <- subset(tnfOrig$MLEstat, select = -c(date, iMRMCversion))
  # Remove columns that should not be compared
  tnf <- viperSummary[[i]]$iMRMC$tnf
  tnf$perReader <- subset(tnf$perReader, select = -c(date, iMRMCversion))
  tnf$Ustat <- subset(tnf$Ustat, select = -c(date, iMRMCversion))
  tnf$MLEstat <- subset(tnf$MLEstat, select = -c(date, iMRMCversion))
  test_that(
    "doIMRMC output doesn't change", {
      expect_equal(tnfOrig, tnf)
    }
  )

}

