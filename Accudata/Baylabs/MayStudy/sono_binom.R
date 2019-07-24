setwd('/Users/stevenhurwitt/Documents/Accudata/Baylabs/MayStudy')
#load("~/Documents/Accudata/Baylabs/MayStudy/clinparam_may.RData")
loads("~/Documents/Accudata/Baylabs/MayStudy/sono_binom.RData")

require('binom')

dir = dir(getwd())
dir

control_study = read.csv(dir[32], row.names = 1)

x_study = control_study$success
n_study = control_study$obs #234

control_view = read.csv(dir[20], row.names = 1)

x_view = control_view$success
n_view = control_view$obs

output = binom.confint(x_study[1], n_study[1], conf.level = .95, methods = 'exact')

params = row.names(control_study)
view_params = row.names(control_view)

study_ci = binom.confint(x_study, n_study, conf.level = .95, methods = 'exact')
view_ci = binom.confint(x_view, n_view, conf.level = .95, methods = 'exact')

write.csv(study_ci, 'study_ci.csv', row.names = FALSE, quote = FALSE)
write.csv(view_ci, 'view_ci.csv', row.names = FALSE, quote = FALSE)
