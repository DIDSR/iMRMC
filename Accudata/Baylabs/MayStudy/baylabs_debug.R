setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs/MayStudy')
load("~/Documents/Accudata/Baylabs/MayStudy/clinparam_may.RData")
require(plyr)
require(iMRMC)

#### read data STUDY

dir = list.files(getwd())
dir
data_file = dir[26]

clinparm = read.csv(data_file, header = T, sep = ",")
clinparm = clinparm[clinparm$READSEQ == 1,]

bad_study = c(1030, 1055, 1082, 2002, 2023, 2044, 2046)
good_study = !(clinparm$RECORD_ID %in% bad_study)
clinparm = clinparm[good_study,]

### read data CONTROL
echo = dir[13]
echo

echoparm = read.csv(echo, header = T, sep = ",") #2710 obs
bad_echo = c(1030, 1055, 1082, 2002, 2023, 2044, 2046, 1063, 1065, 1066, 2087, 2057, 2063)
good_echo = !(echoparm$RECORD_ID %in% bad_echo)
echoparm = echoparm[good_echo,] #2580 obs
echoparm = echoparm[echoparm$READSEQ == 1,] #2340 obs

#### prep scores

## nurses STUDY
nurse = as.numeric(clinparm$STUDY_NURSEID)
record = as.numeric(clinparm$RECORD_ID)
n = length(nurse)
new_nurse = numeric(n)

for (i in 1:n){
  if (record[i] >= 2000){
    new_nurse[i] = nurse[i] + 4
  }
  else{
    new_nurse[i] = nurse[i]
  }
}

hist(nurse)
hist(new_nurse)

params = levels(clinparm$clinParm)
params

#STUDY convert scores
cardio = clinparm[,5:9]
eval = levels(cardio$cardio1)
eval
zero = eval[8]
zero

scores = function(vector){return(as.numeric(as.character(vector) != zero))}

agree = apply(cardio, 2, scores)
agree

percent = apply(agree, 1, sum)/5
percent

hist(percent, breaks = 8, col = 'purple', freq = F)

Score = as.numeric(percent > .5)
hist(Score, breaks = 5, col = 'blue')

#CONTROL convert scores
params_echo = as.character(unique(echoparm$clinParm))
params_echo

cardio_echo = echoparm[,5:9]
eval_echo = levels(cardio_echo$cardio1)
eval_echo
zero_echo = eval_echo[8]
zero_echo

scores_echo = function(vector){return(as.numeric(as.character(vector) != zero_echo))}

agree_echo = apply(cardio_echo, 2, scores_echo)
agree_echo

percent_echo = apply(agree_echo, 1, sum)/5
percent_echo

hist(percent_echo, breaks = 8, col = 'red', freq = F)

Score_echo = as.numeric(percent_echo > .5)
hist(Score_echo, breaks = 5, col = 'orange')

#### make MRMC (CONTROL ECHO)
echo_param = echoparm$clinParm

echo_MRMC = make_echo_data(echo_param)
echo_MRMC_data = echo_MRMC[[1]]
echo_CP_col = as.character(echo_MRMC[[2]])

n = length(echo_param)

make_echo_data = function(parameters){
  n = length(parameters)
  CP_col = rep(parameters, 4)
  Mod = rep("modalityA", n)
  Truth = rep("truth", n)
  Reader = paste0('Reader', echoparm$CONTROL_SONOID)
  truth_score = rep(1, n)
  fake_truth_score = rep(0, n)
  fake_reader_score = rep(.5, n)
  
  Actual = paste0("Actual", echoparm$RECORD_ID)
  Fake = paste0("Fake", echoparm$RECORD_ID)
  
  actual = as.data.frame(cbind(Reader, Actual, Mod, Score_echo), stringsAsFactors = F)
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
  
  final_echo_data = rbind.fill(actual, fake, truth, fake_truth)
  return(list(final_echo_data, CP_col))}

#### make MRMC dataset (STUDY ECHO)

