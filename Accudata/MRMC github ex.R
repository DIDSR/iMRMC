require(iMRMC)
require(Rmpfr)
setwd('/Users/stevenhurwitt/Documents/R programming/MRMC')

data = read.csv("fake pilot.csv", header = T, sep = ",")
result = doIMRMC(data)

var.est = as.matrix(result$varDecomp$BDG$Ustat$comp$modalityA.modalityB) %*% t(as.matrix(result$varDecomp$BDG$Ustat$coeff$modalityA.modalityB))
SE.tot = sqrt(var.est[1,1] + var.est[2,1] - 2*var.est[3,1]) #var A - B

sqrt(var.est[1,1]) #var A
sqrt(var.est[2,1]) #var B

#SE.tot = sqrt(as.numeric(result$Ustat$varAUCAminusAUCB[3]))
dfBDG = as.numeric(result$Ustat$dfBDG[3])
eff.size = .28
t.stat = eff.size/SE.tot
lambda = t.stat^2
cutoff = qf(.95, 1, floor(dfBDG))

d1 = 1
d2 = dfBDG - 1

ints = seq(0:214) - 1

s1 = as.bigz((lambda/2)^(ints))
s2 = factorialZ(ints)
s3 = exp(-lambda/2)

q = cutoff*d1/(cutoff*d1+d2)
betas = pbeta(q, d1/2 + ints, d2/2)

scale = s1 %/% s2
F.val = s3*as.numeric(scale)*betas
power = 1 - sum(F.val)
