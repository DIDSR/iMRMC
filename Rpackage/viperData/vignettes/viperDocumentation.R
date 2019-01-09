# This file creates pdf and html versions of the R help.

# The R help files are created when a package is compiled.
# They are found in the "man" directory and have an .Rd extension.

# To convert the .Rd files to .html, we use the function
# tools::Rd2HTML(inFile, out = outFile)

# To convert the .Rd files to .pdf, we use the function
# system("R CMD Rd2pdf --help")

# Rd2pdf did not work because it was missing latex style files.
# It was necessary to download and install latex style files.
#
# Download from https://www.ctan.org/tex-archive/systems/win32/miktex/tm/packages:
# These two files are prerequisites for establishing a "Package Repository":
# "C:\Users\BDG\Downloads\MiKTexPackages\miktex-zzdb1-2.9.tar.lzma"
# "C:\Users\BDG\Downloads\MiKTexPackages\miktex-zzdb2-2.9.tar.lzma"
# These three files were the latex style files.
# "C:\Users\BDG\Downloads\MiKTexPackages\mptopdf.tar.lzma"
# "C:\Users\BDG\Downloads\MiKTexPackages\url.tar.lzma"
# "C:\Users\BDG\Downloads\MiKTexPackages\inconsolata.tar.lzma"
#
# In MiKTex Package Manager (should be in the Start menu under your MiKTex folder)
# click Repository -> Change Package Repository ->
# “Packages shall be installed from a directory.”
# then Next -> select the location of the MikTexPackagesfolder -> Finish
#
# Then the packages can be installed manually with the package manager
# or interactively when prompted by R CMD Rd2pdf

# Make html documentation ####

# Get the directories
packageDir <- getwd()
manDir <- file.path(packageDir, "man")

# # Get the .Rd files and create .html files
# files.Rd <- list.files(manDir, pattern = ".Rd", full.names = TRUE)
# files.html <- gsub(".Rd", ".html", files.Rd, fixed = TRUE)
#
# # Convert the .Rd files to .html files
# for (i in 1:length(files.Rd)) {
#   tools::Rd2HTML(files.Rd[i], out = files.html[i])
# }

htmlDir <- file.path(find.package("viperData"), "html")
filesToCopy <- list.files(htmlDir, full.names = TRUE)
file.copy(from = filesToCopy, to = manDir,
          overwrite = TRUE, recursive = FALSE, copy.mode = TRUE)

#### Rdconv help####
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

#### Make pdf documentation ####
command.package <- paste("R CMD Rd2pdf ",
                         manDir, " ",
                         "--no-preview ",
                         "--output=", file.path(manDir, "000viperDataDocumentation.pdf "),
                         "--force ",
                         "--title=viperData",
                         sep = "")
system(command.package)

#### Rd2pdf help ####
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
