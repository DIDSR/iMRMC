% function [v] = vc_b2c_v3(r,p,v_tot)
% --------------- General Version ---------------------
% allow for distinct values of r_c,r_r,v_tr, and v_tc for each modality
% inputs: r = (r_c1,r_c2,r_r1,r_r2,r_t,r_tc,r_tr)  
%         p = (pc1,pc2) overall percent correct for each modality
%         v_tot = (v_tot1,v_tot2) sum of all variance components including v_e 
% outputs: v = (v_r,v_c,v_tr1,v_tr2,v_tc1,v_tc2,v_rc,v_e1 v_e2) - vector of variance components  
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


function [v] = vc_b2c_v3(r,p,v_tot)

% check constraints
r_c1 = r(1);
r_c2 = r(2);
r_r1 = r(3);
r_r2 = r(4);
r_t = r(5);
r_tc = r(6);
r_tr = r(7);

if length(v_tot) ~= 2
    error('The argument v_tot has to be a vector of length 2.');
end
if (r_tc<0)
    error('You must set the parameters such that r_tc>=0');
end
if (r_tr<0)
    error('You must set the parameters such that r_tr>=0');
end
if (r_c1<r_tc)
    error('You must set the parameters such that r_c1>=r_tc');
end
if (r_c2<r_tc)
    error('You must set the parameters such that r_c2>=r_tc');
end
if (r_r1<r_tr)
    error('You must set the parameters such that r_r1>=r_tr');
end
if (r_r2<r_tr)
    error('You must set the parameters such that r_r2>=r_tr');
end
if (r_t<(r_tc+r_tr))
    error('You must set the parameters such that r_t>=(r_tc+r_tr)');
end

%optimize v_r
myfun = @(x,p) abs(cont2bincorr45(x,p,v_tot)-r(6));
fun = @(x) myfun(x,p);
v_r = fminbnd(fun,1e-10,max(v_tot)-1e-10);

%optimize v_c
myfun = @(x,p) abs(cont2bincorr45(x,p,v_tot)-r(7));
fun = @(x) myfun(x,p);
v_c = fminbnd(fun,1e-10,max(v_tot)-v_r);

%optimize v_tr
myfun = @(x,v_r,p) abs(cont2bincorr1a(x,v_r,p,v_tot)-r(1));
fun = @(x) myfun(x,v_r,p);
v_tr1 = fminbnd(fun,1e-10,max(v_tot)-v_r-v_c);
myfun = @(x,v_r,p) abs(cont2bincorr1b(x,v_r,p,v_tot)-r(2));
fun = @(x) myfun(x,v_r,p);
v_tr2 = fminbnd(fun,1e-10,max(v_tot)-v_r-v_c);

%optimize v_tc
myfun = @(x,v_c,p) abs(cont2bincorr2a(x,v_c,p,v_tot)-r(3));
fun = @(x) myfun(x,v_c,p);
v_tc1 = fminbnd(fun,1e-10,max(v_tot)-v_r-v_c-min(v_tr1,v_tr2));
myfun = @(x,v_c,p) abs(cont2bincorr2b(x,v_c,p,v_tot)-r(4));
fun = @(x) myfun(x,v_c,p);
v_tc2 = fminbnd(fun,1e-10,max(v_tot)-v_r-v_c-min(v_tr1,v_tr2));

%optimize v_rc
myfun = @(x,v_r,v_c,p) abs(cont2bincorr3(x,v_r,v_c,p,v_tot)-r(5));
fun = @(x) myfun(x,v_r,v_c,p);
v_rc = fminbnd(fun,1e-10,max(v_tot)-v_r-v_c-min(v_tr1,v_tr2)-min(v_tc1,v_tc2));

v_e1 = v_tot(1) - v_r - v_c - v_tr1 - v_tc1 - v_rc;
v_e2 = v_tot(2) - v_r - v_c - v_tr2 - v_tc2 - v_rc; 
v = [v_r v_c v_tr1 v_tr2 v_tc1 v_tc2 v_rc v_e1 v_e2];

function r_ret=cont2bincorr45(xx,p,v_tot)
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde1 = sqrt(v_tot(1) - xx);
sigma_tilde2 = sqrt(v_tot(2) - xx);
sig = sqrt(xx);
f = @(x) normpdf(x,0,sig).*normcdf((mu+tau(1)+x)./sigma_tilde1).*normcdf((mu+tau(2)+x)./sigma_tilde2);
Q = quadgk(@(x) f(x),-inf,inf);
r_ret = (Q - p(1)*p(2))/sqrt(p(1)*(1-p(1))*p(2)*(1-p(2)));

function r_ret=cont2bincorr1a(xx,vr,p,v_tot)
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde = sqrt(v_tot(1) - vr - xx);
sig = sqrt(vr + xx);
f = @(x,tau) normpdf(x,0,sig).*(normcdf((mu+tau(1)+x)./sigma_tilde).^2);
Q = quadgk(@(x) f(x,tau),-inf,inf);
r_ret = (Q - p(1)^2)/(p(1)*(1-p(1)));

function r_ret=cont2bincorr1b(xx,vr,p,v_tot)
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde = sqrt(v_tot(2) - vr - xx);
sig = sqrt(vr + xx);
f = @(x) normpdf(x,0,sig).*(normcdf((mu+tau(2)+x)./sigma_tilde).^2);
Q = quadgk(@(x) f(x),-inf,inf);
r_ret = (Q - p(2)^2)/(p(2)*(1-p(2)));


function r_ret=cont2bincorr2a(xx,vc,p,v_tot)
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde = sqrt(v_tot(1) - vc - xx);
sig = sqrt(vc + xx);
f = @(x) normpdf(x,0,sig).*(normcdf((mu+tau(1)+x)./sigma_tilde).^2);
Q = quadgk(@(x) f(x),-inf,inf);
r_ret = (Q - p(1)^2)/(p(1)*(1-p(1)));

function r_ret=cont2bincorr2b(xx,vc,p,v_tot)
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde = sqrt(v_tot(2) - vc - xx);
sig = sqrt(vc + xx);
f = @(x) normpdf(x,0,sig).*(normcdf((mu+tau(2)+x)./sigma_tilde).^2);
Q = quadgk(@(x) f(x),-inf,inf);
r_ret = (Q - p(2)^2)/(p(2)*(1-p(2)));

function r_ret=cont2bincorr3(xx,vr,vc,p,v_tot)  
tau = sqrt(v_tot).*norminv(p);
mu = mean(tau);
tau = tau-mu;
sigma_tilde1 = sqrt(v_tot(1) - vr - vc - xx);
sigma_tilde2 = sqrt(v_tot(2) - vr - vc - xx);
sig = sqrt(vr + vc + xx);
f = @(x) normpdf(x,0,sig).*normcdf((mu+tau(1)+x)./sigma_tilde1).*normcdf((mu+tau(2)+x)./sigma_tilde2);
Q = quadgk(@(x) f(x),-inf,inf);
r_ret = (Q - p(1)*p(2))/sqrt(p(1)*(1-p(1))*p(2)*(1-p(2)));