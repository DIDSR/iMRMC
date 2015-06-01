The previous versions are provided here from most recent to the oldest.

When accessing links, make sure that you refresh your browser.

<br>
<h2>Sample Input Files (<code>*</code>.imrmc)</h2>

Here are some sample <code>*</code>.imrmc input files that you can use to try the software. Some of the sample input files have common formatting errors that are detected and reported. <br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/FilesTest-iMRMC'>Click to download raw data input files (*.imrmc)</a></b> <br>

<br>

<h1>iMRMC Application Version 2p7</h1>
posted 2015-01-22<br>
<br>
Update calculation of DF_BDG, degrees of freedom described in<br>
Obuchowski2012_Acad-Radiol_v19p1508, to treat unpaired readers, normal cases, or disease cases. Also implemented a minimum DF_BDG according<br>
to Gaylor1969_Technometrics_v4p691 Eq. 5. <br>
Bug Fix: DF_BDG estimates less than 2 can't be treated by the implementation of Student T distribution. So DF_BDG is not allowed to fall below 2. <br>
Bug Fix: When DF_Hillis is infinity (ms_tr=0.0), some users reported an application crash. So DF_Hillis is set to 50 in this case, and you get the same effect.Bug Fix <br>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMC-v2p7.jar'>Click to download application (imrmc-v2p7.jar)</a></b>

<br>

<h1>iMRMC Application Version 2p6</h1>
posted 2014-09-05<br>
<br>
Sizing analysis functioning. We believe it is functioning properly and are planning a validation paper. We have also added a button to view the individual reader AUCs. Analysis of individual reader AUCs is planned. <br>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMC-v2p6.jar'>Click to download application (imrmc-v2p6.jar)</a></b>

<br>

<h1>iMRMC Application Version 2p5</h1>
posted 2014-08-14<br>
<br>
Fixed bug that was causing iMRMC to crash when executing statistical analysis of a single modality AUC. <br>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMC-v2p5.jar'>Click to download application (imrmc-v2p5.jar)</a></b>

<br>

<h1>iMRMC User Manual Version 2p2</h1>
posted 2014-06-03<br>
<br>
Updated some instructions on data input files and fixed a misspelling related to the BCK representation of variance components(Krupinski -> Kupinski).<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMCuserguide-v2p2.pdf'>Click to download user manual (iMRMCUserGuide-v2p2.pdf)</a></b> <br>

<br>
<h1>iMRMC Application Version 2p4</h1>
posted 2014-03-28<br>
<br>
Power and sizing analysis disabled while it goes through a detailed validation. <br>
Database input method disabled as it undergoes a remake. Consequently, this version does not require a database folder or .jdb files.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iMRMC-v2p4.jar'>Click to download application (imrmc-v2p4.jar)</a></b>

<br>
<h1>iMRMC Application Version 2p3</h1>
posted 2014-03-05<br>
<br>
This version provides robust data check and error reporting during the reading of the InputFile.imrmc (Identify missing truth, duplicate cases, replicated observations). Additionally, readerIDs, caseIDs, and modalityIDs can be strings. These changes allow for more flexibility and should help the user determine problems with the file format. <br>
There was a bug that was found and fixed: the coefficients were incorrectly calculated when the data was <b>not</b> fully-crossed. <br>
Power for sizing to come. <br>
Sample files are now provided. <br>
This version requires a database folder named "DB" with .jdb files in the application startup directory.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application_archive/iMRMC/iMRMC-v2p3.jar'>Click to download application (imrmc-v2p3.jar)</a></b>

<br>
<h1>iMRMC Application Version 2p1</h1>
posted 2014-01-15<br>
<br>
This version fixes p-values and confidence intervals in the statistical analysis. <br>
Power for sizing to come. <br>
This version requires a database folder named "DB" with .jdb files in the application startup directory.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application_archive/iMRMC/iMRMC-v2p1.jar'>Click to download application (iMRMC-v2p1.jar)</a></b>

<br>
<h1>iMRMC User Manual Version 2p1</h1>
posted 2014-01-15<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application_archive/iMRMC/iMRMCuserguide-v2p1.pdf'>Click to download user manual (iMRMCuserguide-v2p1.pdf)</a></b> <br>

<br>
<br>

<h1>iMRMC Application Version 2p0</h1>
posted 2013-08-28<br>
<br>
This version includes graphics to check data input and plots of ROC curves. <br>
This version also allows for arbitrary study designs. <br>
This version requires a database folder named "DB" with .jdb files in the application startup directory.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application_archive/iMRMC/iMRMC-v2p0.jar'>Click to download application (imrmc-v2p0.jar)</a></b>

<br>
<h1>iMRMC User Manual Version 2p0</h1>
posted 2013-08-28<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application_archive/iMRMC/iMRMCuserguide-v2p0.pdf'>Click to download user manual (imrmcuserguide-v2p0.pdf)</a></b> <br>