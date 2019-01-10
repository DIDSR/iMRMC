## viperObservations ####
#' @title viperObservations
#'
#' @name viperObservations
#'
#' @description The VIPER data sets were created by aggregating all the raw reader data files from the
#'   FDA VIPER study: Validation of Imaging Premarket Evaluation and Regulation (VIPER).
#'   The data sets differ in the truth labels. See the documentation details.
#'
#' @details A data frame with 36 variables: \cr
#'   \itemize{
#'     \item \code{CaseReadOrder} \cr
#'     \item \code{CaseID} \cr
#'     \item \code{Modality} \cr
#'     \item \code{ReaderID} \cr
#'     \item \code{Study} \cr
#'     \item \code{Track} \cr
#'     \item \code{ReadSetLetter} \cr
#'     \item \code{Ctype} \cr
#'     \item \code{Compare.CaseIDs} \cr
#'     \item \code{Initial.Comparison} \cr
#'     \item \code{Final.Comparison} \cr
#'     \item \code{case_id} \cr
#'     \item \code{redcap_event_name} \cr
#'     \item \code{readerID} \cr
#'     \item \code{form_start_datetime} \cr
#'     \item \code{modality} \cr
#'     \item \code{study} \cr
#'     \item \code{viewbox} \cr
#'     \item \code{num_img} \cr
#'     \item \code{recall} \cr
#'     \item \code{miss_img} \cr
#'     \item \code{score_start_datetime} \cr
#'     \item \code{abnormality_type} \cr
#'     \item \code{recall_scale} \cr
#'     \item \code{score_end_datetime} \cr
#'     \item \code{Group} \cr
#'     \item \code{norecall_scale} \cr
#'     \item \code{set_letter} \cr
#'     \item \code{track} \cr
#'     \item \code{quality} \cr
#'     \item \code{UID} \cr
#'     \item \code{score} \cr
#'     \item \code{desc} \cr
#'     \item \code{read_order} \cr
#'     \item \code{Comment} \cr
#'     \item \code{RecallRegion} \cr
#'     \item \code{count}
#'   }
#'
#'   \strong{viperObs365}: This dataset is identical to \code{\link{viperObs455}} except for the truth labels.
#'   The truth labels for this dataset are based on cancer at 365 days.
#'
#'   \strong{viperObs455}: This dataset is identical to \code{\link{viperObs365}} except for the truth labels.
#'   The truth labels for this dataset are based on cancer at 455 days.
#'
#'   The images used in this study were selected from the DMIST images [Pisano2005_NEJM_v353p1773].
#'   The VIPER study concept 2011. Data collection September 2013 to August 2015.
#'   Please refer to journal paper for details.
#'
NULL

## viperObs365 ####
#' viperObs365
#'
#' @rdname viperObservations
"viperObs365"

## viperObs455 ####
#' viperObs455
#'
#' @rdname viperObservations
"viperObs455"

## viperSummaries ####
#' viperSummaries
#'
#' @name viperSummary
#'
#' @description The VIPER summary files contain summary statistics of the VIPER observations.
#' The iMRMC R package is used to estimate the area under the ROC curve (AUC),
#' the true positive fraction (sensitivity), and the true negative fraction (specificity).
#' The iMRMC package also estimates variances, standard errors, and confidence intervals
#' that account for the variability from readers and cases. You can refer to the R function
#' \code{doViperSummary.R} in this package for the code that produces the summary files.
#' 
#' The iMRMC R package can be found at
#' \itemize{
#'   \item{\url{https://github.com/DIDSR/iMRMC/releases},}
#'   \item{\url{https://github.com/DIDSR/iMRMC/tree/master/Rpackage}, and}
#'   \item{\url{https://cran.r-project.org/web/packages/iMRMC/index.html}.}
#' }
#'
#' @details Each element of the list pertains to one of the VIPER sub-studies:
#'   \itemize{
#'     \item \code{screeningLowP} [list] Screening population, per reader prevalence ~ 10\%
#'     \item \code{screeningMedP} [list] Screening population, per reader prevalence ~ 30\%
#'     \item \code{screeningHighP} [list] Screening population, per reader prevalence ~ 50\%
#'     \item \code{challengeMedP} [list] Challenge population, per reader prevalence ~ 30\%
#'     \item \code{challengeHighP} [list] Challenge population, per reader prevalence ~ 50\%
#'   }
#'
#' Each substudy list element is a list object of length 3.
#'   \itemize{
#'     \item \code{desc} [string] Sub-study label. This is the same as the sub-study list element name.
#'     \item \code{studyDesign} [list] This list object contains several ways
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
#'     \item \code{iMRMC} [list] This list object contains the MRMC analysis results for
#'      auc, tpf, and tnf.
#'      There are four groups of results: perReader, Ustat, MLEstat, ROC, and varDecomp
#'      The analysis results are produced by a command line version of iMRMC called by R:
#'      \url{https://github.com/DIDSR/iMRMC/releases},
#'      \url{https://github.com/DIDSR/iMRMC/tree/master/Rpackage},
#'      \url{https://cran.r-project.org/web/packages/iMRMC/index.html}
#'   }
#'
#'   \strong{viperSummary365}: This summary is based on \code{\link{viperObs365}} the dataset
#'   based on cancer at 365 days.
#'
#'   \strong{viperSummary455}: This summary is based on \code{\link{viperObs455}} the dataset
#'   based on cancer at 455 days.
#'
NULL

## viperSummary365 ####
#' VIPERsummary365
#'
#' @rdname viperSummary
#'
"viperSummary365"

## viperSummary455 ####
#' VIPERsummary455
#'
#' @rdname viperSummary
#'
"viperSummary455"

