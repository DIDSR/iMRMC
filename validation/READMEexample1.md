<h5>Run iRoeMetz validation in parallel in linux </h5>

1. Create a new folder “allinput” for all configure input files (e.g. /raidb/qigong/imrmc/allinput). 
Sample input .irm files are available in https://github.com/DIDSR/iMRMC/tree/master/validation/sampleFiles/inputFiles. 

2. Save iReoMetz.jar file in any directory 

3. Create .txt file in the same directory as iReoMetz.jar to run the input files in parallel. 
In this file each line is for one configure input file. The format should be:
export DISPLAY=":0.0"; java -jar iRoeMetz-v2p1.jar + “input file directory and name”. 
A sample file "runInParallel.txt" file is available in https://github.com/DIDSR/iMRMC/tree/master/validation/sampleFiles.

4. Open terminal and login but don’t get into any node.

5. Visit iReoMetz.jar directory in terminal and input command “swarm runInParallel.txt”

6. Software will create a new folder “output” in input files directory and save 3 output files for each input file : 
  1.each trials’ result (001Trail.csv); 
  2.MC simulation result (001SimulationOutput.omrmc); 
  3.Numerical result (001NumericalOutput.omrmc)

Note: the terminal will show how many input file has finished.
