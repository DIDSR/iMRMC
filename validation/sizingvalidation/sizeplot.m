clc
clear all 
close all

[num,txt,raw] = xlsread('SizingFinal1.xlsx','sheet1');
mcvarAB = strmatch('varAUCAminusAUCB',txt(1,:),'exact');
sizingSE = strmatch('BDGSE',txt(1,:),'exact');
figure(1)
mcVarABnum = num(:,mcvarAB);
mcSE = sqrt(mcVarABnum);
sizingSEnum = num(:,sizingSE);
plot(mcSE,sizingSEnum,'o','MarkerSize',8);
hold on
plot([0,0.1],[0,0.1],'b-');
hold off
title('Estimated AUC Standard Error');
set(gca,'FontSize',16)
xlabel('MC mean of 100,000 trials simulation Standard Error');
ylabel('Sizing Study Standard Error');


rejectBDG = strmatch('rejectBDG',txt(1,:),'exact');
BDGPower = strmatch('BDGPower',txt(1,:),'exact');
figure(2)
rejectBDGnum = num(:,rejectBDG);
BDGPowernum = num(:,BDGPower);

plot(rejectBDGnum,BDGPowernum,'o','MarkerSize',8);
hold on
plot([0,1],[0,1],'b-');
hold off
set(gca,'FontSize',16)
title('Hypothesis Testing Result');
xlabel('MC mean of 100,000 trials reject decision');
ylabel('Sizing Study Power');


powerdif = (rejectBDGnum -BDGPowernum);
perpowerdif = powerdif./rejectBDGnum*100;
figure(3)
%plot(rejectBDGnum,perpowerdif,'o');
plot(rejectBDGnum,powerdif,'o');
%plot((1:size(perpowerdif,1)),perpowerdif,'o');
title('percentage difference between reject and power');
xlabel('MC mean of rejectBDG');
ylabel('difference between reject and Power');