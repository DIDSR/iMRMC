require(iMRMC)
require(ggplot2)
getwd()
setwd('/Users/stevenhurwitt/Documents/R programming/MRMC')
load("~/Documents/R programming/MRMC/MRMC ws.RData")

# Create a sample configuration object
config <- sim.gRoeMetz.config()
# Simulate an MRMC ROC data set
dFrame.imrmc <- sim.gRoeMetz(config)
# Analyze the MRMC ROC data
result <- doIMRMC(dFrame.imrmc)

###############################

#create dataframe
simRoeMetz.config <- sim.gRoeMetz.config()
df.MRMC <- sim.gRoeMetz(simRoeMetz.config)

#remove ground truth obs
df.readers = df.MRMC[81:880,]
df.readers$readerID = as.factor(df.readers$readerID)
index = seq(1:800)
df.readers$index = index

#plot scores colored by reader
ggplot(df.readers, aes(x = index, y=score, color = readerID)) + geom_point()


#simulate data
df <- undoIMRMCdf(df.MRMC)
df <- droplevels(df[grepl("pos", df$caseID), ]) #keep only positives

#### uStat11.jointD.identity ####
# Calculate the reader- and case-averaged difference in scores from testA and testB
# (kernelFlag = 1 specifies the U-statistics kernel to be the identity)
result.jointD.identity <- uStat11.jointD(
  df,
  kernelFlag = 1,
  keyColumns = c("readerID", "caseID", "modalityID", "score"),
  modalitiesToCompare = c("testA", "testB"))

cat("\n")
cat("uStat11.jointD.identity \n")
print(result.jointD.identity[1:2])
