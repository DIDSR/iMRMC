setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs')
load("~/Documents/Accudata/Baylabs/baylabs_update.RData")
save.image("~/Documents/Accudata/Baylabs/baylabs_update.RData")
dir = list.files(getwd())
##############
dir

######## Doug Data #######

##try tripling number of cases w/ readers the same

#########################
doug = read.table(dir[16], sep = ",", header = T)
doug2 = read.table(dir[17], sep = ",", header = T)
levels(doug$ClinParm)
head(doug)
colnames(doug)

unique(doug$ReaderID)
unique(doug$CaseID)
unique(doug$ModalityID)

dougScore = doug$Score
doug$ModalityID = as.character(doug$ModalityID)
hist(dougScore, col = 'red')

doug_data = make_data(dougScore,.8)
doug_output = MRMC_analysis(doug, .8, .05)
doug_output

########### read in raw data ###############
print(dir)
rawdata = read.table(dir[15], sep = ",", header = T)
data = read.table(dir[8], sep = ',', header = T)
data = data[,-1]

n_uniq = function(var){return(length(unique(rawdata$var)))}
dim(rawdata)
head(rawdata)

vars = rawdata[1,]
vars = colnames(vars)[-1]
vars = c(vars)
typeof(vars)
p = length(vars)
vars = as.character(vars)

for (i in 1:p){n_uniq(vars[i])}


######### raw data prep #################
## turns cardiologist (cardio) quality ratings to binary
## 1 - sufficient quality, 0 not
clinical = read.csv('STUDY_ECHO_CLINPARM.csv', header = T)
clin_param = levels(clinical$clinParm)

get_score = function(data, param){

param_sub = data[data$clinParm == param,]

#set equal to one if Inadequate Quality level, zero o.w.
factor_lvl = levels(param_sub$cardio1)
cardio = param_sub[,c(4:8)]
cardio
zero = as.character(factor_lvl[5])

#apply binary function above
scores = function(vector){return(as.integer(as.character(vector) != zero))}

agree = apply(cardio, 2, scores)
agree

#find percent out of 5 cardiologists
percent = apply(agree, 1, sum)/5
percent
#dist of percents
hist(percent, breaks = 8, col = 'purple', freq = F)

#set score to be over fifty percent of agreements
#visualize
Score = as.integer(percent > .5)
hist(Score, breaks = 5, col = 'blue')
return(Score)}


########## make MRMC dataset ##########
##makes dataset for MRMC analysis
##score is number of agreements (# of agreements over 50 %)
#modality (truth, A, or B)
#reader id (truth, reader 1 - m)
#case id (truth, 1 - k)
#score (0 or 1) 

make_data = function(score, p){

Mod = rep("modalityA", length(score))
Truth = rep("truth", length(score))
Case = LV$RECORD_ID

n = length(score)
a = ceiling(length(score)*p)
b = n - a

truth_score = c(rep(0, a), rep(1, b))
Reader = paste0("Reader", LV$STUDY_NURSEID)

LV_1 = cbind(Reader, Case, Mod, as.integer(as.character(Score)))
LV_2 = cbind(Truth, Case, Truth, as.integer(truth_score))

LV_final = data.frame(rbind(LV_1, LV_2))
colnames(LV_final)[4] = "Score"
LV_final$Score = as.character(LV_final$Score)
LV_final$Score = as.integer(LV_final$Score)

return(LV_final)
}

###################
dougScore = rawdata$Score

data = make_data(Score, .5)
data2 = make_data(Score, .8)

mean(data$Score)
mean(data2$Score)

doug = make_data(Score, .97222222222)
dim(doug)

out = MRMC_analysis(data, .8, .05)
out2 = MRMC_analysis(data2, .8, .05)
out3 = MRMC_analysis(doug, .5, .05)
out3

output = rbind(out, out2, out3)
output

###################
triple = rbind(doug, doug, doug)
MRMC_analysis(triple, .5, .05)

#################################
MRMC_analysis = function(data, p0, alpha){
require(iMRMC)
output = doIMRMC(data)

auc = output$Ustat$AUCA
auc.var = sum(output$Ustat$varAUCA)
df.BDG.U = output$Ustat$dfBDG

print('auc value:')
print(auc)
print('auc variance:')
print(auc.var)
print('deg of freedom')
print(df.BDG.U)

t.cut = qt((1-alpha/2), df.BDG.U)
test = round(sqrt((auc - p0)^2/auc.var), 4)
p.val = round(2*(1 - pt(test, floor(df.BDG.U))), 4)

ci.ub = round(100*(auc + t.cut*sqrt(auc.var)), 4)
ci.lb = round(100*(auc - t.cut*sqrt(auc.var)), 4)

ci = paste("(", ci.lb, "*", ci.ub, ")")

return(as.character(c(test, p.val, ci)))}
