getwd()
setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs')
load("~/Documents/Accudata/Baylabs/baylabs.RData")
require(iMRMC)

#read in raw data
clinical = read.csv('STUDY_ECHO_CLINPARM.csv', header = T)
params = unique(as.character(clinical$clinParm))
zero = unique(clinical$cardio1)[11]
clinical2 = clinical

#convert to binary (0 if inadequate)
clinical2$cardio1 = as.numeric(clinical$cardio1 != zero)
clinical2$cardio2 = as.numeric(clinical$cardio2 != zero)
clinical2$cardio3 = as.numeric(clinical$cardio3 != zero)
clinical2$cardio4 = as.numeric(clinical$cardio4 != zero)
clinical2$cardio5 = as.numeric(clinical$cardio5 != zero)

#average scores across all clinicians, turn to binary
data.mat = clinical2[,4:8]
raw.score = apply(data.mat, 1, mean)
Score = as.numeric(raw.score >= .8)
Score

sum(as.numeric(raw.score >= .5))

#make variables for modality and truth obs
Mod = rep("modalityA", length(Score))
Truth = rep("truth", length(Score))
#truthScorePerVar = c(rep(1, 64), rep(0, 8))
#truthScore = rep(truthScorePerVar, 10)
truthScore = c(rep(1, 576), rep(0, 144))
truthScore = rep(c(rep(1, 144), rep(0, 36)), 4)
#truthScore = rep(c(1,1,1,0), 180)

#combine variables to make master dataset
final = cbind(as.character(clinical$clinParm), paste("Reader", as.character(clinical$STUDY_NURSEID), sep = ""), paste("Actual", as.character(clinical$RECORD_ID), sep = ""), as.character(Mod), as.numeric(Score))
final3 = cbind(as.character(clinical$clinParm), paste("Reader", as.character(rev(clinical$STUDY_NURSEID)), sep = ""), paste("Actual", as.character(clinical$RECORD_ID + 38), sep = ""), as.character(Mod), as.numeric(Score))

final2 = cbind(as.character(clinical$clinParm), as.character(Truth), paste("Actual", as.character(clinical$RECORD_ID), sep = ""), as.character(Truth), truthScore)
final4 = cbind(as.character(clinical$clinParm), as.character(Truth), paste("Actual", as.character(clinical$RECORD_ID + 38), sep = ""), as.character(Truth), truthScore)

#doubled observations
baylabs.dbl= as.data.frame(rbind(final2, final4, final, final3))
colnames(baylabs.dbl) = c("Param","ReaderID", "CaseID", "Modality", "Score")
baylabs.dbl = baylabs.dbl[order(baylabs.dbl$CaseID, baylabs.dbl$Param),]

baylabs.dbl$Score = as.numeric(baylabs.dbl$Score) - 1

#normal dataset
baylabs = as.data.frame(rbind(final2, final))
colnames(baylabs) = c("Param","ReaderID", "CaseID", "Modality", "Score")

#change score to binary
baylabs$Score = as.numeric(baylabs$Score) - 1
aggregate(baylabs$Score, list(baylabs$ReaderID), table)

agree = function(parameter){
  bay.sub = subset(baylabs, baylabs$Param == parameter)
  bay.truth = subset(bay.sub, bay.sub$ReaderID == "truth")
  bay.reader = subset(bay.sub, bay.sub$ReaderID != "truth")
  return(sum(bay.truth$Score == bay.reader$Score)/length(bay.reader$Score))}

aucs = sapply(params, agree)
var.bdg = output$`AUC var`
df.bdg = output$`BDG df`
cutoff = qt(.975, df.bdg)
t.stat = sqrt((aucs - .8)^2/var.bdg)
p.vals = 2*(1 - pt(t.stat, df.bdg))
lb.ci = aucs - cutoff*

#save dataset as csv
write.table(baylabs, "baylabs.csv", col.names = T, row.names = F, sep = ",", quote = F)


#### loop analysis ####

#apply MRMC analysis function to every subset of 10 clin parameters
output = t(sapply(params, MRMC_fun))
output = as.data.frame(output)
colnames(output) = c("AUC", "AUC var", "BDG df", "p-value", "CI LB", "CI UB")
write.table(output, "MRMCresults.csv", col.names = T, row.names = T, sep = ",", quote = F)

### FINAL OUTPUT to Vineet ###
clin_param = params
conf_int = paste(output$`CI LB`, '%', '*', output$`CI UB`, '%', sep = '')
p_val = round(output$`p-value`, 4)

final = as.data.frame(cbind(clin_param, conf_int, p_val))

write.table(final, "MRMC_test_file.csv", col.names = T, row.names = F, sep = ",", quote = F)

#same analysis for the double obs data
output.dbl = t(sapply(params, MRMC_fun))
output.dbl = as.data.frame(output.dbl)
colnames(output.dbl) = c("AUC", "AUC var", "BDG df", "p-value", "CI LB", "CI UB")

#calculate power of first clin param
Power.Calc(output[1,3], sqrt(output[1,2]), .3, .05)

#test functions
test = subset(baylabs.quad[,-1], baylabs.quad$Param == params[1])
test.result = doIMRMC(test)

write.table(test, "testmrmc.csv", col.names = T, row.names = F, sep = ",", quote = F)

#function to perform MRMC analysis on subset of data, given parameter
MRMC_fun = function(parameter){
  tmp = subset(baylabs[,2:5], baylabs$Param == parameter)
  mrmc_result = doIMRMC(tmp)
  
  bay.sub = subset(baylabs, baylabs$Param == parameter)
  bay.truth = subset(bay.sub, bay.sub$ReaderID == "truth")
  bay.reader = subset(bay.sub, bay.sub$ReaderID != "truth")
  auc = sum(bay.truth$Score == bay.reader$Score)/length(bay.reader$Score)
  
  #auc = mrmc_result$Ustat$AUCA
  auc.var = sum(mrmc_result$Ustat$varAUCA)
  df.BDG.U = mrmc_result$Ustat$dfBDG
  
  t.cut = qt(.975, df.BDG.U)
  test = sqrt((auc - .8)^2/auc.var)
  p.val = 2*(1 - pt(test, floor(df.BDG.U)))
  
  ci.ub = round(100*(auc + t.cut*sqrt(auc.var)), 1)
  ci.lb = round(100*(auc - t.cut*sqrt(auc.var)), 1)
  print(paste('MRMC ran for clin param', parameter, 'outputting results.', sep = ' '))
  return(c(auc, auc.var, df.BDG.U, p.val, ci.lb, ci.ub))
}

#function to perform power calculations
Power.Calc = function(df, SE, eff, alpha){
  require(Rmpfr)
  F.vals = numeric(70)
  ints = seq(0:70) - 1
  
  t.stat = eff/SE
  lambda = t.stat^2
  cutoff = qf((1-alpha), 1, floor(df))
  d1 = 1
  d2 = df - 1
  
  s1 = as.bigz((lambda/2)^(ints))
  s2 = factorialZ(ints)
  s3 = exp(-lambda/2)
  
  q = cutoff*d1/(cutoff*d1+d2)
  betas = pbeta(q, d1/2 + ints, d2/2)
  
  scale = s1 %/% s2
  F.val = s3*as.numeric(scale)*betas
  power = 1 - sum(F.val)
  print(paste("Power calculated to be: ", round(power, 4)))
  return(power)}
