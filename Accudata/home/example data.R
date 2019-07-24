require(iMRMC)
setwd('/Users/stevenhurwitt/Documents/R programming/MRMC')
#https://github.com/DIDSR/iMRMC/wiki/MRMC-analysis-of-binary-data

vars = c("readerID", "caseID", "modalityID", "score")

#make normal dataframe
c1 = c("Actual1", "Actual2", "Actual3", "Actual4", "Actual5", "Actual6")
c2 = c("Correct", "Correct", "Correct", "Correct", "Correct", "Correct")
c3 = c("Incorrect", "Correct", "Incorrect", "Correct", "Incorrect", "Correct")
c4 = c("Correct", "Incorrect", "Correct", "Incorrect", "Correct", "Incorrect")
c5 = c("Incorrect", "Incorrect", "Incorrect", "Correct", "Correct", "Correct")

Actual1 = c(paste("Actual10", (seq(1:10)-1), sep = ""), paste("Actual1", (9+seq(10:99)), sep = ""))
Actual2 = c(paste("Actual20", (seq(1:10)-1), sep = ""), paste("Actual2", (9+seq(10:99)), sep = ""))
Actual3 = c(paste("Actual30", (seq(1:10)-1), sep = ""), paste("Actual3", (9+seq(10:99)), sep = ""))
CaseID = rep(c(Actual1, Actual2, Actual3),2)

ex.data = data.frame(cbind(c1, c2, c2, c2, c3, c4, c5))
colnames(ex.data) = c("Case ID", "Reader 1", "Reader 2", "Reader 3", "Reader 4", "Reader 5", "Reader 6")

#create variables for dummy data
readerID = c(rep("truth", 12), rep("Reader1", 12), rep("Reader 2", 12), rep("Reader 3", 12),
                 rep("Reader 4", 12), rep("Reader 5", 12), rep("Reader 6", 12))

cases = c(paste("Actual", seq(1:6)), paste("Fake", seq(1:6)))
caseID = rep(cases, 7)

modalityID = c(rep("truth", 12), rep("Modality A", 72))         

score = c(rep(1, 6), rep(0, 6), rep(1, 6), rep(.5, 6), rep(1, 6), rep(.5, 6), rep(1, 6), rep(.5, 6), c(0, 1, 0, 1, 0, 1), rep(.5, 6),
          c(1, 0, 1, 0, 1, 0), rep(.5, 6), c(0, 0, 0, 1, 1, 1), rep(.5, 6))

bin.data = data.frame(cbind(readerID, caseID, modalityID, score))

result <- doIMRMC(bin.data)
