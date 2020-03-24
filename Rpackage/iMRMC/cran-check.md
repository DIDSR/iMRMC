
Download and install the current r-devel distribution.
* https://cran.r-project.org/bin/windows/base/rdevel.html
* Check version with "version" command in the console

Tools->Global Options
* Change the R version to "[64-bit] C:\Program Files\R\R-devel"

Build->Configure Build Tools
* Add "--as-cran" to "Check Package - R CMD check addtional options:"

Fix errors, warnings, and notes

Build source package

Upload to https://cran.r-project.org/submit.html