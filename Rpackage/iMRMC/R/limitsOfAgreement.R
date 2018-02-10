#---- laWRBM ----
#' @title MRMC analysis of within-reader between-modality limits of agreement
#'
#' @details The core analysis is done by ustat11 with the difference kernel (2).
#'   This calculation can also be accomplished by ustat11 with the identity kernel (1),
#'   and the code to do that is provided after the return statement so it never gets exectuted.
#'
#' @export
#'
#' @rdname limitsOfAgreement
laWRBM <- function(
  df.input,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("modalityA", "modalityB")
) {

  # The limits of agreement are determined by the MRMC moments of uStat11
  # when the kernel flag is 2.
  # The limits can also be determined from uStat11 when the kernel flag is 1,
  # but it is a little bit more complicated. Refer to validate laWRBM.R for info.
  result <- uStat11.conditionalD(
    df.input, keyColumns = keyColumns,
    modalitiesToCompare = modalitiesToCompare, kernelFlag = 2
  )
  result2 <- result
  result2$var <- result2$var.1obs
  result2 <- result2[c("mean", "var", "nR", "nC")]

  return(result2)

  #### Alternate method with identical result ####
  #### Uses uStat11 and the identity kernel
  result <- uStat11.jointD(
    rbind(df.MRMC$testA.1, df.MRMC$testB.1),
    kernelFlag = 1,
    keyColumns = c("readerID", "caseID", "modalityID", "score"),
    modalitiesToCompare = c("testA", "testB"))

  moments <- result$moments

  var.Arc = moments$c1r1[1] - moments$c0r0[1]
  var.Brc = moments$c1r1[2] - moments$c0r0[2]
  cov.Ar1cBr1c <- moments$c1r1[3] - moments$c0r0[3]

  var.Ar1cminusBr1c <- var.Arc + var.Brc - 2 * cov.Ar1cBr1c

  print(result2)
  print(var.Ar1cminusBr1c)

  print("You should not reach this point because of a previous return statement.")
  browser()

  return(result2)

}

#---- laBRWM ----
#' @title MRMC analysis of within-reader between-modality limits of agreement
#'
#' @details The core analysis is done by ustat11 with the difference kernel (2).
#'   This calculation can also be accomplished by ustat11 with the identity kernel (1),
#'   and the code to do that is provided after the return statement so it never gets exectuted.
#'
#' @export
#'
#' @rdname limitsOfAgreement
laBRWM <- function(
  df.input,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("modalityA", "modalityB")
) {

  # The limits of agreement are determined by the MRMC moments of uStat11
  # when the kernel flag is 1.
  result <- uStat11.conditionalD(
    df.input, keyColumns = keyColumns,
    modalitiesToCompare = modalitiesToCompare, kernelFlag = 1
  )
  result2 <- result
  result2$var <- result2$var.1obs
  result2 <- result2[c("mean", "var", "nR", "nC")]

  return(result2)

}