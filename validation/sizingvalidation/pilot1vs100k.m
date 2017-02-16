clc
clear all 
close all

[num,txt,raw] = xlsread('pilot10vs100vs1kvs100kOb.xlsx','sheet1');
%[num,txt,raw] = xlsread('pilot10vs100vs1kvs100kp05.xlsx','sheet1');
SE1k = strmatch('PSEBDG1k',txt(1,:),'exact');
SE100k = strmatch('PSEBDG100k',txt(1,:),'exact');
SE100 = strmatch('PSEBDG100',txt(1,:),'exact');
SE10 = strmatch('PSEBDG10',txt(1,:),'exact');
figure
SE1knum = num(:,SE1k);
SE100knum = num(:,SE100k);
SE100num = num(:,SE100);
SE10num = num(:,SE10);
plot(SE100knum,SE1knum,'ob');
hold on
plot(SE100knum,SE100num,'or');
plot(SE100knum,SE10num,'ok');
plot([0,0.1],[0,0.1],'b-');
hold off
title('100k vs 1k trials SE');
xlabel('100k trials SE');
ylabel('small trials SE');
legend('1k trials','100 trials','10 trials');





BDGPower1k = strmatch('PpowerBDG1k',txt(1,:),'exact');
BDGPower100k = strmatch('PpowerBDG100k',txt(1,:),'exact');
BDGPower100 = strmatch('PpowerBDG100',txt(1,:),'exact');
BDGPower10 = strmatch('PpowerBDG10',txt(1,:),'exact');
figure
BDGPower1knum = num(:,BDGPower1k);
BDGPower100knum = num(:,BDGPower100k);
BDGPower100num = num(:,BDGPower100);
BDGPower10num = num(:,BDGPower10);
hold on
plot(BDGPower100knum,BDGPower1knum,'ob');
plot(BDGPower100knum,BDGPower100num,'or');
plot(BDGPower100knum,BDGPower10num,'ok');
plot([0,1],[0,1],'b-');
hold off
title('100k vs 1k trials BDG Power');
xlabel('100k trials BDGPower');
ylabel('small trials BDGPower');
legend('1k trials','100 trials','10 trials');
