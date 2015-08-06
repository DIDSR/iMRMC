% function prob = iMRMC_BinaryValidate(anaMethod,Nr, Nc, r, PC, nexp)
% Validates an analysis method using Monte Carlo simulation by estimating 
% the coverage probability of the 95% confidence interval estimated by the analysis method.
%
% INPUTS:
% anaMethod: a character string specifying the .m file name (in the Matlab
% path) that implements an analysis method. See Section 2.3 of the iMRMC_Binary manual for further details.
% Nr, Nc, r, PC (same as inputs to iMRMC_BinarySimulate.m, see that function for details)
% nexp: number of Monte Carlo trials to calculate the empirical coverage probability of the estimated 95% confidence interval. Default value = 10,000.
% 
% OUPUT:
% prob: empirical coverage probability of the estimated 95% confidence interval.
%
% authors: Weijie Chen and Adam Wunderlich
% This function is part of iMRMC_Binary version 1.0 (beta)

%@@@@@@@@@@@@@@Disclaimer@@@@@@@@@@@@@@@@@@@
%This software and documentation (the "Software") were developed at the 
%Food and Drug Administration (FDA) by employees of the Federal Government 
%in the course of their official duties. Pursuant to Title 17, Section 105 
%of the United States Code, this work is not subject to copyright protection 
%and is in the public domain. Permission is hereby granted, free of charge,
%to any person obtaining a copy of the Software, to deal in the Software 
%without restriction, including without limitation the rights to use, copy,
%modify, merge, publish, distribute, sublicense, or sell copies of the 
%Software or derivatives, and to permit persons to whom the Software is 
%furnished to do so. FDA assumes no responsibility whatsoever for use by 
%other parties of the Software, its source code, documentation or compiled 
%executables, and makes no guarantees, expressed or implied, about its 
%quality, reliability, or any other characteristic. Further, use of this 
%code in no way implies endorsement by the FDA or confers any advantage in 
%regulatory decisions. Although this software can be redistributed and/or 
%modified freely, we ask that any derivative works bear some notice that 
%they are derived from it, and any modified versions bear some notice that 
%they have been modified.
%@@@@@@@@@@@@@@End of Disclaimer@@@@@@@@@@@@@@@@@@@ 

function prob = iMRMC_BinaryValidate(anaMethod,Nr, Nc, r, PC, nexp)
%
%convention: PC(2) is the new modality, PC(1) is the reference modality
if nargin == 5
    nexp = 10000;
end
if ~ischar(anaMethod)
    error ('The first input argument to iMRMC_BinaryValidate must be a character string.');
end
if exist(anaMethod) ~= 2
    error(['Analysis method ' anaMethod ' not found.']);
end

prob = 0;
v = vc_b2c_v3(r,PC,[1.0 1.0]);
trueDiff = PC(2) - PC(1);
for i = 1:nexp
    [S1,S2] = BinaryRoeMetz_v4(Nr,Nc,PC,v);
    cmd = ['ret = ' anaMethod '(S1,S2);'];
    eval(cmd);
    CI = ret.CI95;
    if CI(1) <= trueDiff && CI(2) >= trueDiff
        prob = prob + 1;
    end
end
prob = prob/nexp;