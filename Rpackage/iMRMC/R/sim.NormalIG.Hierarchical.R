#' Simulate an MRMC data set comparing two modalities by a hierarchical model
#' 
#' @description
#' This procedure simulates an MRMC data set for an MRMC agreement study comparing two 
#' modalities. It is a hierarchical model that consists of two interaction terms: reader-case
#' interaction and modality-reader-case-replicate interaction. Both interaction
#' terms are conditionally normally distributed, with the case(-related) factor contributing 
#' to the conditional mean and the reader(-related) factor contributing to the conditional 
#' variance. The case effect is normally distributed, while the reader effect is
#' an inverse-gamma.
#' 
#' The Hierarchical Inverse-Gamma model is described in this paper:
#' 
#' \itemize{
#'   \item S. Wen and B. D. Gallas,
#'     “Three-Way Mixed Effect ANOVA to Estimate MRMC Limits of Agreement,”
#'     \emph{Statistics in Biopharmaceutical Research}, \strong{14}, pp. 532–541, 2022,
#'     \doi{10.1080/19466315.2022.2063169}
#' }
#'
#' @details 
#' The model has the following structure:
#' X.ijkl = mu + m.i + RC.jk + mRCE.ijkl
#' \itemize{
#'   \item mu = grand mean
#'   \item m.i = modalities (levels: A and B)
#'   \item RC.jk given R.j,C.k ~ N(C.k, R.j) reader-case interaction term
#'   \item mRCE.ijkl given mR.ij,mC.ik ~ N(mC.ik, mR.ij) modality-reader-case-replicate term
#'   \item C.k and mC.ik are Normal/beta distributed
#'   \item R.j and mR.ij are Inverse-Gamma distributed
#' }
#'
#' @param config [list] of simulation parameters:
#' \itemize{
#'   \item Experiment labels and size
#'   \itemize{
#'     \item \code{modalityID}: [vector] label modality A and B.
#'     \item \code{nR}: [num] number of readers
#'     \item \code{nC}: [num] number of cases
#'     \item \code{C_dist}: [chr] distribution of the case. Default \code{C_dist="normal"}
#'   }
#'   \item Mean and fixed effects:
#'   \itemize{
#'     \item \code{mu}: [num] grand mean
#'     \item \code{tau_A}: [num] modality A
#'     \item \code{tau_B}: [num] modality B
#'   }
#'   \item Reader-case interaction term
#'   \itemize{
#'     \item \code{sigma_C}: [num] std of case factor (if \code{C_dist="normal"})
#'     \item \code{a_C}:     [num] alpha for distribution of case (if \code{C_dist="beta"})
#'     \item \code{b_C}:     [num] beta for distribution of case (if \code{C_dist="beta"})
#'     \item \code{alpha_R}: [num] shape parameter for reader
#'     \item \code{beta_R}:  [num] scale parameter for reader
#'   }
#'   \item Modality-reader-case-replicate interaction term for modality A
#'     \itemize{
#'     \item \code{sigma_C.A}: [num] std of case factor (if \code{C_dist="normal"})
#'     \item \code{a_C.A}:     [num] alpha for distribution of case (if \code{C_dist="beta"})
#'     \item \code{b_C.A}:     [num] beta for distribution of case (if \code{C_dist="beta"})
#'     \item \code{alpha_R.A}: [num] shape parameter for reader
#'     \item \code{beta_R.A}:  [num] scale parameter for reader
#'   }
#'   \item Modality-reader-case-replicate interaction term for modality B
#'     \itemize{
#'     \item \code{sigma_C.B}: [num] std of case factor (if \code{C_dist="normal"})
#'     \item \code{a_C.B}:     [num] alpha for distribution of case (if \code{C_dist="beta"})
#'     \item \code{b_C.B}:     [num] beta for distribution of case (if \code{C_dist="beta"})
#'     \item \code{alpha_R.B}: [num] shape parameter for reader
#'     \item \code{beta_R.B}:  [num] scale parameter for reader
#'   }
#'   \item Scales for the case related terms and interaction terms
#'     \itemize{
#'       \item \code{C_scale}:      [num] weight for the case factor
#'       \item \code{RC_scale}:     [num] weight for the reader-case interaction term
#'       \item \code{tauC_scale}:   [num] weight for the modality-case term
#'       \item \code{tauRCE_scale}: [num] weight for the modality-reader-case-replicate interaction term
#'   }
#' }
#' @param R [vector] of size \code{nR} of reader factors pre-generated from
#'   a gamma(\code{alpha_R}, \code{beta_R}) distribution 
#'   to allow the reader factor to be fixed across multiple simulations.
#'   Default \code{= NULL}  
#' 
#' @param AR [vector] of size \code{nR} of modality-reader interaction terms
#'   pre-generated from a gamma(\code{alpha_R.A}, \code{beta_R.A}) distribution 
#'   to allow the modality-reader interaction terms to be
#'   fixed across multiple simulations the modality-reader interaction.
#'   Default \code{= NULL}
#' 
#' @param BR [vector] of size \code{nR} of modality-reader interaction terms
#'   pre-generated from a gamma(\code{alpha_R.B}, \code{beta_R.B}) distribution 
#'   to allow the modality-reader interaction terms to be
#'   fixed across multiple simulations the modality-reader interaction.
#'   Default \code{= NULL}
#' 
#' @param is.within [bol] whether the data are within-modality (A==B).
#'   In this case the modality-reader and modality-case interaction terms 
#'   will be the same.
#'   Default \code{= FALSE}
#'
#' @return df   [data.frame] with nR x nC x 2 rows including
#' \itemize{
#'   \item readerID: [Factor] w/ nR levels "reader1", "reader2", ...
#'   \item caseID: [Factor] w/ nC levels "case1", "case2", ...
#'   \item modalityID: [Factor] w/ 2 levels "testA" and "testB"
#'   \item score: [num] reader score
#' }
#' 
#' @importFrom stats rbeta rgamma rnorm
#' 
#' @export
#'
# @examples
# # Create a sample configuration object
# config <- sim.NormalIG.Hierarchical.config()
# # Simulate an MRMC ROC data set
# dFrame <- sim.NormalIG.Hierarchical(config)


