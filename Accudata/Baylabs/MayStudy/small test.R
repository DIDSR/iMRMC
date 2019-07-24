params
parameter = params[7]
sub = final_data[CP_col == parameter,]
write.csv(sub, 'PE.csv', quote = F, row.names = F)

