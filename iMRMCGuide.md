### Summary ###
The primary objective of this application, iMRMC, is to assist investigators with analyzing and sizing multi-reader multi-case (MRMC) reader studies that compare the difference in the area under Receiver Operating Characteristic curves (AUCs) from two modalities. The core features of this java application include the ability to do MRMC variance analysis, the ability to size an MRMC trial, and a database containing components of variance from past MRMC studies.

When publishing results using our software, please reference with one of the following:
  * Gallas, B. D.; Bandos, A.; Samuelson, F. & Wagner, R. F. (2009), 'A Framework for Random-Effects ROC Analysis: Biases with the Bootstrap and Other Variance Estimators', _Commun Stat A-Theory_ **38** (15), 2586-2603.
  * Gallas, B. D. (2006), 'One-Shot Estimate of MRMC Variance: AUC.' _Acad Radiol_, **13** (3), 353-362.

The software treats arbitrary study designs that are not "fully-crossed". Please refer to the following for more information.
  * Gallas, B. D. & Brown, D. G. (2008), 'Reader Studies for Validation of CAD Systems.' _Neural Networks Special Conference Issue_, **21**, (2-3), 387-397.
  * Obuchowski, N.; Gallas, B. D. & Hillis, S. L. (2012), 'Multi-Reader ROC Studies with Split-Plot Designs: A Comparison of Statistical Methods.' _Acad Radiol_, **19**, 1508-1517.

### _Status_ ###
_The sizing analysis has been debugged and enabled once more. We believe all elements are now functioning properly. Confirmatory validation of statistical and sizing analysis by Roe and Metz simulation is planned. Please contact us with any issues by email or via the issues page._


### _Authors_ ###
  * Rohan Pathare
  * Brandon D. Gallas, PhD
  * Xin He, PhD

<br>
<h1>User Manual Current Version</h1>
Please <b>read</b> the user manual for useful information. See below for the current version of the user manual. The user manual contains<br>
<br>
<ul><li>Introduction to ROC Reader Studies</li></ul>

<ul><li>How iMRMC is implemented</li></ul>

<ul><li>How to use the iMRMC application</li></ul>

<ul><li>References for iMRMC</li></ul>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMCuserguide-v2p2.pdf'>Click to download user manual (iMRMCUserGuide-v2p2.pdf)</a></b> <br>

<br>

<h1>Application Current Version</h1>
Version 2p7 posted 2015-01-22<br>
<br>
Update calculation of DF_BDG, degrees of freedom described in<br>
Obuchowski2012_Acad-Radiol_v19p1508, to treat unpaired readers, normal cases, or disease cases. Also implemented a minimum DF_BDG according<br>
to Gaylor1969_Technometrics_v4p691 Eq. 5. <br>
Bug Fix: DF_BDG estimates less than 2 can't be treated by the implementation of Student T distribution. So DF_BDG is not allowed to fall below 2. <br>
Bug Fix: When DF_Hillis is infinity (ms_tr=0.0), some users reported an application crash. So DF_Hillis is set to 50 in this case, and you get the same effect. <br>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMC-v2p7.jar'>Click to download application (imrmc-v2p7.jar)</a></b>

<br>

<h1>Other Resources</h1>

<h2>Presentation: "Resources for Reader Studies"</h2>

Presented at SPIE Medical Imaging Conference 2015 <br>
Invited speaker for a joint workshop between the Physics and Image Perception, Observer Performance, and Technology tracks <br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/docs/2015SPIE-MIworkshopBDG-4.pdf'>Click to download presentation</a></b> <br>
<br>

<h2>Sample Input Files (<code>*</code>.imrmc)</h2>

Here are some sample <code>*</code>.imrmc input files that you can use to try the software. There is a directory of sample input files generated from iRoeMetz following the parameters spelled out in Roe1997_Acad-Radiol_v4p298. There is also a directory of the sample input files that have common formatting errors that are detected and reported. <br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/FilesTest-iMRMC'>Click to download raw data input files (*.imrmc)</a></b> <br>

<br>

<h2>Sample eCRF and Reader Training</h2>

Here is what the eCRF looks like for a reader study we are executing that compares Full-Field Digital Mammography to Screen-Film. The clinician is first asked whether or not to recall the patient. It has a 100-point scale below the no recall/recall decision threshold and a 100-point scale above the no recall/recall decision threshold. It also has instructions for using each 100-point scale. <br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/docs/VIPER/eCRF-3.pdf'>Click to download a sample eCRF</a></b> <br>

Here are instructions for readers on reporting the numeric score on the eCRF.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/docs/VIPER/VIPER%20instructions-scoring-v2.pdf'>Click to download the sample training instructions</a></b> <br>
<br>




<h1><a href='iMRMCVersionHistory.md'>Download Previous Versions</a></h1>
<br>

<h1><a href='http://imrmc.googlecode.com/svn/'>Direct Access to Repository Contents</a></h1>

<br>
<br>
<br>


<h4>View the Javadoc for the source code <a href='http://imrmc.googlecode.com/svn/javadoc/index.html'>here</a></h4>

<a href='http://imrmc.googlecode.com/svn/wiki/gadgets/printwikis.html?_gnpi_subversion="svn"&_gnpi_wikilist="TableOfContents|IntroToROCReaderStudies|iMRMCMethods|iMRMCDatabase|DatasetSummaryInfo|iMRMCUsage|ROCStudyDataFormat|iMRMCReferences"'>Print Me</a>