sim.NormalIG.Hierarchical = function(config,R = NULL,AR = NULL,BR = NULL,is.within=FALSE) {
  
  # Initialize ----
  nR = config$nR
  nC = config$nC
  modalityID = config$modalityID
  C_dist = config$C_dist
  
  mu = config$mu
  tau_A = config$tau_A
  tau_B = config$tau_B
  
  alpha_R = config$alpha_R
  alpha_R.A = config$alpha_R.A
  alpha_R.B = config$alpha_R.B
  
  beta_R = config$beta_R
  beta_R.A = config$beta_R.A
  beta_R.B = config$beta_R.B
  
  C_scale = config$C_scale
  RC_scale = config$RC_scale
  tauC_scale = config$tauC_scale
  tauRCE_scale = config$tauRCE_scale
  
  
  # C and tauC, different case distribution 
  if(C_dist == 'normal'){
    
    C = matrix(rep(rnorm(nC,0,config$sigma_C),nR),nR,nC,byrow =TRUE)
    ## Mod A/ first replicate
    tauC.A = matrix(rep(rnorm(nC,0,config$sigma_C.A),nR),nR,nC,byrow =TRUE)
    ## Mod B/ second replicate 
    if(is.within){
      tauC.B = tauC.A
    }else{
      tauC.B = matrix(rep(rnorm(nC,0,config$sigma_C.B),nR),nR,nC,byrow =TRUE)
    }
        
  }else if(C_dist == 'beta'){
    
    C = matrix(rep(rbeta(nC,config$a_C,config$b_C),nR),nR,nC,byrow =TRUE)
    ## Mod A/ first replicate
    tauC.A = matrix(rep(rbeta(nC,config$a_C.A,config$b_C.A),nR),nR,nC,byrow =TRUE)
    ## Mod B/ second replicate 
    if(is.within){
      tauC.B = tauC.A
    }else{
      tauC.B = matrix(rep(rbeta(nC,config$a_C.B,config$b_C.B),nR),nR,nC,byrow =TRUE)
    }
  }
  
  # RC
  if(is.null(R)){
    R = rgamma(nR, alpha_R, rate = beta_R)
  }
  var_RC = matrix(rep(R, nC), nR, nC)
  
  RC = apply(1/sqrt(var_RC), c(1,2), rnorm, n=1, mean=0) # Normal variable with mean 0 and variance inv-Gamma distributed
  
  
  # tauRCE
  ## Mod A/ first replicate
  if(is.null(AR)){
    AR = rgamma(nR, alpha_R.A, rate = beta_R.A)
  }
  var_tauRCE.A = matrix(rep(AR, nC), nR, nC)
  
  tauRCE.A = apply(1/sqrt(var_tauRCE.A), c(1,2), rnorm, n= 1, mean= 0)
  
  ## Mod B/ second replicate 
  if(is.within){
    BR = AR
  }else{
    if(is.null(BR)){
      BR = rgamma(nR, alpha_R.B, rate = beta_R.B)
    }
  }

  var_tauRCE.B = matrix(rep(BR, nC), nR, nC)
  
  tauRCE.B = apply(1/sqrt(var_tauRCE.B), c(1,2), rnorm, n= 1, mean= 0)
  
  # Aggregate ----
  
  modA = mu + tau_A + C * C_scale + RC * RC_scale + tauC.A * tauC_scale + tauRCE.A * tauRCE_scale
  modB = mu + tau_B + C * C_scale + RC * RC_scale + tauC.B * tauC_scale + tauRCE.B * tauRCE_scale
  
  # Five column format ----
  
  df = as.data.frame(matrix(0,nrow=nR*nC*2,ncol=4))
  colnames(df) = c("caseID","readerID","modalityID","score")
  
  df$score = c(as.vector(t(modA)),as.vector(t(modB)))
  df$caseID = paste0("Case",rep(1:nC,nR*2))
  df$readerID = as.factor(paste0("reader",rep(rep(1:nR,each=nC),2)))
  df$modalityID = as.factor(rep(c(modalityID[1],modalityID[2]),each=nR*nC))
  
  
  return(df)
}

