% function [S1,S2] = iMRMC_BinarySimulate(r,PC,Nr,Nc)
% Generate binary MRMC data with specified parameters and sample size.
%
% INPUTS: 
% r: a vector of length 7 representing 7 correlation coefficient parameters that characterize the correlations in the binary data.
% r(1): Correlation between two cases from modality 1 (conventional modality) read by the same reader
% r(2): Correlation between two cases from modality 2 (new modality) read by the same reader
% r(3): Correlation between two readers reading the same case from modality 1
% r(4): Correlation between two readers reading the same case from modality 2
% r(5): Correlation between two modalities with the same reader reading the same case
% r(6): Correlation between two cases from different modalities read by the same reader
% r(7): Correlation between two readers reading the same case from the different modalities
% PC: a vector of length 2 representing the expected percentage correct (or agreement) for the two modalities
% PC(1): the expected percentage correct (or agreement) for modality 1
% PC(2): the expected percentage correct (or agreement) for modality 2
% Nr: the number of readers
% Nc: the number of cases
%
% Important note: The correlation parameters must be between 0 and 1 and must satisfy the constraints
% r(1) >= r(6), r(2) >= r(6), r(3) >= r(7), r(4) >= r(7), r(5) >=  r(6)+r(7) 
%
% OUTPUTS:
% S1: binary assessment or success data for modality 1, Nc x Nr matrix 
% S2: binary assessment or success data for modality 2, Nc x Nr matrix
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

function [S1,S2] = iMRMC_BinarySimulate(r,PC,Nr,Nc)

% check constraints

if (r(6)<0)
    error('You must set the parameters such that r(6)>=0');
end
if (r(7)<0)
    error('You must set the parameters such that r(7)>=0');
end
if (r(1)<r(6))
    error('You must set the parameters such that r(1)>=r(6)');
end
if (r(2)<r(6))
    error('You must set the parameters such that r(2)>=r(6)');
end
if (r(3)<r(7))
    error('You must set the parameters such that r(3)>=r(7)');
end
if (r(4)<r(7))
    error('You must set the parameters such that r(4)>=r(7)');
end
if (r(5)<(r(6)+r(7)))
    error('You must set the parameters such that r(5)>=(r(6)+r(7))');
end

v = vc_b2c_v3(r,PC,[1.0 1.0]);
[S1,S2] = BinaryRoeMetz_v4(Nr,Nc,PC,v);

