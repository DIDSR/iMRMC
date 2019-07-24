setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs/MayStudy')
load("~/Documents/Accudata/Baylabs/MayStudy/clinparam_may.RData")
save.image("~/Documents/Accudata/Baylabs/MayStudy/clinparam_may.RData")
require(plyr)
require(iMRMC)
dir = list.files(getwd())
dir
dir[23]

### turn data into agreement scores ###
clinparm = read.csv(dir[20], header = T, sep = ",")
echoparm = read.csv(dir[6], header = T, sep = ",")

clinparm = clinparm[clinparm$READSEQ == 1,]

echoparm = echoparm[!is.na(echoparm$CONTROL_SONOID),]
echoparm = echoparm[!is.na(echoparm$cardio1),]

params = levels(clinparm$clinParm)
params

cardio = echoparm[,5:9]
eval = levels(cardio$cardio1)
eval
zero = eval[6]
zero

scores = function(vector){return(as.numeric(as.character(vector) != zero))}

agree = apply(cardio, 2, scores)
agree

percent = apply(agree, 1, sum)/5
percent

hist(percent, breaks = 8, col = 'purple', freq = F)

Score = as.numeric(percent > .5)
hist(Score, breaks = 5, col = 'blue')

CP_col = rep(clinparm[,4], 4)


################

#make MRMC dataset
make_data = function(){

m = length(unique(echoparm$RECORD_ID))
n = length(Score)

Mod = rep("modalityA", n)
Truth = rep("truth", n)  
Reader = paste0("Reader", echoparm$CONTROL_SONOID)

truth_score = rep(1, n)
fake_truth_score = rep(0, n)
fake_reader_score = rep(.5, n)

Actual = paste0("Actual", echoparm$RECORD_ID)
Fake = paste0("Fake", echoparm$RECORD_ID)


actual = as.data.frame(cbind(Reader, Actual, Mod, Score), stringsAsFactors = F)
colnames(actual) = c("ReaderID", "CaseID", "ModalityID", "Score")
actual$Score = as.numeric(actual$Score)
colnames(actual) = c("ReaderID", "CaseID", "ModalityID", "Score")

fake = actual
truth = actual
fake_truth = actual

colnames(fake) = c("ReaderID", "CaseID", "ModalityID", "Score")
colnames(truth) = c("ReaderID", "CaseID", "ModalityID", "Score")
colnames(fake_truth) = c("ReaderID", "CaseID", "ModalityID", "Score")

fake$CaseID = Fake
fake$Score = as.numeric(fake_reader_score)
fake_truth$CaseID = Fake
fake_truth$Score = as.numeric(fake_truth_score)

truth$ModalityID = Truth
fake_truth$ModalityID = Truth

truth$ReaderID = Truth
fake_truth$ReaderID = Truth

truth$Score = as.numeric(truth_score)

fake$Score = as.numeric(fake$Score)
truth$Score = as.numeric(truth$Score)
fake_truth$Score = as.numeric(fake_truth$Score)

actual = unname(actual)
fake = unname(fake)
truth = unname(truth)
fake_truth = unname(fake_truth)

colnames(actual) = c("ReaderID", "CaseID", "ModalityID", "Score")
colnames(fake) = c("ReaderID", "CaseID", "ModalityID", "Score")
colnames(truth) = c("ReaderID", "CaseID", "ModalityID", "Score")
colnames(fake_truth) = c("ReaderID", "CaseID", "ModalityID", "Score")

final_data = rbind.fill(actual, fake, truth, fake_truth)

final_data$Score = as.numeric(final_data$Score)
final_data$Score[is.na(final_data$Score)] = .5
colnames(final_data) = c("ReaderID", "CaseID", "ModalityID", "Score")

return(final_data)
}

#loop thru analysis
final_data = make_data()
hist(final_data$Score)

##### MRMC ######
percents = function(parameter){
sub = final_data[CP_col == parameter,]
final = sub[('Actual' %in% sub$CaseID & 'Reader' %in% sub$ReaderID),]
return(mean(final$Score))
}

sub = final_data[CP_col == 'AV',]


final = sub[(('Actual' %in% sub$CaseID) & ('Reader' %in% sub$ReaderID)),]
percents('AV')

loop_analysis = function(parameter){
print(paste('working with parameter', parameter))
sub = final_data[CP_col == parameter,]

output.sub = doIMRMC(sub)

analysis_result = MRMC_analysis(output.sub, .8)
return(analysis_result)}

sub_params = params[c(4, 6, 7, 8)]

final_results = sapply(sub_params, loop_analysis)

to_vineet = as.data.frame(t(final_results))


#### output tables to vineet ###
row.names(to_vineet)
prim = row.names(to_vineet)[c(2, 5, 7, 9)]
second = row.names(to_vineet)[c(1, 3, 4, 6, 8, 10)]

table21 = to_vineet[row.names(to_vineet) %in% prim,]
table22 = to_vineet[row.names(to_vineet) %in% prim,c(1,2)]
table29 = to_vineet[row.names(to_vineet) %in% second,c(1,2)]

colnames(table21) = c('AUC', 'CI', 'P_VAL')
colnames(table22) = c('AUC', 'CI')
colnames(table29) = c('AUC', 'CI')

write.csv(table21, 'accept-nurse.csv', quote = F)
write.csv(table22, 'accept-side-prim-nurse.csv', quote = F)
write.csv(table29, 'accept-side-second-nurse.csv', quote = F)

###

table53_sono = to_vineet[row.names(to_vineet) %in% prim,]
table22_sono = to_vineet[row.names(to_vineet) %in% prim,c(1,2)]
table29_sono = to_vineet[row.names(to_vineet) %in% second,c(1,2)]

colnames(table53_sono) = c('AUC', 'CI', 'P_VAL')
colnames(table22_sono) = c('AUC', 'CI')
colnames(table29_sono) = c('AUC', 'CI')

write.csv(table53_sono, 'accept-sono.csv', quote = F)
write.csv(table22_sono, 'accept-side-prim-sono.csv', quote = F)
write.csv(table29_sono, 'accept-side-second-sono.csv', quote = F)

####### test w java ######

write.csv(LV, 'LV.csv', quote = F, row.names = F)
write.csv(TV, 'TV.csv', quote = F, row.names = F)
write.csv(MV, 'MV.csv', quote = F, row.names = F)
write.csv(GLVF, 'GLVF.csv', quote = F, row.names = F)


#### results

MRMC_analysis = function(output, p0){
auc = output$Ustat$AUCA
auc.var = sum(output$Ustat$varAUCA)
df.BDG.U = output$Ustat$dfBDG

if (df.BDG.U == "∞"){df.BDG.U = 9999}

print('auc value:')
print(auc)
print('auc variance:')
print(auc.var)
print('deg of freedom')
print(df.BDG.U)

t.cut = qt((1-.05/2), df.BDG.U)
test = round(sqrt((auc - p0)^2/auc.var), 4)
p.val = round((1 - pt(test, floor(df.BDG.U))), 4)

ci.ub = round(100*(auc + t.cut*sqrt(auc.var)), 1)
ci.lb = round(100*(auc - t.cut*sqrt(auc.var)), 1)

ci = paste("(", ci.lb, "*", ci.ub, ")", sep = "")
auc = round(100*auc, 1)

return(c(as.numeric(auc), ci, as.numeric(p.val)))}


####### comparison ######
setwd("/Users/stevenhurwitt/Documents/Accudata/Baylabs/")
dir = list.files(getwd())
dir[12]
doug_input = read.csv(dir[12], header = T)

head(doug_input)
