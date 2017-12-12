## Test environments
* devtools::build_win() on 12/4/2017
* Fedora 24, kernel 4.10.17, R 3.5.0.73808 (11/30/2017)

## Submission 2
CRAN feedback from submission 1 and responses.

#### 1. Please add a reference for the methods in the 'Description'  
   field of your DESCRIPTION file in the form  
   authors (year) <doi:...>  
   authors (year) <arXiv:...>  
   authors (year, ISBN:...)  
   with no space after 'doi:', 'arXiv:' and angle brackets for auto-linking.
   
#### R1. References included with doi formatted as above.


#### 2. Please reduce the exucation time of your examples to less than 5 sec per Rd-file. We get on our Linux machine:

    Examples with CPU or elapsed time > 10s  
                           user system elapsed  
     sim.gRoeMetz.config 10.232  0.748   5.885  
     simMRMC             10.028  0.756   5.808
   
#### R2.I have reduced the sample size of my examples shuch that the elapsed time is only half. I hope this has a similar impact on the user/CPU time, but I can't tell since my user/CPU time is very different from the CRAN Linux machine. The proc.time() results I get before and after the reduction are the following:
  
    * Before size reduction: 0.21    0.03    2.92 
    * After  size reduction: 0.18    0.03    1.27 

## Submission 1
R CMD check results

There were no ERRORs or WARNINGs or NOTEs on linux.

There were no ERRORs or WARNINGs on windows. There was one NOTE on windows:

* checking CRAN incoming feasibility ... NOTE

   Maintainer: 'Brandon Gallas <Brandon.Gallas@fda.hhs.gov>'

   New submission

   Possibly mis-spelled words in DESCRIPTION:
   Multi (3:8, 3:22)
   ROC (3:51, 9:23)

  * I hope the "mis-spelled" words can remain in DESCRIPTION. "Multi" is a well-known combining form used in the formation of compound words. "ROC" is a well-known abbreviation for Receiver Operating Characteristic that is used in the description of other CRAN packages.

* checking installed package size ... NOTE
  installed size is  5.0Mb
  sub-directories of 1Mb or more:
    java   4.9Mb
    
  * The core statistical analysis is executed by a validated java application (same author as this R package). We decided to implement the R package by calling the java application rather than porting all the source code (and creating mistakes). The main work flow has three steps: 1) write an input data file to the R session's temporary directory, 2) launch the .jar application that reads the input file and writes the results to an output directory (also in the R session's temporary directory), 3) read in the results from the output directory into an R object. Including the java application causes the size of the R package to be bigger and slower than one might otherwise expect.

## Downstream dependencies
This is a new submission and there are no packages known to depend on the current submission.