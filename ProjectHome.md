# iMRMC Home #


---


## [iMRMC](iMRMCGuide.md) ##
Desc: The primary objective of the iMRMC application is to assist investigators with analyzing and sizing multi-reader multi-case (MRMC) reader studies that compare the difference in the area under Receiver Operating Characteristic curves (AUCs) from two modalities. The core elements of this application include the ability to perform MRMC variance analysis, the ability to size an MRMC trial, and a database containing components of variance from past MRMC studies. The iMRMC application is a stand-alone, precompiled, license-free Java applications and the source code.


## [iRoeMetz](iRoeMetzGuide.md) ##
Desc: The iRoeMetz application can be used to simulate the reader scores for MRMC experiments via Monte Carlo methods given variance components of the ROC scores. The application also estimates the variance components of AUC and can calculate the variance components directly with numerical integration. The simulated experiments can be saved and used in MRMC variance analysis programs, such as iMRMC. The iRoeMetz application is a stand-alone, precompiled, license-free Java applications and the source code.

## [iMRMC\_Binary](iMRMC_Binary.md) ##
iMRMC\_Binary is a software package for simulating, sizing, and analyzing a multi-reader multi-case (MRMC) reader study with binary assessments (e.g., whether a reader’s assessment of a patient case agrees with a reference standard). The software allows generating MRMC datasets (binary reader assessment scores) that have the user-specified correlation structure (or variance components) that exists in real-world MRMC studies. The package also has a function that uses simulated datasets to validate an analysis method (i.e., examine the empirical coverage probability of the confidence interval estimated by an analysis method).  The software also does Monte Carlo based power calculation that can be applied to any analysis method. The current version of the software assumes a fully-cross design, ie, every reader reads every case of both modalities. The documentation (user manual) has a description of the simulation model, the use of the model for validation and sizing, and literature related to MRMC analysis and study designs. The software can run under both Matlab and Octave (free version of Matlab).
<br>

<h3><a href='http://imrmc.googlecode.com/svn/'>Direct Access to Repository Contents</a></h3>

<h3>View the Javadoc for the source code description <a href='http://imrmc.googlecode.com/svn/javadoc/index.html'>here</a></h3>

<h2>License and Copyright</h2>
This software and documentation (the "Software") were developed at the Food and Drug Administration (FDA) by employees of the Federal Government in the course of their official duties. Pursuant to Title 17, Section 105 of the United States Code, this work is not subject to copyright protection and is in the public domain. Permission is hereby granted, free of charge, to any person obtaining a copy of the Software, to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, or sell copies of the Software or derivatives, and to permit persons to whom the Software is furnished to do so. FDA assumes no responsibility whatsoever for use by other parties of the Software, its source code, documentation or compiled executables, and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic. Further, use of this code in no way implies endorsement by the FDA or confers any advantage in regulatory decisions. Although this software can be redistributed and/or modified freely, we ask that any derivative works bear some notice that they are derived from it, and any modified versions bear some notice that they have been modified.