# In this test we compare the output of current R markdown files
# with target saved versions


library(testthat)
library(rmarkdown)
library(knitr)

testthat::context("test_markdown")



# Initialize render function ##########
validate_rmarkdown <- function(file_path) {
  tryCatch({
    rmarkdown::render(file_path)
    TRUE
  }, error = function(e) {
    FALSE
  })
}



# Initialize package root directory name ##########
rootPackage <- system.file(package = "iMRMC")



# Non-inferiority-by-iMRMC ##########

# R markdown file to compile
fileName.rmd <- file.path(
  rootPackage,
  "extra", "000-non-inferiority-by-iMRMC",
  "Non-inferiority-by-iMRMC.rmd"
)

# Target markdown file
fileName.md.target <- file.path(
  rootPackage,
  "extra", "000-non-inferiority-by-iMRMC",
  "Non-inferiority-by-iMRMC-forTest.md"
)

# Current markdown file
fileName.md.current <- file.path(
  rootPackage,
  "extra", "000-non-inferiority-by-iMRMC",
  "Non-inferiority-by-iMRMC.md"
)

# Compile R markdown file
validate_rmarkdown(fileName.rmd)

# Compare current to target
test_that("Non-inferiority-by-iMRMC Markdown output has not changed", {
  expect_equal(
    readLines(fileName.md.target),
    readLines(fileName.md.current)
  )
})


