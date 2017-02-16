clc
clear all
%close all
[num,txt,raw] = xlsread('input1.xlsx','sheet1');
mcvarAUCAcol = strmatch('McvarAUCA',txt(1,:),'exact');
mcvarAUCBcol = strmatch('McvarAUCB',txt(1,:),'exact');
mcvarAUCAminusBcol = strmatch('McvarAUCAminusAUCB',txt(1,:),'exact');
mcMeanvarAUCAcol = strmatch('McmeanvarA',txt(1,:),'exact');
mcMeanvarAUCBcol = strmatch('McmeanvarB',txt(1,:),'exact');
mcMeanvarAUCAminusBcol = strmatch('McmeanvarAUCAminusAUCB',txt(1,:),'exact');
numvarAUCAcol = strmatch('NumvarA',txt(1,:),'exact');
numvarAUCBcol = strmatch('NumvarB',txt(1,:),'exact');
numvarAUCAminusBcol = strmatch('NumvarAUCAminusAUCB',txt(1,:),'exact');
%mod A
figure(3)
subplot(1,2,1)
plot(num(:,numvarAUCAcol),num(:,mcMeanvarAUCAcol),'o');
hold on
plot([0,0.0045],[0,0.0045],'b-');
hold off
set(gca,'FontSize',12);
xlabel('Numerical VarAUC','FontSize',16);
ylabel('MC Mean VarAUC','FontSize',16);
title({'MC Mean VarAUC vs';'Numerical VarAUC'},'FontSize',16);
axis([0,0.0045,0,0.0045])


subplot(1,2,2)
AUCAperBias = (num(:,mcMeanvarAUCAcol)-num(:,numvarAUCAcol))./num(:,numvarAUCAcol) *100;
plot (num(:,numvarAUCAcol),AUCAperBias,'o');
xlabel('Numerical VarAUC','FontSize',16);
ylabel('Percentage Bias','FontSize',16);
title({'Percentage Bias of MC Mean';'and Numerical VarAUC'},'FontSize',16);
axis([0,0.0045,0,140])
