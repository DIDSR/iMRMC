


## doROCxy ##############################################################
#' Create empirical ROC curve
#'
#' @param sa signal-absent scores
#' @param sp signal-present scores
#'
#' @return data frame of an ROC curve
#'  \itemize{
#'   \item{\code{fpf} False-positive fractions (1-specificity)}
#'   \item{\code{tpf} True-positive fractions (sensitivity)}
#'   \item{\code{threshold} Threshold corresponding to each fpf, tpf
#'   operating point}
#' }
#' 
#' @export
#'
#' @examples
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dFrame.imrmc <- sim.gRoeMetz(config)
#' # Isolate signal absent scores
#' indexSA <- grep("negCase", dFrame.imrmc$caseID)
#' sa <- dFrame.imrmc[indexSA, ]$score
#' # Isolate signal present scores
#' indexSP <- grep("posCase", dFrame.imrmc$caseID)
#' sp <- dFrame.imrmc[indexSP, ]$score
#' # Compute empirical ROC curve
#' result <- doROCxy(sa, sp)
#
doROCxy <- function(sa, sp) {  
  
  levels <- sort(unique(c(sa,sp)), decreasing = T)
  
  result <- list(
    fpf = c(0,cumsum(table(factor(sa, levels = levels)))/length(sa)),
    tpf = c(0,cumsum(table(factor(sp, levels = levels)))/length(sp))
  )
  
  threshold <- as.numeric(names(result$fpf))
  threshold[1] <- threshold[2] + 100
  
  result  <- data.frame(
    fpf = result$fpf,
    tpf = result$tpf,
    threshold = threshold,
    row.names = NULL
  )
  
  return(result)
  
}



## doROCxyMRMC - not exported ################################################
#' Create empirical ROC curve from an MRMC formatted data frame
#'
#' @param mrmcAlternate data frame
#' \itemize{
#'   \item{\code{readerID}}
#'   \item{\code{caseID}}
#'   \item{\code{modalityID}}
#'   \item{\code{score}}
#'   \item{\code{truthLabel}}
#' }
#'
#' @return data frame of an ROC curve
#' \itemize{
#'   \item{\code{modalityID}}
#'   \item{\code{readerID}}
#'   \item{\code{fpf} False-positive fractions (1-specificity)}
#'   \item{\code{tpf} True-positive fractions (sensitivity)}
#'   \item{\code{threshold} Threshold corresponding to each fpf, tpf}
#' }
#' 
#'
# @examples
doROCxyMRMC <- function(mrmcAlternate) {

  # We want the list of negative and positive truth case scores from a reader
  s0 <- mrmcAlternate[mrmcAlternate$truthLabel == 0, "score"]
  s1 <- mrmcAlternate[mrmcAlternate$truthLabel == 1, "score"]

  if (length(s0) < 2) {
    ROC <- NULL
    return(ROC)
  } else if (length(s1) < 2) {
    ROC <- NULL
    return(ROC)
  }
  
  
  modalityID <- mrmcAlternate$modalityID[1]
  readerID <- mrmcAlternate$readerID[1]
  
  rocXY <- doROCxy(s0, s1)
  
  ROC <- list(
    modalityID = modalityID,
    readerID = readerID,
    fpf = rocXY$fpf,
    tpf = rocXY$tpf,
    threshold = rocXY$threshold
  )
  
  return(ROC)
  
}



