require(iMRMC)
setwd('/Users/stevenhurwitt/Documents/Accudata/MRMC')
load("~/Documents/Accudata/MRMC/doug data ws.RData")
#save.image("~/Documents/Accudata/MRMC/doug data ws.RData")

#### read data ####
fake = read.csv("fake88_nurse6_n252_delta15.csv", header = T, sep = ",")
raw = read.csv("raw88_nurse6_n252_delta15.csv", header = T, sep = ",")

#fake = fake[,-1]
#raw = raw[,-1]

#### calc percents by reader ####
tapply(raw$Score, raw$ReaderID, FUN = sum)
tapply(fake$Score, fake$ReaderID, FUN = sum)/84

### run MRMC and save variance, auc est, BDG df ###
result = doIMRMC(fake)
result

auc = result$Ustat$AUCA
auc.var = sum(result$Ustat$varAUCA)
df.BDG.U = result$Ustat$dfBDG

#### t-test with BDG df, auc & auc variance ###
test = sqrt((auc - .8)^2/auc.var)
2*(1 - pt(test, floor(df.BDG.U)))

##confidence int (should match results)
t.cutoff = qt(.975, floor(df.BDG.U))
auc + t.cutoff*sqrt(auc.var)
auc - t.cutoff*sqrt(auc.var)

result$Ustat$topCIBDG
result$Ustat$botCIBDG

#### save variance decomp & coeff,                   ####
#### use moments approach to calc var (sanity check) ####
result$varDecomp$BDG$Ustat$comp$modalityA.NO_MOD

tot.varU = sum(result$varDecomp$BDG$Ustat$comp$modalityA.NO_MOD[1,])
#tot.varMLE = sum(result$varDecomp$BDG$MLE$comp$modalityA.NO_MOD[1,])

Ucoeff = as.numeric(result$varDecomp$BDG$Ustat$coeff$modalityA.NO_MOD[1,])
Umom = as.numeric(result$varDecomp$BDG$Ustat$comp$modalityA.NO_MOD[1,])

est.var = as.numeric(t(Ucoeff)%*%Umom)
est.var

tstat = eff.size/sqrt(est.var)
tstat

#### single modality power calc (replicates java app) ####

df.BDG.U
(est.var - result$Ustat$varAUCA) < .00001
std.err.U = sqrt(est.var)
effect = .12
alpha0 = .05


Power.Calc(df.BDG.U, std.err.U, effect, alpha0)

###### Use Beta CDF to calc noncentral F for power ######
#http://www.real-statistics.com/chi-square-and-f-distributions/noncentral-f-distribution/
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

###############################