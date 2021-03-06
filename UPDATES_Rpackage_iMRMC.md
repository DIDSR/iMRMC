<h2>Updates</h2>

<h4>Version Under Development</h4>

We are rewriting do_IMRMC to use only R code and not call the iMRMC java app. The iMRMC java app is big (4MB) and slow (R code writes ROC data to a file, java app loads, java app reads ROC data from the file, java app processes ROC data, java app writes results to several files, R code reads results from the several files). 
* The ROC curves have minor differences that are due to java app rounding numbers when writing the results to files. Also, the objects containing the ROC curves have inconsequential differences in their attributes arising from the results being read from files.

...

<h4>iMRMC_1.2.3</h4>
*05/05/20*

Improve error reporting to help end users identify incorrect format of iMRMC input data/file.

<h4>iMRMC_1.2.2</h4>
*03/24/20*

Update all calls to data.frame and read.table
* CRAN identified new potential issues. data.frame() and read.table() will use a stringsAsFactors = FALSE default. This change is planned to become the new default for the upcoming R 4.0.0.

<h4>iMRMC_1.2.1</h4>
*01/20/20*

Update iMRMC.R
* CRAN identified new errors from this function
* This function calls a java app for the iMRMC engine using system2.
This fails if a user does not have the correct version of java.
system2 errors are not always managed as errors, some happen silently.
Therefore we added tryCatch error control to help the user identify why the function fails.

Update limitsOfAgreement.R
* Add functions getWRBM and getBRBM. These functions get within-reader between-modality or between-reader between-modality paired data from an MRMC data file. Useful for plotting Bland-Altman-like difference plots.

Update utils.R
* Add convertDF to the package. This function converts list-mode data to matrix-mode data.
* Add html documentation into inst/extra/man for easy reference on GitHub.

Update simMRMC.R
* Use the current version of the random number generator. Default was using version 3.5.0

<h4>iMRMC_1.2.0</h4>
*04/15/19*

Update the core java package to iMRMC-v4.0.3.jar.

Update simMRMC.R
 * Move convertDFtoScoreMatrix and convertDFtoDesignMatrix to utils.R.

Update uStats.R
 * In uStat11.conditionalD and uStat11.jointD, var.1obs[3] is fixed to be the variance of the difference;
 * Remove default specifications of modalitiesToCompare;
 * Add check that modalitiesToCompare contains the right number of modalities;
 * In uStat11.conditionalD, check that all readers must have more than 2 observations in both modalities;
 * In uStats.identity and uStats.diff: return the kernel matrix equal to the product of the kernel and design matrices.

Update utils.R
 * In undoIMRMCdf, drop levels before returning data frame;
 * Add function createGroups, extractPairedComparisonsBRBM, extractPairedComparisonsWRBM, getMRMCscore;
 * Move convertDFtoScoreMatrix and convertDFtoDesignMatrix from simMRMC.R
      * Add dropFlag to drop levels from the design matrix;
      * Add modality parameter to determine which modality creates the matrices;
      * Check that only one modality creates the matrices.

Add limitsOfAgreement.R, which includes the following analysis functions:
 * laWRBM: within-reader, between-modality;
 * laBRWM: between-reader, within-modality;
 * laBRBM: between-reader, between-modality.
 
 
<h4>iMRMC_1.1.0</h4>
*12/12/17*

We have created an R package called iMRMC. It was published on CRAN, https://cran.r-project.org/web/packages/iMRMC/index.html Please refer to the R help pages for the documentation.

The main component of the package is the iMRMC.jar application in [the GitHub repository](https://github.com/DIDSR/iMRMC). The R package function doIMRMC calls iMRMC.jar and returns all the results (command line call, intermediate files are written to and read from the R temporary directory; there are options available to use or save the intermediate files in a user directory). Details on iMRMC.jar and the results are given in the [documentation here](http://didsr.github.io/iMRMC/).

Here are other components of the iMRMC R package:

 * Simulation tools (simMRMC, sim.gRoeMetz, sim.gRoeMetz.config, simRoeMetz.example)
 * Functions that analyze U-statistics of degree 1,1 (uStat11.conditional is the recommended function).
 * Utility functions
     * Initialize the l'Ecuyer random number generator that is perfect for parallel programming (init.lecuyerRNG)
     * Transform ROC data formatted for doIMRMC to TPF and FPF data formatted for doIMRMC (roc2binary)
     * Transform typical R data frames to and from data frames formatted for the iMRMC.jar program (createIMRMCdf and undoIMRMCdf)
     * Extract design and success matrices from a data frame (convertDFtoDesignMatrix and convertDFtoScoreMatrix).
     * Convert an MRMC data frame of successes to one formatted for doIMRMC to do MRMC analysis of binary performance (successDFtoROCdf)
