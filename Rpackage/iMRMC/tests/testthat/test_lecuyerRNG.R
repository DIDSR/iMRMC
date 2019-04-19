library(testthat)
library(iMRMC)

context("uStats21Diff")

init.lecuyerRNG()

test_that(
  ".Random.seed is set to expected result", {
    suppressWarnings(RNGversion("3.5.0"))
    expect_equal(.Random.seed[1], 403)
    expect_equal(.Random.seed[2], 624)
    expect_equal(.Random.seed[7], -675253042)
  }
)

