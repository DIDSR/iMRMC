# This file creates pdf and html versions of the R help.

# system("R CMD Rdconv --help")

packageDir <- file.path("C:", "Users", "BDG", "Documents", "000_github",
                        "DIDSR.iMRMC.trunk", "Rpackage", "viperData")
manDir <- file.path(packageDir, "man")

root <- file.path(manDir, "dmistData")

convertRd <- function(root){

  file.Rd <- paste(root, ".Rd", sep = "")
  file.pdf <- paste(root, ".pdf", sep = "")
  file.html <- paste(root, ".html", sep = "")

  tools::Rd2HTML(file.Rd, out = file.html)

  command.package <- paste("R CMD Rd2pdf ",
                           paste(root, ".Rd ", sep = ""),
                           "--no-preview ",
                           "--output=", file.pdf, " ",
                           "--force",
                           sep = "")
  system(command.package)

}

root <- file.path(manDir, "dmistData")
convertRd(root)
root <- file.path(manDir, "viperObservations")
convertRd(root)
root <- file.path(manDir, "viperReaderQualifications")
convertRd(root)
root <- file.path(manDir, "viperSummary")
convertRd(root)

#### Rdconv ####
# > system("R CMD Rdconv --help")
# Usage: R CMD Rdconv [options] FILE
#
# Convert R documentation in FILE to other formats such as plain text,
# HTML or LaTeX.
#
# Options:
#   -h, --help		print short help message and exit
# -v, --version		print version info and exit
# -t, --type=TYPE	convert to format TYPE
# --encoding=enc        use 'enc' as the output encoding
# --package=pkg         use 'pkg' as the package name
# -o, --output=OUT	use 'OUT' as the output file
# --os=NAME		assume OS 'NAME' (unix or windows)
# --OS=NAME		the same as '--os'
# --RdMacros=pkglist
# packages from which to get Rd macros
#
# Possible format specifications are 'txt' (plain text), 'html', 'latex',
# and 'example' (extract R code in the examples).
#
# The default is to send output to stdout, which is also given by '-o -'.
# Using '-o ""' will choose an output filename by removing a '.Rd'
# extension from FILE and adding a suitable extension.
#
# Report bugs at <https://bugs.R-project.org>.




#### Rd2pdf ####
# > system("R CMD Rd2pdf --help")
# Usage: R CMD Rd2pdf [options] files
#
# Generate PDF output from the Rd sources specified by files, by
# either giving the paths to the files, or the path to a directory with
# the sources of a package, or an installed package.
#
# Unless specified via option '--output', the basename of the output file
# equals the basename of argument 'files' if this specifies a package
# or a single file, and 'Rd2' otherwise.
#
# The Rd sources are assumed to be ASCII unless they contain \encoding
# declarations (which take priority) or --encoding is supplied or if using
# package sources, if the package DESCRIPTION file has an Encoding field.
# The output encoding defaults to the package encoding then to 'UTF-8'.
#
# Files are listed in the order given: for a package they are in alphabetic
# order of the \name sections.
#
# Options:
#   -h, --help		print short help message and exit
# -v, --version		print version info and exit
# --batch		no interaction
# --no-clean	do not remove created temporary files
# --no-preview	do not preview generated PDF file
# --encoding=enc    use 'enc' as the default input encoding
# --outputEncoding=outenc
# use 'outenc' as the default output encoding
# --os=NAME		use OS subdir 'NAME' (unix or windows)
# --OS=NAME		the same as '--os'
# -o, --output=FILE	write output to FILE
# --force		overwrite output file if it exists
# --title=NAME	use NAME as the title of the document
# --no-index	do not index output
# --no-description	do not typeset the description of a package
# --internals	typeset 'internal' documentation (usually skipped)
# --build_dir=DIR	use DIR as the working directory
# --RdMacros=pkglist
# packages from which to get Rd macros
#
# The output papersize is set by the environment variable R_PAPERSIZE.
# The PDF previewer is set by the environment variable R_PDFVIEWER.
#
# Report bugs at <https://bugs.R-project.org>.

#### TEST ####
# command.Rdconv <- 'R CMD Rdconv -t html -o "" '
#
# system(paste(command.Rdconv, file.path(packageDir, "man", "dmistData.Rd")))
#
#
# command.package <- paste("R CMD Rd2pdf", file.path("C:", "Users", "BDG", "Documents", "000_svn", "viper", "Rpackage", "viperData", "man"))
# system(command.package)
#
# command.dmistData <- paste("R CMD Rd2pdf", file.path("C:", "Users", "BDG", "Documents", "000_svn", "viper", "Rpackage", "viperData", "man", "dmistData.Rd"))
# system(command.dmistData)
#
#
# command2 <- paste("R CMD Rd2html", file.path("C:", "Users", "BDG", "Documents", "000_svn", "viper", "Rpackage", "viperData", "man", "dmistData.Rd"))
#
