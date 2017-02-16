clc
clear all 
%close all

%[num,txt,raw] = xlsread('pilotStudy1_4p05.xlsx','sheet1');
[num,txt,raw] = xlsread('pilotStudy1_4.xlsx','sheet1');
%[num,txt,raw] = xlsread('pilotStudy5_8.xlsx','sheet1');
pilotSE = strmatch('PSEBDG',txt(1,:),'exact');
sizingSE = strmatch('BDGSE',txt(1,:),'exact');
figure
pilotSEnum = num(:,pilotSE);
sizingSEnum = num(:,sizingSE);
plot(sizingSEnum,pilotSEnum,'o');
hold on
plot([0,0.1],[0,0.1],'b-');
hold off
title('large sample size vs pilot');
xlabel('large size SE');
ylabel('1 trial pilot study SE');






pilotBDGPower = strmatch('PpowerBDG',txt(1,:),'exact');
BDGPower = strmatch('BDGPower',txt(1,:),'exact');
figure
pilotBDGPowernum = num(:,pilotBDGPower);
BDGPowernum = num(:,BDGPower);
hold on
plot(BDGPowernum,pilotBDGPowernum,'o');
plot([0,1],[0,1],'b-');
hold off
title('large sample size vs pilot study');
xlabel('large sample size BDGPower');
ylabel('1 trial pilot study BDGPower');
