library(iMRMC)

dirName <- file.path("inst", "data-raw", "RMconfigurations")
fileNames <- list.files(dirName)

roeMetzConfigs <- data.frame()
for (i in 1:length(fileNames)) {
  
  fileName.i <- fileNames[[i]]
  
  df <- read.csv(file.path(dirName, fileName.i), header = FALSE)
  temp <- matrix(unlist(strsplit(as.character(df$V1), ":")),
                 nrow = nrow(df), ncol = 2, byrow = TRUE)
  parameters <- as.numeric(temp[ ,2])
  names(parameters) <- temp[ ,1]
  
  config <- data.frame(sim.gRoeMetz.config())
  
  config$modalityID.A <- "testA"
  config$modalityID.B <- "testB"
  
  config$nR <- parameters["nr"]
  config$nC.neg <- parameters["n0"]
  config$nC.pos <- parameters["n1"]
  
  config$mu.neg <- 0
  config$var_r.neg <- parameters["R0"]
  config$var_c.neg <- parameters["C0"]
  config$var_rc.neg <- parameters["RC0"]
  
  config$mu.pos <- 0
  config$var_r.pos <- parameters["R1"]
  config$var_c.pos <- parameters["C1"]
  config$var_rc.pos <- parameters["RC1"]
  
  config$mu.Aneg <- 0
  config$var_r.Aneg <- parameters["AR0"]
  config$var_c.Aneg <- parameters["AC0"]
  config$var_rc.Aneg <- parameters["ARC0"]
  
  config$mu.Apos <- parameters["uA"]
  config$var_r.Apos <- parameters["AR1"]
  config$var_c.Apos <- parameters["AC1"]
  config$var_rc.Apos <- parameters["ARC1"]
  
  config$mu.Bneg <- 0
  config$var_r.Bneg <- parameters["BR0"]
  config$var_c.Bneg <- parameters["BC0"]
  config$var_rc.Bneg <- parameters["BRC0"]
  
  config$mu.Bpos <- parameters["uB"]
  config$var_r.Bpos <- parameters["BR1"]
  config$var_c.Bpos <- parameters["BC1"]
  config$var_rc.Bpos <- parameters["BRC1"]
  
  roeMetzConfigs <- rbind(roeMetzConfigs, config)
  
}

temp <- matrix(unlist(strsplit(fileNames, ".", fixed = TRUE)),
               nrow = length(fileNames), ncol = 2, byrow = TRUE)
rownames(roeMetzConfigs) <- temp[ , 1]

usethis::use_data(roeMetzConfigs, overwrite = TRUE)
