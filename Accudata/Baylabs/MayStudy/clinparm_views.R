setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs/MayStudy')
load("~/Documents/Accudata/Baylabs/MayStudy/clinparam_may.RData")
require(plyr)
require(iMRMC)
dir = list.files(getwd())

### Read data, subset
control_views = dir[18]
study_views = dir[41]

control_views
study_views

CV_data = read.csv(control_views) #2710 obs
SV_data = read.csv(study_views) #2710

CV_data = CV_data[CV_data$READSEQ == 1,] #2470
SV_data = SV_data[SV_data$READSEQ == 1,] #2470

bad_nurse = c(1030, 1055, 1082, 2002, 2023, 2044, 2046)
bad_sono = c(1030, 1055, 1082, 2002, 2023, 2044, 2046, 1063, 1065, 1066, 2087, 2057, 2063)

good_nurse = !(SV_data$RECORD_ID %in% bad_nurse)
good_sono = !(CV_data$RECORD_ID %in% bad_sono)

CV_data = CV_data[good_sono,] #2340
SV_data = SV_data[good_nurse,] #2400

#sanity check nurse & sono
hist(CV_data$CONTROL_SONOID)
hist(echoparm$CONTROL_SONOID)

hist(SV_data$STUDY_NURSEID)
hist(new_nurse)

#### make views data

view_params = levels(SV_data$VIEW)

control_cardio_view = CV_data[,7:11]
study_cardio_view = SV_data[,7:11]

study_diag_qual = apply(study_cardio_view, 1, median)
control_diag_qual = apply(control_cardio_view, 1, median)

study_score = as.numeric(study_diag_qual >= 3)
control_score = as.numeric(control_diag_qual >= 3)

hist(study_score)
hist(control_score)

study_views = rep(SV_data$VIEW, 4)
control_views = rep(CV_data$VIEW, 4)

make_view_data = function(data, score, control){
  
  m = length(unique(data$RECORD_ID)) 
  n = length(score)
  
  if (control == T){reader = data$CONTROL_SONOID}
  else if (control == F){reader = data$STUDY_NURSEID}
  
  Mod = rep("modalityA", n)
  Truth = rep("truth", n)  
  Reader = paste0("Reader", reader)
  
  truth_score = rep(1, n)
  fake_truth_score = rep(0, n)
  fake_reader_score = rep(.5, n)
  
  Actual = paste0("Actual", data$RECORD_ID)
  Fake = paste0("Fake", data$RECORD_ID)
  
  actual = as.data.frame(cbind(Reader, Actual, Mod, score), stringsAsFactors = F)
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
  
  return(list(final_data, actual))
}

### run function to make datasets
SV_output = make_view_data(SV_data, study_score, F)
CV_output = make_view_data(CV_data, control_score, T)

study = SV_output[[1]]
control = CV_output[[1]]

actual_study = SV_output[[2]]
actual_control = CV_output[[2]]

#### MRMC analysis / percent check

percents_CV = function(parameter){
  sub = actual_control[CV_data$VIEW == parameter,]
  mu = mean(sub$Score)
  tot = length(sub$Score)
  m = sum(sub$Score)
  return(c(mu, tot, m))
}

percents_SV = function(parameter){
  sub = actual_study[SV_data$VIEW == parameter,]
  mu = mean(sub$Score)
  tot = length(sub$Score)
  m = sum(sub$Score)
  return(c(mu, tot, m))
}

#run
CV_percent_check = as.data.frame(t(sapply(view_params, percents_CV)))
colnames(CV_percent_check) = c('percent', 'success', 'obs')
row.names(CV_percent_check) = view_params
CV_percent_check

SV_percent_check = as.data.frame(t(sapply(view_params, percents_SV)))
colnames(SV_percent_check) = c('percent', 'success', 'obs')
row.names(SV_percent_check) = view_params
SV_percent_check

#output
write.csv(CV_percent_check, 'CONTROL_VIEW_PERCENT.csv', row.names = T, quote = F)
write.csv(SV_percent_check, 'STUDY_VIEW_PERCENT.csv', row.names = T, quote = F)

##### MRMC #####

## loop analysis

loop_view_study_analysis = function(parameter){
  print(paste('using view:', parameter))
  sub = study[study_views == parameter,]
  
  output.sub = doIMRMC(sub)
  
  analysis_result = MRMC_analysis(output.sub, .8)
  return(analysis_result)}

loop_view_control_analysis = function(parameter){
  print(paste('using view:', parameter))
  sub = control[control_views == parameter,]
  
  output.sub = doIMRMC(sub)
  
  analysis_result = MRMC_analysis(output.sub, .8)
  return(analysis_result)}

#### results #####

study_views_result = as.data.frame(t(sapply(view_params, loop_view_study_analysis)))
control_views_result = as.data.frame(t(sapply(view_params, loop_view_control_analysis)))

colnames(study_views_result) = c('AUC', 'CI', 'p_val')
colnames(control_views_result) = c('AUC', 'CI', 'p_val')

study_views_result
control_views_result

write.csv(study_views_result, 'STUDY_VIEW_MRMC.csv')
write.csv(control_views_result, 'CONTROL_VIEW_MRMC.csv')

### analysis function
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


vineet_views = as.data.frame(t(views_result))

test = study[views == 'AP5',]