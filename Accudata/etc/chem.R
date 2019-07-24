require(mcr)
require(strucchange)
getwd()
setwd('/Users/stevenhurwitt/Documents/Accudata')
load("~/Documents/Accudata/chem.RData")
qed = read.table('QED.csv', header = T, sep = ",")

#fit CRL (x) vs SDIX (y)
x = qed$CRL
y = qed$SDIX

plot(x, y, xlab = "CRL fFN Concentration", ylab = "SDIX fFN Concentration", 
     main = "Plot of CRL vs. SDIX (ng/mL)", pch = 16, col = "blue", xlim = c(0,400), ylim = c(0,400))
#method.reg = Deming for Deming Reg
# = PaBaLarge for Passing Bablok

#jackknife ci
DR = mcreg(x, y, method.reg = "WDeming", method.ci = "jackknife")
DR@para

#change type to absolute for numbers
MCResult.calcBias(DR, x.levels = 50, type = "proportional", percent = T)
DRbias = DR@para[1,1] + (DR@para[2,1]-1)*50
pDR = DRbias/50
pDR

meDR = 1.96*pDR*(1 - pDR)/sqrt(148)
pDR - meDR
pDR + meDR

#bootstrap ci
PB = mcreg(x, y, method.reg = "PaBaLarge", method.ci = "bootstrap", nsamples = 1000, rng.seed = 978)
PB@para

PBbias = PB@para[1,1] + PB@para[2,1]*50 - 50
pPB = PBbias/50
pPB

ols = summary(lm(y~x))
beta = coef(ols)[,1]

MCResult.plot(DR, add.legend = F, points.pch = 16)
abline(beta[1], beta[2], col = "green", lwd = 2)
MCResult.plot(PB, add.legend = F, points.pch = 16)

sctest(y~x)


CI = function(Xjack, xhat, alpha){
  npoints<-length(Xjack)
  delta.X <- npoints*xhat-(npoints-1)*Xjack
  se <- sd(delta.X)/sqrt(npoints)
  CI <- c(xhat-qt(1-alpha/2,npoints-2)*se , xhat+qt(1-alpha/2,npoints-2)*se)
  return(list(est=xhat,se=se,CI=CI))
}


###jackknife
n = dim(qed)[[1]]
DRbias.boot = numeric(n)
PBbias.boot = numeric(n)

for (i in 1:n){
  x.boot = qed$CRL[-i]
  y.boot = qed$SDIX[-i]
  
  DR.boot = mcreg(x.boot, y.boot, method.reg = "Deming", method.ci = "jackknife")
  PB.boot = mcreg(x.boot, y.boot, method.reg = "PaBaLarge", method.ci = "bootstrap", nsamples = 1000, rng.seed = 978)
  
  
  DRbias.bs = DR.boot@para[1,1] + DR.boot@para[2,1]*50 - 50
  DRbias.boot[i] = DRbias.bs/50
  
  PBbias.bs = PB.boot@para[1,1] + PB.boot@para[2,1]*50 - 50
  PBbias.boot[i] = PBbias.bs/50
  print(paste("done with iter ", i))
}

mean(DRbias.boot) + 1.96*sd(DRbias.boot)/sqrt(148)
mean(DRbias.boot) - 1.96*sd(DRbias.boot)/sqrt(148)

mean(PBbias.boot) + 1.96*sd(PBbias.boot)/sqrt(148)
mean(PBbias.boot) - 1.96*sd(PBbias.boot)/sqrt(148)