cp_param = clinparm[,4]

make_data = function(parameters){
n = length(parameters)
CP_col = rep(parameters, 4)
Mod = rep("modalityA", n)
Truth = rep("truth", n)
Reader = paste0('Reader', new_nurse)
truth_score = rep(1, n)
fake_truth_score = rep(0, n)
fake_reader_score = rep(.5, n)

Actual = paste0("Actual", clinparm$RECORD_ID)
Fake = paste0("Fake", clinparm$RECORD_ID)

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
return(final_data)}

#### loop thru percents

hist(final_data$Score)

loop_percent = function(parameter){
  print(paste('working with parameter', parameter))
  sub = actual[echo_param == parameter,]
  mu = mean(sub$Score)
  tot = sum(sub$Score)
  m = length(sub$Score)
  return(c(mu, tot, m))
}

debug_loop = function(parameter){
  print(paste('working with parameter', parameter))
  sub = final_data[cp_param == parameter,]
  file = paste(parameter, '.csv', sep = '')
  colnames(sub) = c('BEGIN DATA:', '', '', '')
  print('writing file...')
  write.csv(sub, file, quote = F, row.names = F)
  print('running MRMC')
  #mrmc = doIMRMC(sub)
  #return(mrmc)
}

MRMC_results = sapply(params, debug_loop)

percent_test = as.data.frame(t(sapply(params, loop_percent)))
colnames(percent_test) = c('percent', 'success', 'obs')
row.names(percent_test) = params
percent_test

write.csv(percent_test, 'percent-test-echo.csv', row.names = T, quote = F)

#### loop thru analysis

final_data = make_data()

MRMC_analysis = function(output, p0){
  auc = output$Ustat$AUCA
  auc.var = sum(output$Ustat$varAUCA)
  df.BDG.U = output$Ustat$dfBDG
  
  #if (df.BDG.U == "∞"){df.BDG.U = 9999}
  
  print('auc value:')
  print(auc)
  print('auc variance:')
  print(auc.var)
  print('deg of freedom')
  print(df.BDG.U)
  if (auc.var < 0){auc.var = .00001}
  
  t.cut = qt((1-.05), df.BDG.U)
  test = round(sqrt((auc - p0)^2/auc.var), 4)
  p.val = round((1 - pt(test, floor(df.BDG.U))), 4)
  
  ci.ub = round(100*(auc + t.cut*sqrt(auc.var)), 1)
  ci.lb = round(100*(auc - t.cut*sqrt(auc.var)), 1)
  
  ci = paste("(", ci.lb, "*", ci.ub, ")", sep = "")
  auc = round(100*auc, 1)
  
  return(c(as.numeric(auc), ci, as.numeric(p.val)))}


loop_analysis = function(parameter){
  print(paste('working with parameter', parameter))
  sub = echo_MRMC_data[echo_CP_col == parameter,]
  
  output.sub = doIMRMC(sub)
  
  analysis_result = MRMC_analysis(output.sub, .8)
  return(analysis_result)}

final_results = sapply(params, loop_analysis)

params_echo_good = params_echo[!(params_echo %in% c('LV', 'GLVF', 'PE', 'MV'))]
final_results_echo = sapply(params_echo_good, loop_analysis)

sample = as.data.frame(t(final_results_echo))
colnames(sample) = c('AUC', 'CI', 'p_val')
sample
write.csv(sample, 'CONTROL_ECHO_MRMC_RESULTS.csv', quote = F)

#### export results

to_vineet = as.data.frame(t(final_results))
colnames(to_vineet) = c('AUC', 'CI', 'p_val')
to_vineet$AUC = as.numeric(as.character(to_vineet$AUC))
to_vineet$p_val = as.numeric(as.character(to_vineet$p_val))

to_vineet

write.csv(to_vineet, 'STUDY_ECHO_MRMC_RESULTS.csv', quote = F)

#### outputs to vineet

row.names(to_vineet) = params
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
