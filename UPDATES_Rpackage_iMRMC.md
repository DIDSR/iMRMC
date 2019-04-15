<h2>Updates</h2>

<h4>Version Under Development</h4>
...

<h4>iMRMC_1.2.0</h4>

*04/15/19*

Use new version iMRMC-v4.0.3.jar to do the analysis.

In simMRMC.R, add option to drop levels from the design matrix in convertDFtoDesignMatrix.

Add limitsOfAgreement.R to analyze limits of agreement for: 
 * within-reader, between-modality
 * between-reader, within-modality
 * between-reader, between-modality
Create new test for split plot data.

Pre-check input file before do analysis.

Rename and re-organize validation functions.

Rename some data frame columns name. 


<h4>iMRMC_1.1.1.1000</h4>
Under development

New utilities under development in the repository, but not yet in the R package on CRAN.
 * laBRBM and laWRBM are wrappers for producing the Bland-Altman limits of agreement using uStat11.conditional.
 * Assign group labels to items in a vector in order to create split-plot groups (createGroups).
 * extractPairedComparisonsBRBM and extractPairedComparisonsWRMB that enable (scatter) plots for agreement analysis
 * renameCol renames a data frame column
 
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