## doROCavg ###############################################################
#' Empirically average over multiple empirical ROC curves
#'
#' @param ROC list of ROC curves. Each element of the list
#'   is a data frame with pairs of (fpf, tpf) operating points.
#'   
#' @param direction the direction over which to average
#' \itemize{
#'   \item{\code{SeSp} (default) The ROC curves are averaged diagonally.
#'     Average Se+Sp of all the input ROC curves
#'     for every possible Se-Sp.}
#'   \item{\code{Se} The ROC curves are averaged vertically.
#'     Average the sensitivity of all the input ROC curves
#'     for every possible specificity.}
#'   \item{\code{Sp} The ROC curves are averaged horizontally.
#'     Average the specificity of all the input ROC curves
#'     for every possible specificity.}
#' }
#'
#' @return data frame of an ROC curve
#' \itemize{
#'   \item{\code{fpf} False-positive fractions (1-specificity)}
#'   \item{\code{tpf} True-positive fractions (sensitivity)}
#' }
#' 
#' @export
#'
#@examples
# # Create a sample configuration file
# config <- sim.gRoeMetz.config()
# # Simulate an MRMC ROC data set
# dFrame.imrmc <- sim.gRoeMetz(config)
# # Analyze the MRMC ROC data
# result <- doIMRMC(dFrame.imrmc)
# # Isolate ROC results
# resultROC <- result[["ROC"]][["testA.pooled"]]
# dFrameROC <- data.frame(tpf = resultROC[["tpf"]], fpf = resultROC[["fpf"]])
# # Empirically average over ROC curves 
# resultROCavg <- doROCavg(dFrameROC, direction = "SeSp")
# 
doROCavg <- function(ROC, direction="SeSp") {
  e <- 9*.Machine$double.eps
  
  ## Loop over ROC curves, rotating them depending upon 
  ## direction of averaging.
  rotrocs <- lapply(ROC, function(ROC.cur) {
    if (direction == "SeSp") {
      
      return(data.frame(u = (ROC.cur$fpf + ROC.cur$tpf) / 2.,
                        v = (ROC.cur$tpf - ROC.cur$fpf) / 2.))
      
    } else if (direction == "Se") {
      
      result <- data.frame(u = ROC.cur$fpf, v = ROC.cur$tpf)
      
    } else if (direction == "Sp") {
      
      result <- data.frame(u = ROC.cur$tpf, v = ROC.cur$fpf)
      
    } else {
      
      stop("Parameter direction must be SeSp, Se, or Sp")
      
    }
    
    # Eliminate redundant "vertical" points for approx()
    result = do.call(rbind, by(result, result$u,function(i) {
      if (nrow(i) < 2) return(i)
      
      top <- i[which.max(i$v),]
      bot <- i[which.min(i$v),]
      
      if (top$u < 1.) top$u = top$u + e
      if (bot$u > 0.) bot$u = bot$u - e
      
      return(rbind(top,bot))
    }))
    
    return(result)
  })
  
  
  # Collect ordinate values at which we will 
  # approximate the mean ROC curve
  xMean <- sort(unique(unlist(lapply(rotrocs, function(x) x$u))))
  
  ## Interpolate all points on all ROC curves.
  approtrocs <- lapply(rotrocs, function(ROC.cur) {
    
    result <- stats::approx(ROC.cur$u, ROC.cur$v, xMean)
    return(result$y)
    
  })
  yMean <- do.call(rbind, approtrocs)
  # str(yMean)
  yMean <- colMeans(yMean)
  
  # Rotate our average ROC curve back to the usual Se(Sp) space.
  if (direction == "SeSp") {
    fpf <- xMean - yMean
    tpf <- xMean + yMean
  } else if (direction == "Se") {
    tpf <- c(0, yMean,1)
    fpf <- c(0, xMean,1)
  } else {
    tpf <- c(0, xMean,1)
    fpf <- c(0, yMean,1)
  }
  
  # Return the x,y points of the average ROC curve
  return(data.frame(fpf = fpf, tpf = tpf))
  
}