#' Create a configuration object for the sim.NormalIG.Hierarchical function
#'
#' @description
#' This function creates a configuration object that sets the parameters
#' for the Hierarchical Inverse-Gamma simulation model. The configuration
#' object is an to the \code{\link{sim.NormalIG.Hierarchical}} function.
#' 
#' @details If no arguments, this function returns a default simulation
#' configuration for the \code{\link{sim.NormalIG.Hierarchical}} function.
#'
#' @param nR [num] Number of readers. Default \code{nR = 5}
#' @param nC [num] Number of cases. Default \code{nC = 100}
#' @param modalityID [vector] List of modalityID. Default \code{modalityID = c("testA", "testA*")} 
#' @param C_dist [chr] Distribution of the case. Default \code{C_dist="normal"}
#' @param mu [num] grand mean. Default \code{mu = 0}
#' @param tau_A [num] modality A effect. Default \code{tau_A = 0}
#' @param tau_B [num] modality B effect. Default \code{tau_B = 0}
#' @param alpha_R [num] shape parameter for reader. Default \code{alpha_R = 10}
#' @param beta_R [num] scale parameter for reader. Default \code{beta = 1}
#' @param sigma_C [num] std of case factor (if \code{C_dist="normal"}). Default \code{sigma_C = 1}
#' @param a_C [num] alpha for distribution of case (if \code{C_dist="beta"}). Default \code{a_C = 0.8}
#' @param b_C [num] beta for distribution of case (if \code{C_dist="beta"}). Default \code{b_C = 3}
#' @param sigma_tauC [num] std of modality-case (if \code{C_dist="normal"}). Default \code{sigma_tauC = 1}
#' @param alpha_tauR [num] shape parameter for modality-reader. Default \code{alpha_tauR = 10}
#' @param beta_tauR [num] scale parameter for modality-reader. Default \code{beta_tauR = 1}
#' @param C_scale [num] weight for the case factor. Default \code{C_scale = 1}
#' @param RC_scale [num] weight for the reader-case interaction term. Default \code{RC_scale = 1}
#' @param tauC_scale [num] weight for the modality-case term. Default \code{tauC_scale = 1}
#' @param tauRCE_scale [num] weight for the modality-reader-case-replicate interaction term. Default \code{tauRCE_scale = 1}
#'
#' @return config [list] of input parameters for \code{\link{sim.NormalIG.Hierarchical}}.
#' 
#' @export
#'

sim.NormalIG.Hierarchical.config = function (nR = 5, nC = 100, modalityID = c("testA", "testA*"), C_dist = 'normal', 
                                        mu = 0, tau_A = 0, tau_B = 0, alpha_R = 10, beta_R = 1,
                                        sigma_C = 1, a_C = 0.8, b_C = 3, sigma_tauC = 1,
                                        alpha_tauR = 10, beta_tauR = 1,
                                        C_scale = 1, RC_scale = 1,tauC_scale = 1, tauRCE_scale = 1) 
{
  if (C_dist == 'normal'){
    config <- list(modalityID = modalityID, nR = nR, nC = nC,                           # sample size
                   C_dist = C_dist, mu = mu, tau_A = tau_A, tau_B = tau_B,                # distribution and mean
                   sigma_C = sigma_C, alpha_R = alpha_R, beta_R = beta_R,                 # parameter for [RC]
                   sigma_C.A = sigma_tauC, alpha_R.A = alpha_tauR, beta_R.A = beta_tauR,  # parameter for [tauRCE].A
                   sigma_C.B = sigma_tauC, alpha_R.B = alpha_tauR, beta_R.B = beta_tauR,  # parameter for [tauRCE].B
                   C_scale = C_scale, RC_scale = RC_scale,                                # scale for [RC]
                   tauC_scale = tauC_scale, tauRCE_scale = tauRCE_scale)                  # scale for [tauRCE]
    
  }else if(C_dist == 'beta'){
    config <- list(modalityID = modalityID, nR = nR, nC = nC,                             # sample size
                   C_dist = C_dist, mu = mu, tau_A = tau_A, tau_B = tau_B,                # distribution and mean
                   a_C = a_C, b_C = b_C, alpha_R = alpha_R, beta_R = beta_R,              # parameter for [RC]
                   a_C.A = a_C, b_C.A = b_C, alpha_R.A = alpha_tauR, beta_R.A = beta_tauR,# parameter for [tauRCE].A
                   a_C.B = a_C, b_C.B = b_C, alpha_R.B = alpha_tauR, beta_R.B = beta_tauR,# parameter for [tauRCE].B
                   C_scale = C_scale, RC_scale = RC_scale,                                # scale for [RC]
                   tauC_scale = tauC_scale, tauRCE_scale = tauRCE_scale)                  # scale for [tauRCE])
  }else{
    print(paste0("C_dist = ", C_dist))
    stop("ERROR: C_dist should be either normal or beta.")
  }
  
  return(config)
}
