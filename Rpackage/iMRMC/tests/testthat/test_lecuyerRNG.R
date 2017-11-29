library(testthat)
library(iMRMC)

context("uStats21Diff")

init.lecuyerRNG()

test_that(
  ".Random.seed is set to expected result", {
    expect_equal(.Random.seed[1], 407)
    expect_equal(.Random.seed[2], 4246464)
    expect_equal(.Random.seed[7], 594164961)
  }
)

