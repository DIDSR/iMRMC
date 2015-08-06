% BinaryRoeMetz_v4.m
% This function creates binary MRMC data based on the continuous-valued Roe Metz model.  
% For binary data, the "t" indices in the Roe-Metz model drop out. 
% inputs: N_r, N_c, p=(PC1,PC2), v =(v_r,v_c,v_tr1,v_tr2,v_tc1,v_tc2,v_rc,v_e1 v_e2)
% output: S1, S2 = success matrices for modality 1 and 2
%
% authors: Weijie Chen and Adam Wunderlich
% This function is part of iBinaryMRMC version 1.0 (beta)

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

function [S1,S2,S1c,S2c] = BinaryRoeMetz_v4(N_r,N_c,p,v)
PC1 = p(1);
PC2 = p(2);
v_r = v(1);
v_c = v(2);
v_tr1 = v(3);
v_tr2 = v(4);
v_tc1 = v(5);
v_tc2 = v(6);
v_rc = v(7);
v_e1 = v(8);
v_e2 = v(9);
sig_c = sqrt(v_c);
sig_tc1 = sqrt(v_tc1);
sig_tc2 = sqrt(v_tc2);
sig_rc = sqrt(v_rc);
sig_e1 = sqrt(v_e1);
sig_e2 = sqrt(v_e2);
sig_r = sqrt(v_r);
sig_tr1 = sqrt(v_tr1);
sig_tr2 = sqrt(v_tr2);

v1 = v_c+v_tc1+v_rc+v_e1+v_r+v_tr1;  % overall variance 1
v2 = v_c+v_tc2+v_rc+v_e2+v_r+v_tr2;  % overall variance 2

% Determine cutoffs required to get desired overall mean percent correct
% for each modality.  The modality fixed effect, tau, is determined by 
% the cutoffs.
c1 = sqrt(v1)*norminv(1-PC1);
c2 = sqrt(v2)*norminv(1-PC2);

% generate random matrices of continuous values according to RM model
% S1 = zeros(N_c,N_r); %this is not needed so commented out
% S2 = zeros(N_c,N_r);

R = randn(1,N_r)*sig_r;
C = randn(N_c,1)*sig_c;
tauR = randn(1,N_r)*sig_tr1;
tauC = randn(N_c,1)*sig_tc1;
RC = randn(N_c,N_r)*sig_rc;
epsilon = randn(N_c,N_r)*sig_e1;
S1c = repmat(R+tauR,N_c,1) + repmat(C+tauC,1,N_r) + RC + epsilon;
% get independent set of deviates for modality-dependent components
tauR = randn(1,N_r)*sig_tr2;
tauC = randn(N_c,1)*sig_tc2;
epsilon = randn(N_c,N_r)*sig_e2;
S2c = repmat(R+tauR,N_c,1) + repmat(C+tauC,1,N_r) + RC + epsilon;

S1 = (S1c > c1);
S2 = (S2c > c2);
S1 = double(S1); % convert from logical to double
S2 = double(S2);