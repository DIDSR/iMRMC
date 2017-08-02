library(LaplacesDemon)

#' Numerically calculate the mean of model A. GLMM with
#'   fixed mean
#'   reader and case cross-correlated effects
#'   a reader by case interaction term
#'
#' @param simModelAconfig list of simulation parameters:
#'          Nr         number of readers
#'          Nc         number of cases
#'          mu         fixed effect: mean in logistic space
#'          var_c      variance of random case effect
#'          var_r      variance of random reader effect
#'          var_rc     variance of randome reader by case effect
#'
#' @param nSamples = 400 (default) controls precision of integration
#' @param showPlots = FALSE (default) If TRUE, function shows plots of integrand
#'                    in probability space and logistic space.
#'
#' @return Mean of model A by numerical integration
#'
# @import LaPlacesDemon
#'
#' @export
#'
# @examples
meanModelA <- function(simModelAconfig, showPlots = FALSE, nSamples = 400) {

#

  # Create a vector that will sample a normal distribution out to six standard errors
  x1 <- ((1:(nSamples + 1)) - nSamples/2 - 1) / (nSamples/2) * 6
  dx1 <- x1[2] - x1[1]

  p <- simModelAconfig$p
  var_r <- simModelAconfig$var_r
  var_c <- simModelAconfig$var_c
  var_rc <- simModelAconfig$var_rc

  # In logit space:
  # yModel = the pdf of the performance of a random reader reading a random case
  mean_Model <- LaplacesDemon::logit(p)
  var_Model <- var_r + var_c + var_rc
  xModel <- x1 * sqrt(var_Model) + mean_Model
  dxModel <- xModel[2] - xModel[1]
  yModel <- dnorm(xModel, mean = mean_Model, sd = sqrt(var_Model))
  # plot(xModel, yModel, main = "Distribution of logit(pij)")

  # The reader- and case-averaged performance
  #  given the model parameters
  integrand = yModel * LaplacesDemon::invlogit(xModel)
  # plot(xModel, integrand)
  meanP <- sum(integrand * dxModel)

  if (showPlots) {

    # Create a vector that will sample [0,1]
    x2 <- ((1:nSamples) - 0.5)/nSamples
    dx2 <- x2[2] - x2[1]

    integrand2 <- dnorm(LaplacesDemon::logit(x2), mean = mean_Model, sd = sqrt(var_Model))
    # plot(x2, integrand2)
    integrand2 <- integrand2 / x2 / (1 - x2)
    # plot(x2, integrand2)
    meanP2 <- sum(integrand2*x2*dx2 )

    par(mfrow = c(2,2))

    plot(x2, integrand2, main = "Distribution of pij")
    plot.new()
    plot(LaplacesDemon::logit, ylim = c(-4, 4))
    plot(yModel, xModel, main = "Distribution of logit(pij)", ylim = c(-4,4))

    par(mfrow = c(1,1))

  }

  return(meanP)

}
