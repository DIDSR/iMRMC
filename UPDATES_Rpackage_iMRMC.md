# Updates

Please refer to this file: https://github.com/DIDSR/iMRMC/blob/master/Rpackage/iMRMC/NEWS.md

# Coming soon

We are rewriting do_IMRMC to use only R code and not call the iMRMC java app. The iMRMC java app is big (4MB) and slow (R code writes ROC data to a file, java app loads, java app reads ROC data from the file, java app processes ROC data, java app writes results to several files, R code reads results from the several files). 
* The ROC curves have minor differences that are due to java app rounding numbers when writing the results to files. Also, the objects containing the ROC curves have inconsequential differences in their attributes arising from the results being read from files.
