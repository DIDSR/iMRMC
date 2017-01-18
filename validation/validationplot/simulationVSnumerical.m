clc
clear all
close all
[num,txt,raw] = xlsread('input5.xlsx','sheet1');
mcvarAUCAcol = strmatch('mcVarAUC_A',txt(1,:),'exact');
mcvarAUCBcol = strmatch('mcVarAUC_B',txt(1,:),'exact');
mcvarAUCAminusBcol = strmatch('mcVarAUC_AB',txt(1,:),'exact');
mcMeanvarAUCAcol = strmatch('mcMeanvarAUC_A',txt(1,:),'exact');
mcMeanvarAUCBcol = strmatch('mcMeanvarAUC_B',txt(1,:),'exact');
mcMeanvarAUCAminusBcol = strmatch('mcMeanvarAUC_AB',txt(1,:),'exact');
numvarAUCAcol = strmatch('NumvarAUC_A',txt(1,:),'exact');
numvarAUCBcol = strmatch('NumvarAUC_B',txt(1,:),'exact');
numvarAUCAminusBcol = strmatch('NumvarAUC_AB',txt(1,:),'exact');
%mod A
figure(1)

plot(num(:,numvarAUCAcol),num(:,mcMeanvarAUCAcol),'o');
hold on
plot([0,0.003],[0,0.003],'b-');
hold off
set(gca,'FontSize',12);
xlabel('Numerical VarAUC','FontSize',16);
ylabel('MC Mean VarAUC','FontSize',16);
title({'Numerical VarAUC vs. MC Mean VarAUC'},'FontSize',16);
% subplot(1,2,2)
% plot(num(:,numvarAUCAcol),num(:,mcvarAUCAcol),'o');
% hold on
% plot([0,0.003],[0,0.003],'b-');
% hold off
% set(gca,'FontSize',12);
% xlabel('Numerical varAUC','FontSize',16);
% ylabel('MC Variance AUC','FontSize',16);
% title({'Numerical varAUC vs.'; 'MC Variance AUC'},'FontSize',16);
%mod B
% figure(3)
% plot(num(:,numvarAUCBcol),num(:,mcMeanvarAUCBcol),'o');
% xlabel('numericalvarAUC_B');
% ylabel('mcMeanVarAUC_B');
% title('Modality B numericalvarAUC vs. mcMeanVarAUC');
% figure(4)
% plot(num(:,numvarAUCBcol),num(:,mcvarAUCBcol),'o');
% xlabel('numericalvarAUC_B');
% ylabel('mcVarAUC_B');
% title('Modality B numericalvarAUC vs. mcVarAUC');
% %mod difference
% figure(5)
% plot(num(:,numvarAUCAminusBcol),num(:,mcMeanvarAUCAminusBcol),'o');
% xlabel('numericalvarAUC_AminusB');
% ylabel('mcMeanVarAUC_AminusB');
% title('Modality A,B difference numericalvarAUC vs. mcMeanVarAUC');
% figure(6)
% plot(num(:,numvarAUCAminusBcol),num(:,mcvarAUCAminusBcol),'o');
% xlabel('numericalvarAUC_AminusB');
% ylabel('mcVarAUC_AminusB');
% title('Modality A,B numericalvarAUC vs. mcVarAUC');
% diff= abs(num(:,numvarAUCAcol)-num(:,mcvarAUCAcol));
% diffp= diff./num(:,numvarAUCAcol)*100;
% figure(7)
% plot(1:size(diff),diffp,'o');
% xlabel('input ID');
% ylabel('percent error %');
% title('percent error between MOD A numerical AUC and mcVarAUC');
% outlierID = find(diffp>0.05);
% outlier = num(outlierID,:);