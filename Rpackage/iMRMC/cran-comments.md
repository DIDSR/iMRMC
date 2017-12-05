## Test environments
* devtools::build_win() on 12/4/2017
* Fedora 24, kernel 4.10.17, R 3.5.0.73808 (11/30/2017)

## R CMD check results
There were no ERRORs or WARNINGs or NOTEs on linux.

There were no ERRORs or WARNINGs on windows. There was one NOTE on windows:

* checking CRAN incoming feasibility ... NOTE

Maintainer: 'Brandon Gallas <Brandon.Gallas@fda.hhs.gov>'

New submission

Possibly mis-spelled words in DESCRIPTION:
  Multi (3:8, 3:22)
  ROC (3:51, 9:23)

      * I hope the "mis-spelled" words can remain in DESCRIPTION. "Multi" is a well-known combining form used in the formation of compound words. "ROC" is a well-known abbreviation for Receiver Operating Characteristic that is used in the description of other CRAN packages.
      
* installed size is 8.6Mb ... sub-directories of 1Mb or more: java 8.5Mb
    
     * The core statistical analysis is executed by a validated java application (same author). We decided to implement the R package by calling the java application rather than porting all the source code (and creating mistakes). The main work flow has three steps: 1) write an input data file to the R session's temporary directory, 2) launch the .jar application that reads the input file and writes the results to an output directory (also in the R session's temporary directory), 3) read in the results from the output directory into an R object. Including the java application causes the size of the R package to be bigger and slower than one might expect.

## Downstream dependencies
This is a new submission and there are no packages known to depend on the current submission.