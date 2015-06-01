### Summary ###
iRoeMetz is a Java application which can be used to simulate the results of a number of experiments via Monte Carlo methods given variance components of a theoretical study. These experiment results can be used in MRMC variance analysis programs such as iMRMC.

<br>
For more information and when publishing results using our software, please reference with one of the following:<br>
<ul><li>Gallas, B. D. & Hillis, S. L. (Accepted 2014), 'Generalized Roe and Metz ROC Model: Analytic Link Between Simulated Decision Scores and Empirical AUC Variances and Covariances.' <i>JMI</i>, <b>1</b> (3), proofs. <b><a href='http://imrmc.googlecode.com/svn/standalone_application/docs/Gallas2014_JMI_v1proofs.pdf'>[Gallas2014_JMI_v1proofs.pdf</a>]</b></li></ul>

<br>
<h1>User Manual</h1>
Please <b>read</b> the user manual for useful information. See below for the current version of the user manual. The user manual contains<br>
<br>
<ul><li>Introduction to ROC Reader Studies</li></ul>

<ul><li>Discussion of the Roe and Metz Simulation Model</li></ul>

<ul><li>How to use the iRoeMetz application</li></ul>

<ul><li>References</li></ul>

<br>

<h3><i>Authors</i></h3>
<ul><li>Rohan Pathare<br>
</li><li>Brandon D. Gallas, PhD<br>
</li><li>Xin He, PhD</li></ul>

<br>
<h1>Current Version</h1>
Version v1p2 posted 2014-04-08<br>
<br>
Simulation configuration files were created to replicate the simulations in Roe1997_Acad-Radiol_v4p298 and are now available. When 10,000 simulations were run, the application crashed. The corresponding memory problem was fixed.<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iRoeMetz-v1p2.jar'>Click to download application (iRoeMetz-v1p2.jar)</a></b>

<b><a href='http://imrmc.googlecode.com/svn/standalone_application/iRoeMetzUserManual-v1p0.pdf'>Click to download user manual (iRoeMetzUserManual-v1p0.pdf)</a></b> <br>

<br>

<h2>Simulation Configurations</h2>
iRoeMetz allows for using simulation configuration files. Here are the simulation configurations that replicate the original Roe and Metz simulations (Roe, Acad Radiol, 1997, v4 p298).<br>
<br>
<br>
<b><a href='http://imrmc.googlecode.com/svn/standalone_application/FilesTest-iRoeMetz/'>Click to download simulation configuration files (*.irm)</a></b>

<br>

<h1><a href='iMRMCVersionHistory.md'>Download Previous Versions</a></h1>
<br>

<h1><a href='http://imrmc.googlecode.com/svn/'>Direct Access to Repository Contents</a></h1>

<br>
<br>
<br>

<h4>View the Javadoc for the source code <a href='http://imrmc.googlecode.com/svn/javadoc/index.html'>here</a></h4>

<a href='http://imrmc.googlecode.com/svn/wiki/gadgets/printwikis.html?_gnpi_subversion="svn"&_gnpi_wikilist="iRoeMetzMethods|iRoeMetzUsage|iRoeMetzReferences"'>Print Me</a>

<br><br>

<h2>This code uses the MersenneTwister random number generator</h2>
Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji Nishimura, All rights reserved.<br>
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:<br>
<br>
<blockquote>Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.<br>
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.<br>
The names of its contributors may not be used to endorse or promote products derived from this software without specific prior written permission.</blockquote>

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.