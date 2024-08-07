<h2>Updates</h2>

<h4>Java Version Frozen</h4>
*04/20204*

We are no longer developing the Java version of iMRMC. We are only developing the R package. Please refer to the [NEWS.md](https://github.com/DIDSR/iMRMC/blob/master/Rpackage/iMRMC/NEWS.md) for more information regarding the change. 


<h4>iMRMC4.0.3</h4>
*04/15/19*

When running iMRMC by the command line, do not pop-up any warning or error windows. Instead, display all the warning and error messages in the command line. When errors occur, the software is forced to quit.

Update "analysis all modalities" to analyze all the modalities in an input file. Previously, it only analyzed the first two.

Fix the help button. The help button used to point to the wrong documentation. 


<h4>iMRMC4.0.0</h4>
*12/11/18*

iMRMC.jar: 
  * Set the locale to be US so that csv output files are not corrupted by the European convention of using a comma to distinguish decimal values. 
  * Fix show reader AUCs button error. The AUCB results were mistakenly duplicated in the position corresponding to AUCAminusAUCB. 

iMRMC.R:
  * Allow users to input a file name of a data set as an alternative to a data frame.
  * It reads and writes intermediate files to the R session temporary directory.
  * Additional edits to satisfy CRAN submission requirements and feedback.  


<h4>iMRMC3p3</h4>
*10/6/17*

Fix bugs for output files (comma, Index&ID relationship).

In command line execution, quiet the warnings about too few observations. 


<h4>iMRMC3p2</h4>
*2/24/17*

In command-line execution, the user can specify the directory for the output files.

Estimate covariances for each reader-modality pair. Results are exported by "Analysis All Modalities" buttons. This covariance matrix can be used for the Obuchowski Rockette method (Obuchowski1995_Commun-Stat-Simulat_v24p285).

Add MLE option for Sizing prediction.

Reorganize GUI to make it more compact.

For truth rows in input file, reader ID can be either "-1" or "truth".


<h4>iMRMC3p1</h4>
*1/12/17*

Variance estimates for individual readers can now be found by the "Show Reader AUCs" button.

A pdf version of the statistical analysis is available by the " Save Stat Analysis". The button also exports summary .omrmc and .csv files.


<h4>iMRMC3p0</h4>
*7/14/16*

Add split-plot and unpaired study design options for sizing analysis.

Add “Explore Experiment Size” button to predict multiple size studies of variance and power.

Update output files:
  * Add “Save Stat” button to save statistical analysis results into one line and export to disk for easier reading by other software.
  * Add “Save Size” button to export sizing analysis results to disk.
  * Add “Save All Stat” button to export analysis results from all modalities and all combinations of modalities. This includes variance components, individual reader AUCs and all the ROC curves. Most data are saved as .csv files for easier reading by other software.

Add option to run iMRMC in command line mode. Software will do analysis and export the same results as the "Save All Stat" button.


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




