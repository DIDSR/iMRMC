% function ret = iMRMC_BinaryAnalyze_OR(S1,S2) 
% Analyze binary MRMC data using the Obuchowski-Rockette (1995) method 
% together with the Hillis (2007) degrees of freedom.
%
% INPUTS: S1, S2 = Nc x Nr sucess matrices for each modality
% 
% OUTPUT: ret = an output structure that has a field 'CI95' containing the
% 95% confidence interval for the difference in the percent agreement
% between the two modalities
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


function ret = iMRMC_BinaryAnalyze_OR(S1,S2) 

sPC = [mean(S1)',mean(S2)']; % N_r x 2 matrix
% each column is the set of success scores for a given reader and modality
X = [S1 S2];
[theta,C] = propCov(X);

ret = mrmc_OR_analysis(sPC,C);

function ret=mrmc_OR_analysis(AUC,covAUC)
%Updated Obuchowski-Rockette method for MRMC ROC analysis
%AUC: rxt AUC matrix, r - # readers, t - # modalities
%covAUC: t*rxt*r, covariance matrix of AUC

[r,t]=size(AUC);
mauc_t=mean(AUC); mauc_t=mauc_t(:);
mst=r*sum((mauc_t-mean(mauc_t)).^2)/(t-1);
mauc_r=mean(AUC,2);
msr=t*sum((mauc_r-mean(mauc_r)).^2)/(r-1);
mstr=sum(sum((AUC-repmat(mauc_t',r,1)-repmat(mauc_r,1,t)+mean(mauc_r)).^2),2)/((r-1)*(t-1));

varepsi=mean(diag(covAUC));

%cov1: same reader, different tests
cov1=0;
for i=1:(t-1)
    for j=1:(t-i)*r
        cov1=cov1+covAUC(j+i*r,j);
    end
end

%
%cov2: different readers, same test
cov2=0;
for i=1:t
    for j=2:r
        for k=1:(j-1)
            cov2=cov2+covAUC(j+(i-1)*r,k+(i-1)*r);
        end
    end
end

%
%cov3: different readers, different tests
cov3=((sum(covAUC(:))-sum(diag(covAUC)))/2-cov1-cov2)/(t*(t-1)/2*r*(r-1));

cov1=cov1/(t*(t-1)/2*r);
cov2=cov2/(t*r*(r-1)/2);
r1=cov1/varepsi;
r2=cov2/varepsi;
r3=cov3/varepsi;
tmp=r*(cov2-cov3);
if tmp<0
    tmp=0;
end
F_stat=mst/(mstr+tmp);
ddf_H=(mstr+tmp)^2/(mstr^2/((t-1)*(r-1)));
pval=1-cdf('f',F_stat,t-1,ddf_H);
delta1=icdf('t',0.025,ddf_H)*sqrt(2*(mstr+tmp)/r);
delta2=icdf('t',1-0.025,ddf_H)*sqrt(2*(mstr+tmp)/r);
CI95=[mauc_t(2)-mauc_t(1)+delta1 mauc_t(2)-mauc_t(1)+delta2];
ret.mst=mst;
ret.msr=msr;
ret.mstr=mstr;
ret.varepsi=varepsi;
ret.cov1=cov1;
ret.cov2=cov2;
ret.cov3=cov3;
ret.r1=r1;
ret.r2=r2;
ret.r3=r3;
ret.F_stat=F_stat;
ret.ddf_H=ddf_H;
ret.pval=pval;
ret.CI95=CI95;
ret.TotalVarOfDiff=2*(mstr+tmp)/r;
ret.DiffPfm=mauc_t(2)-mauc_t(1);

% propCov.m
% Estimate covariance matrix for a vector of binomial proportions using unbiased estimator 
%
% Input: X = matrix of success scoresm where each column is a set of success scores for a given reader and modality
% Outputs: theta = vector of percent correct estimates, C = estimated covariance matrix

function [theta,C] = propCov(X)

[n,K] = size(X);
theta = mean(X)';
P = X'*X./n;
C = (P- theta*theta')./(n-1);
