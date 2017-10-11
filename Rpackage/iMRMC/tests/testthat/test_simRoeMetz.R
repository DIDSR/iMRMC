library(testthat)
library(parallel)
library(iMRMC)

context("simRoeMetz")

init.lecuyerRNG()

# Create an MRMC data frame
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

result.iMRMC <- doIMRMC(df.MRMC)

# Split the data into four pieces, not including truth
df.MRMC.Apos <- droplevels(df.MRMC[grepl("pos", df.MRMC$caseID) & grepl("A", df.MRMC$modalityID), ])
df.MRMC.Aneg <- droplevels(df.MRMC[grepl("neg", df.MRMC$caseID) & grepl("A", df.MRMC$modalityID), ])
df.MRMC.Bpos <- droplevels(df.MRMC[grepl("pos", df.MRMC$caseID) & grepl("B", df.MRMC$modalityID), ])
df.MRMC.Bneg <- droplevels(df.MRMC[grepl("neg", df.MRMC$caseID) & grepl("B", df.MRMC$modalityID), ])

print("")
cat("modality A mean shift, neg = ", mean(df.MRMC.Aneg$score), "\n")
cat("modality A mean shift, pos = ", mean(df.MRMC.Apos$score), "\n")
cat("modality B mean shift, neg = ", mean(df.MRMC.Bneg$score), "\n")
cat("modality B mean shift, pos = ", mean(df.MRMC.Bpos$score), "\n")

test_that(
  "sim.gRoeMetz does not change", {
    expect_equal(mean(df.MRMC.Aneg$score), -0.1642567, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Apos$score),  1.012758, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Bneg$score), -0.07882189, tolerance = 1e-6)
    expect_equal(mean(df.MRMC.Bpos$score),  1.122282, tolerance = 1e-6)
  }
)

test_that(
  "doIMRMC does not change", {
    expect_equal(result.iMRMC$Ustat$AUCA[1], 0.78959, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$AUCA[2], 0.79517, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$varAUCA[1], 0.001558203, tolerance = 1e-9)
    expect_equal(result.iMRMC$Ustat$varAUCA[2], 0.001514700, tolerance = 1e-9)
    expect_equal(result.iMRMC$Ustat$varAUCAminusAUCB[3], 0.0008030667, tolerance = 1e-9)

    expect_equal(result.iMRMC$Ustat$dfBDG[1], 18.09487, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$dfBDG[2], 18.05763, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$dfBDG[3], 48.47472, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$pValueBDG[1], 8.225544e-07, tolerance = 1e-12)
    expect_equal(result.iMRMC$Ustat$pValueBDG[2], 5.196685e-07, tolerance = 1e-12)
    expect_equal(result.iMRMC$Ustat$pValueBDG[3], 8.447327e-01, tolerance = 1e-07)
    expect_equal(result.iMRMC$Ustat$botCIBDG[1], 0.70665804, tolerance = 1e-8)
    expect_equal(result.iMRMC$Ustat$botCIBDG[2], 0.71340391, tolerance = 1e-8)
    expect_equal(result.iMRMC$Ustat$botCIBDG[3], -0.06255824, tolerance = 1e-8)

    expect_equal(result.iMRMC$Ustat$dfHillis[1], 18.27544, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$dfHillis[2], 18.24291, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$dfHillis[3], 54.31726, tolerance = 1e-5)
    expect_equal(result.iMRMC$Ustat$pValueHillis[1], 8.225544e-07, tolerance = 1e-12)
    expect_equal(result.iMRMC$Ustat$pValueHillis[2], 5.196685e-07, tolerance = 1e-12)
    expect_equal(result.iMRMC$Ustat$pValueHillis[3], 8.439013e-01, tolerance = 1e-07)
    expect_equal(result.iMRMC$Ustat$botCIHillis[1], 0.70665804, tolerance = 1e-8)
    expect_equal(result.iMRMC$Ustat$botCIHillis[2], 0.71340391, tolerance = 1e-8)
    expect_equal(result.iMRMC$Ustat$botCIHillis[3], -0.06112231, tolerance = 1e-8)

    expect_equal(result.iMRMC$perReader$varAUCA[1], 0.00161429, tolerance = 1e-8)
    expect_equal(result.iMRMC$perReader$varAUCA[10], 0.00075093, tolerance = 1e-8)

    expect_equal(result.iMRMC$MLEstat$botCIBDG[1], 0.70696872, tolerance = 1e-8)
    expect_equal(result.iMRMC$MLEstat$botCIBDG[2], 0.71362848, tolerance = 1e-8)
    expect_equal(result.iMRMC$MLEstat$botCIBDG[3], -0.06336447, tolerance = 1e-8)

    expect_equal(result.iMRMC$ROC$`modalityA: Diagonal Average`$tpf[2], 0.0141, tolerance = 1e-4)
    expect_equal(result.iMRMC$ROC$`modalityA: Diagonal Average`$fpf[4], 0.002, tolerance = 1e-4)

    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "N"], 0.021898200, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "D"], 0.024511600, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "ND"], 6.23156e-03, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "R"], 0.010478300, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "NR"], 2.17010e-02, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "DR"], 2.33584e-02, tolerance = 1e-9)
    expect_equal(result.iMRMC$varDecomp$BCK$Ustat$comp$modalityA.modalityB["modalityA", "RND"], 5.95167e-02, tolerance = 1e-9)
  }
)