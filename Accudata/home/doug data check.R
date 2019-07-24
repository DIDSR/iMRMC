require(iMRMC)
setwd('/Users/stevenhurwitt/Documents/Rprogramming/MRMC')
load("~/Documents/Rprogramming/MRMC/doug data check.RData")

#read data, find counts of success
out = read.csv("out.csv")
apply(out, 2, mean)

times = as.double(apply(out, 2, sum))

#generate bin labels based on successes
ones = sapply(times, function(x) rep(1, x))
zeros = sapply((39 - times), function(x) rep(0, x))
ind = seq(1:length(times))

#function to make vector of successes
data.vec = numeric()
for (j in 1:length(times)){
  new = c(ones[[j]], zeros[[j]])
  data.vec = append(data.vec, new)
}

data.vec

#variables for dataset

dat = append(c(rep(1, 20), rep(0, 19)), data.vec)
caseid = paste("Actual", seq(1:39), sep = "")
caseid = rep(caseid, 6)
mod = rep("ModalityA", 234)
readid = rep("truth", 39)
readid2 = paste(rep("reader", 195), sort(rep(ind, 39)), sep = "")
reader = append(readid, readid2)

#make dataset, name vars
data.mat = data.frame(cbind(reader, caseid, mod, dat)) 
colnames(data.mat) = c("ReaderID", "CaseID", "Modality", "Score")
data.mat
write.table(data.mat, "output.csv", quote = F, sep = ",", col.names = T, row.names = F) 

##### run MRMC ####  
results = doIMRMC(data.mat)

dfBDG = as.numeric(results$Ustat$dfBDG)
SE = sqrt(as.numeric(results$Ustat$varAUCA))
dfBDG
SE

Power.Calc(dfBDG, SE, .10, .05)

###
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
