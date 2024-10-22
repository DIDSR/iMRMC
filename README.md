# README for iMRMC and iRoeMetz

Here is a high-level overview of the iMRMC software:

* [PDF document](https://zenodo.org/record/8383591)
* [Entry in FDA/CDRH Regulatory Science Tool Catalog](https://cdrh-rst.fda.gov/imrmc-software-do-multi-reader-multi-case-statistical-analysis-reader-studies)

Examples of specific MRMC analyses and other FAQs are available in the [iMRMC repository's Wiki](https://github.com/DIDSR/iMRMC/wiki).

The R package is available from [CRAN](https://cran.r-project.org/web/packages/iMRMC/index.html). You can also visit the <a href="https://github.com/DIDSR/iMRMC/releases" rel="nofollow">release Page</a> to download the R package (source tar.gz file and binary zip file), the zip and tar.gz files of the entire repository, or the iMRMC java app (jar file).

Documentation of the java app can be found here: http://didsr.github.io/iMRMC/.

Here are summaries of updates to the now frozen java apps:  [iMRMC](https://github.com/DIDSR/iMRMC/blob/master/UPDATES_iMRMC.md) and [iRoeMetz](https://github.com/DIDSR/iMRMC/blob/master/UPDATES_iRoeMetz.md).

Please check out a statement of the licenses related to our software [here](LICENSE.md).

**[Issues](https://github.com/DIDSR/iMRMC/issues)**: You may find it useful to post an issue or search the issues posted by other users. We don't have dedicated time to respond to the issues, but we try our best to help. Receiving issues from GitHub allows us to document support efforts to management, helps manage email load, and allows others to benefit from the questions and answers. We also ask that your GitHub account profile include your name and affiliation and keep your questions to fixing bugs, understanding the inputs and outputs, and possibly what the software can do. Engaging via the GitHub issues also protects us from spam and malicious files.

## iMRMC

Desc: The primary objective of the iMRMC application is to assist investigators with analyzing and sizing multi-reader multi-case (MRMC) reader studies that compare the difference in the area under Receiver Operating Characteristic curves (AUCs) from two modalities. The core elements of this application include the ability to perform MRMC variance analysis and the ability to size an MRMC trial.

 * The core iMRMC application is a stand-alone, precompiled, license-free Java applications and the source code. It can be used in GUI mode or on the command line.
 * There is also an R package that utilizes the core Java application. Examples for using the programs can be found in the R help files.

Here are some ways to cite our work:
* FDA/CDRH, “iMRMC: Software for the Statistical Analysis of multi-reader multi-case studies,” *RST Catalog*, 2022, https://doi.org/10.5281/ZENODO.6628838.
* Gallas, Brandon D., Andriy Bandos, Frank Samuelson, and Robert F. Wagner. “A Framework for Random-Effects ROC Analysis: Biases with the Bootstrap and Other Variance Estimators.” Commun Stat A-Theory 38, no. 15 (2009): 2586–2603. https://doi.org/10.1080/03610920802610084.
* Gallas, Brandon D. “One-Shot Estimate of MRMC Variance: AUC.” Acad Radiol 13, no. 3 (2006): 353–62. https://doi.org/10.1016/j.acra.2005.11.030.


## iRoeMetz

Desc: The iRoeMetz application can be used to simulate the reader scores for MRMC experiments via Monte Carlo methods given variance components of the ROC scores. The application also estimates the variance components of AUC and can calculate the variance components directly with numerical integration. The simulated experiments can be saved and used in MRMC variance analysis programs, such as iMRMC. The iRoeMetz application is a stand-alone, precompiled, license-free Java applications and the source code.

Here is one way to cite our work:
* Gallas, Brandon D., and Stephen L. Hillis. “Generalized Roe and Metz ROC Model: Analytic Link between Simulated Decision Scores and Empirical AUC Variances and Covariances.” J Med Img 1, no. 3 (2014): 031006. https://doi.org/doi:10.1117/1.JMI.1.3.031006.

[![Github All Releases](https://img.shields.io/github/downloads/DIDSR/iMRMC/total)]()

<h5>Team</h5>
Please refer to the [NEWS](https://github.com/DIDSR/iMRMC/blob/iMRMC-v1.2.5/Rpackage/iMRMC/NEWS.md) for the complete list of members that have made this project possible.
