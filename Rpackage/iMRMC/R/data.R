#' Example of iMRMC formatted data frame
#'
#' An example data frame formatted for `doIMRMC` and other iMRMC functions.
#'
#' @format A data frame with 880 rows and 4 columns:
#' \describe{
#'   \item{readerID}{Factor with 5 levels like "reader1", "reader2", ...
#'   As well as the special reader "truth"
#'   }
#'   \item{caseID}{Factor with 80 levels like "case1", "case2", ...}
#'   \item{modalityID}{Factor with 2 levels like "modality1", "modality2", ...
#'   As well as the special modality "truth"
#'   }
#'   \item{score}{Numeric reader score}
#' }
#' 
#' Each row of this data frame corresponds to an observation. For every caseID, 
#' there must be a row corresponding to the truth observation. The readerID for 
#' a truth observation is "truth". The modalityID for a truth observation is 
#' "truth". The score for a truth observation must be either 0 (signal-absent) 
#' or 1 (signal-present). 
#' 
#' @examples 
#' # Create a sample configuration file
#' config <- sim.gRoeMetz.config()
#' # Simulate an MRMC ROC data set
#' dfMRMC_example <- sim.gRoeMetz(config)
#' # Analyze the MRMC ROC data
#' result <- doIMRMC(dfMRMC_example)
#' 
"dfMRMC_example"