## VIPER reader qualification summary ####
#' VIPER reader qualification summary
#'
#' @description This data set contains the information from the VIPER reader recruitment qualification forms.
#'   The original data was modified to turn
#'   text in numeric fields to appropriate numbers. For example, "~100" was modified to 100,
#'   "100+" was modified to 100,  "<50" was modified to 50, "200-250" was modified to 225,
#'   "don't know" was modified to "NA", and "very few" was modified to "NA".
#'
#' @details A data frame with 10 variables:
#'    \itemize{
#'      \item \code{readerID} \cr
#'      \item \code{certifiedABR} \cr
#'      \item \code{breastImagingFellowship} \cr
#'      \item \code{fellowshipYear} \cr
#'      \item \code{min50sfm} \cr
#'      \item \code{min50ffdm} \cr
#'      \item \code{howManySFMlast2years} \cr
#'      \item \code{howManyFFDMlast2years} \cr
#'      \item \code{howManyYearsPostResidencyExperienceInterpretingSFM} \cr
#'      \item \code{howManyYearsPostResidencyExperienceInterpretingFFDM}
#'    }
#'
"viperReaderQualifications"

## DMIST data ####
#' DMIST data
#'
#' @description DMIST performance data manually transcribed from
#' Pisano2005_NEJM_v353p1773 and its supplement.
#'
#' @details
#' The performance data is organized in a list with the following elements.
#'    \itemize{
#'      \item \code{nObs.FFDM} [num] The number of FFDM observations (from Supplemental Table 2)
#'      \item \code{nObs.SFM} [num] The number of SFM observations (from Supplemental Table 2)
#'      \item \code{nObs.total} [num] the total number of observations (from Supplemental Table 2)
#'      \item \code{caseDist} [list] This list contains two items (from Supplemental Table 2)
#'        \itemize{
#'          \item \code{desc} [char] A description of the data.
#'          \item \code{caseDist.df} [data frame] The distribution of BIRADS scores from DMIST. \cr
#'            Row 1 is the distribution of FFDM BIRADS scores among DMIST cancers. \cr
#'            Row 2 is the distribution of SFM BIRADS scores among DMIST cancers. \cr
#'            Row 3 is the distribution of FFDM BIRADS scores among all DMIST cases. \cr
#'            Row 4 is the distribution of SFM BIRADS scores among all DMIST cases.
#'        }
#'      \item \code{caseDist.denseBreasts} [data frame] Caption to Figure 1 and row "T stage" of Table 2.
#'        One row of cancer=n1 detected by FFDM and SFM, cancer=n1 detected by SFM but not FFDM,
#'        cancer=n1 detected by FFDM but not SFM, cancer=n1 not detected by SFM or FFDM, non-cancer=n2, and total.
#'      \item \code{performance} [list]
#'      \itemize{
#'        \item \code{desc} [char] A description of the data taken from Figure 1 and the text of
#'          Pisano2005_NEJM_v353p1773.pdf. SE of AUC is taken to be 1/4 of the confidence
#'          interval specified. AUC determined using seven-point malignancy scale.
#'          TPF,FPF are from Table 2 of Pisano2005_NEJM_v353p1773-Suppl.pdf.
#'          TPF,FPF are determined using BIRADS. Cancer determined at 455 days for AUC, TPF, and FPF.
#'        \item \code{FFDM} [list] A list of the diagnostic performance statistics:
#'          \code{AUC, AUCse, CIbot, CItop, TPF, TPFse, TNF, TNFse}
#'        \item \code{SFM} [list] A list of the diagnostic performance statistics:
#'          \code{AUC, AUCse, CIbot, CItop, TPF, TPFse, TNF, TNFse}
#'        \item \code{FFDMminusSFM} [list] A list of the diagnostic performance statistics:
#'          \code{AUC, AUCse, CIbot, CItop, p, TPF, TPFse, TPFCIbot, TPFCItop, TPFp,
#'            TNF, TNFse, TNFCIbot, TNFCItop, TNFp}
#'      }
#'    }
#'
"dmistData"

## DMIST Reader Studies Summaries ####
#' dmistRSsummary
#'
#' @description Performance summaries from the following DMIST reader studies:
#' \itemize{
#'   \item Hendrick2008_Radiology_v247p38, 'Accuracy of soft-copy digital mammography
#'   versus that of screen-film mammography according to digital manufacturer:
#'   ACRIN DMIST retrospective multireader study.' There were four machines studied,
#'   labeled below as hendrick2008.GE, hendrick2008.Fuji, hendrick2008.Fischer,
#'   hendrick2008.Hologic. The summary based on the aggregate of all this data is
#'   hendrick2008.
#'   \item Nishikawa2009_Radiology_v251p41, 'Comparison of Soft-Copy and Hard-Copy
#'   Reading for Full-Field Digital Mammography.'  There were four machines studied,
#'   labeled below as nishikawa2009.GE, nishikawa2009.Fuji, nishikawa2009.Fischer,
#'   nishikawa2009.Hologic. The summary based on the aggregate of all this data is
#'   nishikawa2009.
#'   \item An unpublished study evaluating the effect of breast density on 
#'   diagnostic accuracy of FFDM and SFM. The study included 320 women 
#'   (109 cancers) stratified by original SFM breast density determination,
#'   8 readers reading FFDM, and 8 readers reading SFM. There were
#'   four density levels studied, corresponding to the four BIRADS density
#'   levels, labeled as breastDensity.A, breastDensity.B, breastDensity.C,
#'   breastDensity.D. The summary based on the aggregate of all this data is breastDensity.
#'   \item An unpublished study evaluating performance in a reader study
#'   at 50% prevalence. The scope of the study has been described as including
#'   30 readers assigned to read a total of 300 cases as FFDM and SFM.
#'   There was one data set, labeled as prevalence50.
#' }
#'
"dmistRSsummary"

