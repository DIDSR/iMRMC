#' doViperStudyDesign
#'
#' @description Extract basic study design info
#'
#' @param rawData One of the VIPER sub-study data frames.
#'
#' @return A list object that contains several ways
#'      to summarize the distribution of cases in the study accoring to case type.
#'      \itemize{
#'        \item \code{nObs} [num] The number of observations in the sub-study.
#'        \item \code{readers} [char] The reader IDs in the sub-study.
#'        \item \code{nR} [num] The number of readers in the sub-study.
#'        \item \code{nC} [data frame] The number of cases in the sub-study by case type.
#'          One row of birads0ffdm, birads0sfm, birads12ffdm, birads12sfm, cancer, noncancer.
#'        \item \code{perReader} [list] This list contains two data frames: FFDM and SFM.
#'          Each of these data frames includes the number of cases evaluated by each reader by case type.
#'          20 readers == 20 Rows of birads0ffdm, birads0sfm, birads12ffdm, birads12sfm, cancer, noncancer.
#'        \item \code{perReader.summary} [list] This list contains two data frames: FFDM and SFM.
#'          Each of these data frames includes the total number of cases evaluated by all readers by case type.
#'          One row of birads0ffdm, birads0sfm, birads12ffdm, birads12sfm, cancer, noncancer.
#'      }
#'
#' @export
#'
#' @examples
#' df <- split(viperData::viperObs455, viperData::viperObs455$desc)
#' df.studyDesign <- doViperStudyDesign(df$screeningLowP)
doViperStudyDesign <- function(rawData) {

  nObs <- nrow(rawData)
  rawData <- droplevels(rawData)

  # Identify the readers
  readers <- levels(rawData$readerID)
  nR <- nlevels(rawData$readerID)

  # Identify the number of cases for each type
  nC <- data.frame(
    birads0ffdm = length(unique(rawData$caseID[rawData$Ctype == "birads0ffdm"])),
    birads0sfm = length(unique(rawData$caseID[rawData$Ctype == "birads0sfm"])),
    birads12ffdm = length(unique(rawData$caseID[rawData$Ctype == "birads123ffdm"])),
    birads12sfm = length(unique(rawData$caseID[rawData$Ctype == "birads123sfm"])),
    cancer = length(unique(rawData$caseID[rawData$Ctype == "cancer"])),
    nonCancer = length(unique(rawData$caseID[rawData$Ctype != "cancer"]))
  )

  # For every case type, reader, and modality, count the number of observations
  desc <- (by(
    rawData,
    list(rawData$readerID, rawData$Ctype, rawData$modality), nrow))

  FFDM <- data.frame(desc[,,1])
  SFM <- data.frame(desc[,,2])

  FFDM$nonCancer <- rowSums(FFDM[, names(FFDM) != "cancer"])
  SFM$nonCancer <- rowSums(SFM[, names(SFM) != "cancer"])

  FFDM$prevalence <- FFDM$cancer / (FFDM$cancer + FFDM$nonCancer)
  SFM$prevalence <- SFM$cancer / (SFM$cancer + SFM$nonCancer)

  perReader <- list(FFDM = FFDM, SFM = SFM)

  FFDM <- data.frame(t(sapply(perReader$FFDM, sum)))
  SFM <- data.frame(t(sapply(perReader$SFM, sum)))

  FFDM <- rbind(FFDM, data.frame(t(sapply(perReader$FFDM, mean))))
  SFM <- rbind(SFM, data.frame(t(sapply(perReader$SFM, mean))))

  FFDM <- rbind(FFDM, data.frame(t(sapply(perReader$FFDM, stats::median))))
  SFM <- rbind(SFM, data.frame(t(sapply(perReader$SFM, stats::median))))

  FFDM <- rbind(FFDM, data.frame(t(sapply(perReader$FFDM, min))))
  SFM <- rbind(SFM, data.frame(t(sapply(perReader$SFM, min))))

  FFDM <- rbind(FFDM, data.frame(t(sapply(perReader$FFDM, max))))
  SFM <- rbind(SFM, data.frame(t(sapply(perReader$SFM, max))))

  rownames(FFDM) <- c("total", "mean", "median", "min", "max")
  rownames(SFM) <- c("total", "mean", "median", "min", "max")

  perReader.summary <- list(FFDM = FFDM, SFM = SFM)

  studyDesign <- list(
    nObs = nObs,
    readers = readers,
    nR = nR,
    nC = nC,

    perReader = perReader,
    perReader.summary = perReader.summary
  )

  return(studyDesign)

}

