library(testthat)
library(iMRMC)

context("doIMRMC given filename")

#fileName <- file.path("data-raw", "inputAUC.imrmc")
fileName <- "inputAUC.imrmc"
if (!file.exists(fileName)) {
  fileName <- file.path("tests", "testthat", fileName)
}
result.auc <- doIMRMC(fileName = fileName)

test_that(
  "doIMRMC does not change", {
    expect_equal(result.auc$Ustat$AUCA[1], 0.6920837, tolerance = 1e-5)
    expect_equal(result.auc$Ustat$AUCA[2], 0.7090612, tolerance = 1e-5)
    expect_equal(result.auc$Ustat$varAUCA[1], 0.0008662066, tolerance = 1e-9)
    expect_equal(result.auc$Ustat$varAUCA[2], 0.0008119780, tolerance = 1e-9)
    expect_equal(result.auc$Ustat$varAUCAminusAUCB[3], 0.0006429189, tolerance = 1e-9)

    expect_equal(result.auc$Ustat$dfBDG[1], 66.01861, tolerance = 1e-5)
    expect_equal(result.auc$Ustat$dfBDG[2], 69.87244, tolerance = 1e-5)
    expect_equal(result.auc$Ustat$dfBDG[3], 37.40402, tolerance = 1e-5)
    expect_equal(result.auc$Ustat$pValueBDG[1], 6.732859e-11, tolerance = 1e-12)
    expect_equal(result.auc$Ustat$pValueBDG[2], 2.189360e-13, tolerance = 1e-12)
    expect_equal(result.auc$Ustat$pValueBDG[3],  5.072895e-01, tolerance = 1e-07)
    expect_equal(result.auc$Ustat$botCIBDG[1], 0.63439921, tolerance = 1e-8)
    expect_equal(result.auc$Ustat$botCIBDG[2], 0.65321155, tolerance = 1e-8)
    expect_equal(result.auc$Ustat$botCIBDG[3], -0.06835332, tolerance = 1e-8)
  }
)
