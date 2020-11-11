# General submission notes

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

# Notes Submission 12/12/2020

## Prepare with
* devtools::spell_check()
* rhub::check_rhub()
* devtools::check_win_devel()

## Submit with
```
> devtools::release()
Have you checked for spelling errors (with `spell_check()`)?
1: Uhhhh... Maybe?
2: Of course
3: I forget

Selection: 2
Have you run `R CMD check` locally?
1: Absolutely
2: I forget
3: Nope

Selection: 1
── Running additional devtools checks for iMRMC ────────────────────────────────
Checking version number has three components... OK
Checking dependencies don't rely on dev versions... OK
Checking NEWS.md is not ignored... OK
Checking NEWS.Rd does not exist... OK
Checking DESCRIPTION doesn't have Remotes field... OK
────────────────────────────────────────────────────────────────────────────────
Were devtool's checks successful?
1: Definitely
2: I forget
3: Not yet

Selection: 1
Have you checked on R-hub (with `check_rhub()`)?
1: Of course
2: Nope
3: Not yet

Selection: install.packages("rhub")
Enter an item from the menu, or 0 to exit
Selection: 1
Have you checked on win-builder (with `check_win_devel()`)?
1: Definitely
2: Uhhhh... Maybe?
3: I forget

Selection: 1
Have you updated `NEWS.md` file?
1: Of course
2: Not yet
3: Uhhhh... Maybe?

Selection: 1
Have you updated `DESCRIPTION`?
1: Not yet
2: Absolutely
3: Nope

Selection: 2
Have you updated `cran-comments.md?`
1: I forget
2: Absolutely
3: No

Selection: 2
Is your email address Brandon.Gallas@fda.hhs.gov?
1: No
2: Yeah
3: Not yet

Selection: 2
Building
```