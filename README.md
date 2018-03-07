<h3>README for iMRMC and iRoeMetz</h3>

Visit the <a href="https://github.com/DIDSR/iMRMC/releases" rel="nofollow">release Page</a> to download the programs.

The documentation for both packages can be found <a href="http://didsr.github.io/iMRMC/" rel="nofollow">here.</a>

Here is a summary of updates to [iMRMC](https://github.com/DIDSR/iMRMC/blob/master/UPDATES_iMRMC.md) and a summary of updates to [iRoeMetz](https://github.com/DIDSR/iMRMC/blob/master/UPDATES_iRoeMetz.md).

Please check out a statement of the licenses related to our software [here](LICENSE.md).

<h5>iMRMC</h5>

Desc: The primary objective of the iMRMC application is to assist investigators with analyzing and sizing multi-reader multi-case (MRMC) reader studies that compare the difference in the area under Receiver Operating Characteristic curves (AUCs) from two modalities. The core elements of this application include the ability to perform MRMC variance analysis and the ability to size an MRMC trial.

 * The core iMRMC application is a stand-alone, precompiled, license-free Java applications and the source code. It can be used in GUI mode or on the command line.
 * There is also an R package that utilizes the core Java application. Examples for using the programs can be found in the R help files.

<h5>iRoeMetz</h5>

Desc: The iRoeMetz application can be used to simulate the reader scores for MRMC experiments via Monte Carlo methods given variance components of the ROC scores. The application also estimates the variance components of AUC and can calculate the variance components directly with numerical integration. The simulated experiments can be saved and used in MRMC variance analysis programs, such as iMRMC. The iRoeMetz application is a stand-alone, precompiled, license-free Java applications and the source code.

<h5>R package iMRMC</h5>

Desc: The R package iMRMC executes the iMRMC program which writes the results to the local files system, it reads the analysis results from the local file system, packs the analysis results into a list object, deletes the data and analysis results from the local file system, and returns the list object.
