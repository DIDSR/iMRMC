% function pow = iMRMC_BinaryPower(anaMethod,Nr, Nc, r, PC, nim, nexp)
% Calculation of empirical power of an analysis method (anaMethod) in a non-inferiority study 
% using Monte Carlo simulations given a set of parameters and sample sizes.
%
% INPUTS:
% anaMethod, Nr, Nc, r, PC, nexp (same as for iMRMC_BinaryValidate.m, see that function for descriptions) 
% nim: non-inferiority margin
%
% OUTPUT:
% pow: empirical power in nexp Monte Carlo trials
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

function pow = iMRMC_BinaryPower(anaMethod,Nr, Nc, r, PC, nim, nexp)
%
%convention: PC(2) is the new modality, PC(1) is the reference modality
if nargin == 6
    nexp = 10000;
end
if ~ischar(anaMethod)
    error ('The first input argument to iMRMC_BinaryPower must be a character string.');
end
if exist(anaMethod) ~= 2
    error(['Analysis method ' anaMethod ' not found.']);
end

pow = 0;
v = vc_b2c_v3(r,PC,[1.0 1.0]);

for i = 1:nexp
    [S1,S2] = BinaryRoeMetz_v4(Nr,Nc,PC,v);
    cmd = ['ret = ' anaMethod '(S1,S2);'];
    eval(cmd);
    CI = ret.CI95;
    if CI(1) > -nim
        pow = pow + 1;
    end
end
pow = pow/nexp;