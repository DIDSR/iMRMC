library(iMRMC)

# Create an MRMC data frame
config.gRoeMetz <- sim.gRoeMetz.config()

# Simulate data
df.MRMC <- sim.gRoeMetz(config.gRoeMetz)

result <- tryCatch(
  doIMRMC(df.MRMC),
  warning = function(w) {},
  error = function(w) {}
) 
