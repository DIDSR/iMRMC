<h2>Updates</h2>

<h4>iMRMC2p8</h4>
*12/1/15*

Users can click on "Save to File" button to export the statistical analysis summary to a file. Also users can input reader study analysis summary file (via "Select an input method") instead of reader study scores to view analysis results and size a future study.

The input file format can be imrmc, csv and omrmc.

Software remembers last loaded and saved file directory and goes to the that directory next time.

Software can plot ROC curves from multiple modalities in one figure and export ROC curve data.

In study Design summary, user can view and export the lookup tables showing the relationships between (user-input) IDs and (software labeled) indicies for readers and cases.

Minor changes for gui interface.


<h4>iMRMC2p7</h4>
*1/22/15*

Update calculation of DF_BDG, degrees of freedom described in Obuchowski2012_Acad-Radiol_v19p1508, to treat unpaired readers, normal cases, or disease cases. Also implemented a minimum DF_BDG according to Gaylor1969_Technometrics_v4p691 Eq. 5. 

Bug Fix: DF_BDG estimates less than 2 can't be treated by the implementation of Student T distribution. So DF_BDG is not allowed to fall below 2. 

Bug Fix: When DF_Hillis is infinity (ms_tr=0.0), some users reported an application crash. So DF_Hillis is set to 50 in this case, and you get the same effect.Bug Fix.


<h4>iMRMC2p6</h4>
*9/5/14*

Sizing analysis functioning. We believe it is functioning properly and are planning a validation paper. We have also added a button to view the individual reader AUCs. Analysis of individual reader AUCs is planned. 


<h4>iMRMC2p5</h4>
*8/14/14*

Fixed bug that was causing iMRMC to crash when executing statistical analysis of a single modality AUC.


<h4>iMRMC2p4</h4>
*3/28/14*

Power and sizing analysis disabled while it goes through a detailed validation. 
Database input method disabled as it undergoes a remake. Consequently, this version does not require a database folder or .jdb files.


<h4>iMRMC2p3</h4>
*3/5/14*

This version provides robust data check and error reporting during the reading of the InputFile.imrmc (Identify missing truth, duplicate cases, replicated observations). Additionally, readerIDs, caseIDs, and modalityIDs can be strings. These changes allow for more flexibility and should help the user determine problems with the file format. 

There was a bug that was found and fixed: the coefficients were incorrectly calculated when the data was not fully-crossed. 

Power for sizing to come. 

Sample files are now provided. 

This version requires a database folder named "DB" with .jdb files in the application startup directory.


<h4>iMRMC2p1</h4>
*1/15/14*

This version fixes p-values and confidence intervals in the statistical analysis. 

Power for sizing to come. 

This version requires a database folder named "DB" with .jdb files in the application startup directory.


<h4>iMRMC2p0</h4>
*8/28/13*

This version includes graphics to check data input and plots of ROC curves. 

This version also allows for arbitrary study designs. 

This version requires a database folder named "DB" with .jdb files in the application startup directory.




