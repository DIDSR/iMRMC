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
  modalitiesToCompare
) {

  if (length(modalitiesToCompare) != 2) {
    print(paste("length(modalitiesToCompare) =", length(modalitiesToCompare)))
    stop("ERROR: modalitiesToCompare should have 2 elements.")
  }
  mToCompare <- c(modalitiesToCompare, modalitiesToCompare)

  # The limits of agreement are determined by the MRMC moments of uStat11
  # when the kernel flag is 2.
  # The limits can also be determined from uStat11 when the kernel flag is 1,
  # but it is a little bit more complicated. Refer to validate laWRBM.R for info.
  result1 <- uStat11.conditionalD(
    df.input, keyColumns = keyColumns,
    modalitiesToCompare = mToCompare, kernelFlag = 2
  )

  mean <- result1$mean[1]
  var <- result1$var.1obs[1]

  bot <- mean - 2 * sqrt(var)
  top <- mean + 2 * sqrt(var)

  result2 <- data.frame(bot = bot, top = top, mean = mean, var = var)

  return(result2)

  #### Alternate method with identical result ####
  #### Uses uStat11 and the identity kernel
  result <- uStat11.jointD(
    df.input, keyColumns = keyColumns,
    modalitiesToCompare = modalitiesToCompare, kernelFlag = 1)

  moments <- result$moments

  var.Arc = moments$c1r1[1] - moments$c0r0[1]
  var.Brc = moments$c1r1[2] - moments$c0r0[2]
  cov.Ar1cBr1c <- moments$c1r1[3] - moments$c0r0[3]

  var.Ar1cminusBr1c <- var.Arc + var.Brc - 2 * cov.Ar1cBr1c

  print(result2$var)
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