## doROCcurveMRMC - not exported ############################################################
#' Create a standard set of ROC curves from an MRMC data frame
#'
#' @param mrmcAlternate data frame
#' \itemize{
#'   \item{\code{readerID}}
#'   \item{\code{caseID}}
#'   \item{\code{modalityID}}
#'   \item{\code{score}}
#'   \item{\code{truthLabel}}
#' }
#'
#'@return list of ROC curves corresponding to
#' all reader x modality combinations,
#' all modalities with the reader scores pooled,
#' all modalities with the per-reader ROC curves, horizontally, diagonally, 
#' and vertically averaged. 
#' 
#'
# @examples
# # Create a sample configuration file
# config <- sim.gRoeMetz.config()
# # Simulate an MRMC ROC data set
# dFrame.imrmc <- sim.gRoeMetz(config)
# # Analyze the MRMC ROC data
# result <- doIMRMC(dFrame.imrmc)
# # Create standard ROC curves from MRMC data
# result_ROCcurves <- doROCcurveMRMC(result)
#' 
#' 
doROCcurveMRMC <- function(mrmcAlternate) {
  
  nm <- length(unique(mrmcAlternate$modalityID))
  modalities <- unique(mrmcAlternate$modalityID)
  
  # ROC per reader ######
  mrmcAlternate.cur <- split(mrmcAlternate, 
                      list(mrmcAlternate$modalityID, mrmcAlternate$readerID))
  ROC <- lapply(mrmcAlternate.cur, doROCxyMRMC)
  
  # Remove list elements when there is no ROC curve
  index.TF <- sapply(ROC, is.null)
  ROC <- ROC[!index.TF]
  
  # ROC pooled readers #####
  mrmcAlternate.cur <- mrmcAlternate
  mrmcAlternate.cur$readerID <- factor("pooled")
  mrmcAlternate.cur <- split(mrmcAlternate.cur, 
                             list(mrmcAlternate.cur$modalityID, 
                                  mrmcAlternate.cur$readerID))
  ROC.pooled <- lapply(mrmcAlternate.cur, doROCxyMRMC)
  
  # ROC averages #####
  ROC.diagonalAvg <- vector("list", nm)
  ROC.verticalAvg <- vector("list", nm)
  ROC.horizontalAvg <- vector("list", nm)
  for (m in 1:nm) {
    
    modalityID <- modalities[m]
    index.TF <- grepl(modalityID, names(ROC))
    
    
    ####
    #### Diagonal (SeSp) average
    ####   
    ROC.cur <- doROCavg(ROC[index.TF], direction = "SeSp")
    
    ROC.cur <- list(
      modalityID = modalityID,
      readerID = "diagonalAvg",
      fpf = ROC.cur$fpf,
      tpf = ROC.cur$tpf
    )
    
    ROC.diagonalAvg[[m]] <- ROC.cur
    
    
    
    ####
    #### Vertical (Se) average
    ####   
    ROC.cur <- doROCavg(ROC[index.TF], direction = "Se")
    
    ROC.cur <- list(
      modalityID = modalityID,
      readerID = "verticalAvg",
      fpf = ROC.cur$fpf,
      tpf = ROC.cur$tpf
    )
    
    ROC.verticalAvg[[m]] <- ROC.cur
    
    
    
    ####
    #### Horizontal (Sp) average
    ####   
    ROC.cur <- doROCavg(ROC[index.TF], direction = "Sp")
    
    ROC.cur <- list(
      modalityID = modalityID,
      readerID = "horizontalAvg",
      fpf = ROC.cur$fpf,
      tpf = ROC.cur$tpf
    )
    
    ROC.horizontalAvg[[m]] <- ROC.cur
    
  }
  names(ROC.diagonalAvg) <- paste(modalities, "diagonalAvg", sep = ".")
  names(ROC.verticalAvg) <- paste(modalities, "verticalAvg", sep = ".")
  names(ROC.horizontalAvg) <- paste(modalities, "horizontalAvg", sep = ".")
  
  
  
  # ROC Aggregate results #####
  ROC <- c(ROC, ROC.pooled, ROC.diagonalAvg, ROC.verticalAvg, ROC.horizontalAvg)
  
  return(ROC)
  
}

