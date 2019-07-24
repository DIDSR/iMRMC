require(iMRMC)
setwd('/Users/stevenhurwitt/Documents/Accudata/MRMC')
load("~/Documents/Accudata/MRMC/doug data ws2.RData")

##### Read data & run iMRMC #####

data = read.csv("fake pilot.csv", header = T, sep = ",")

data2 = read.csv("fake88_nurse6_n252_delta15.csv", header = T, sep = ",")
data2 = data2[,-1]

result = doIMRMC(data)
result2 = doIMRMC(data2)
result

##### MRMC Analysis (uses random effects for reader skill & case difficulty) #####

Ustat = uStat11.jointD(data, keyColumns = c("ReaderID", "CaseID", "Modality","Score"), 
             modalitiesToCompare = c("modalityA", "modalityB"))

Ustat2 = uStat11.conditionalD(data, keyColumns = c("ReaderID", "CaseID", "Modality","Score"), 
                       modalitiesToCompare = c("modalityA", "modalityB"))



##### MLE variances #####
var.est = as.matrix(result$varDecomp$BDG$MLE$comp$modalityA.modalityB) %*% t(as.matrix(result$varDecomp$BDG$MLE$coeff$modalityA.modalityB))
std.err = sqrt(var.est)

var.est[1,1] + var.est[2,1] - 2*var.est[3,1] #var of A - B
sqrt(var.est[1,1] + var.est[2,1] - 2*var.est[3,1])

var.est[1,1] #var A
var.est[2,1] #var B

###### U stat AUC values #####
colMeans(Ustat$design.A)

AUC.A = result$Ustat$AUCA[1] #modality A

AUC.B = result$Ustat$AUCA[2] #modality B

AUC.diff = AUC.A - AUC.B #diff in modality

AUC.diff == result$Ustat$AUCAminusAUCB[3]

AUC.var = result$Ustat$

###### U stat variances #####
var.est2 = as.matrix(result$varDecomp$BDG$Ustat$comp$modalityA.modalityB) %*% t(as.matrix(result$varDecomp$BDG$Ustat$coeff$modalityA.modalityB))
var.est2

AUC.var = var.est2[1,1] + var.est2[2,1] - 2*var.est2[3,1]
SE.tot = sqrt(AUC.var) #var A - B
sqrt(var.est2[1,3] + var.est2[2,3] - 2*var.est2[3,3])

var.A = var.est2[1,1] #var A
var.B = var.est2[2,1] #var B

##### t-test for AUC's #####

##function to calc p-value given auc, var, df, alpha and null
t.test.AUC = function(auc, auc.var, df, null){
  t.stat = sqrt((auc - null)^2/auc.var)
  return(2*(1 - pt(t.stat, floor(df))))
}

dfBDG = as.numeric(result$Ustat$dfBDG[3])
df.A = result$Ustat$dfBDG[1]
df.B = result$Ustat$dfBDG[2]

AUC.diff
AUC.var

t.test.AUC(AUC.diff, AUC.var, dfBDG, 0)

t.test.AUC(AUC.A, var.A, df.A, .5)

t.test.AUC(AUC.B, var.B, df.B, .5)

#### CI for AUC's ####
t.cutoff = qt(.975, floor(dfBDG))
low.diff = AUC.diff - t.cutoff*SE.tot
high.diff = AUC.diff + t.cutoff*SE.tot

t.cutoff.A = qt(.975, floor(df.A))
low.A = AUC.A - t.cutoff.A*sqrt(var.A)
high.A = AUC.A + t.cutoff.A*sqrt(var.A)

t.cutoff.B = qt(.975, floor(df.B))
low.B = AUC.B - t.cutoff.B*sqrt(var.B)
high.B = AUC.B + t.cutoff.B*sqrt(var.B)

low.CI = c(low.A, low.B, low.diff)
high.CI = c(high.A, high.B, high.diff)

low.CI
high.CI
result$Ustat$botCIBDG
result$Ustat$topCIBDG

##### Kappa #####
R1 = data[data$ReaderID %in% levels(data$ReaderID)[1],]
R1 = droplevels(R1)

R1.cor = xtabs(Score~Modality, data = R1)
R1.wrong = 300 - R1.cor

cont.tab = rbind(R1.cor, R1.wrong)
N = sum(cont.tab)

cor.tab = xtabs(Score~Modality+ReaderID, data = data)[-3,-4]
cor.tab
cor.tab2 = rbind(cor.tab, colSums(cor.tab))
cor.tab3 = cbind(cor.tab2, rowSums(cor.tab2))
colnames(cor.tab3) = c("Reader1", "Reader2", "Reader3", "modTotal")
row.names(cor.tab3) = c("modalityA", "modalityB", "readerTotal")
cor.tab3

p.o = 
p.e = (cor.tab3[1,1] * cont.tab[1,2])/N^2 + (cont.tab[2,1] * cont.tab[2,2])/N^2 


#### get agreements for readers ####
data.sub = subset(data, ReaderID != "truth")

R1.R2.agree = (data.sub[data.sub$ReaderID == "Reader1",]$Score == data.sub[data.sub$ReaderID == "Reader2",]$Score)
R1.R3.agree = (data.sub[data.sub$ReaderID == "Reader1",]$Score == data.sub[data.sub$ReaderID == "Reader3",]$Score)
R3.R2.agree = (data.sub[data.sub$ReaderID == "Reader3",]$Score == data.sub[data.sub$ReaderID == "Reader2",]$Score)

R1.R2 = sum(R1.R2.agree)
R1.R3 = sum(R1.R3.agree)
R3.R2 = sum(R3.R2.agree)

N = length(data.sub[data.sub$ReaderID == "Reader1",]$Score)

agree = diag(600, nrow = 3)
agree[2:3,1] = c(R1.R2, R1.R3)
agree[,2] = c(R1.R2, 600, R3.R2)
agree[1:2,3] = c(R3.R2, R1.R3)
colnames(agree) = c("R1", "R2", "R3")
row.names(agree) = c("R1", "R2", "R3")

agree

disagree = N - agree
disagree

###### Power Analysis #######
dfBDG = as.numeric(result$Ustat$dfBDG[3])
SE.tot = sqrt(as.numeric(result$Ustat$varAUCAminusAUCB[3]))

Power.Calc(dfBDG, SE.tot, .28, .05)


#### single modality data ###
dfBDG = as.numeric(result2$Ustat$dfBDG)
SE = sqrt(as.numeric(result2$Ustat$varAUCA))

Power.Calc(dfBDG, SE, .15, .05)

### graph ###

plot(F.val, type = "l", col = "blue", lwd = 3)
cut = qf(.95, 1, floor(dfBDG))
abline(v = cut, col = "red")

cum = numeric(70)

for (i in 1:70){
  cum[i] = sum(F.val[0:i])
}
plot(cum, type = "l", col = "green", lwd = 3)
abline(v = cut, col = "pink", lwd = 3)

###### Use Beta CDF to calc noncentral F ######
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