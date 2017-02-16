clc
clear all
close all
[num,txt,raw] = xlsread('input2.xlsx','sheet1');
plotcolor = {'r','g','b','k'};
plotsign = {'o','*','s'};
plotlegend = {};
mucol = strmatch('uA',txt(1,:));
rvarcol = strmatch('AR0',txt(1,:));
cvarcol = strmatch('AC0',txt(1,:));
AUCAcol = strmatch('mcMeanAUC_A',txt(1,:),'exact');
AUCBcol = strmatch('mcMeanAUC_B',txt(1,:),'exact');
AUCAminusBcol = strmatch('mcMeanAUC_AB',txt(1,:),'exact');
mulist = unique(num(:,mucol));
rvarlist = unique(num(:,rvarcol));
cvarlist = unique(num(:,cvarcol));
for i = 1: length(cvarlist)
    cvar_group = num(find(num(:,cvarcol)==cvarlist(i)),:);
    for j = 1: length(rvarlist)
       cvar_rvar_group = cvar_group(find(cvar_group(:,rvarcol)==rvarlist(j)),:);
       if ~isempty(cvar_rvar_group)
           for k = 1: length(mulist)
               cvar_rvar_mu_group = cvar_rvar_group(find(cvar_rvar_group(:,mucol)==mulist(k)),:);
               if ~isempty(cvar_rvar_mu_group)
                   figure(1)
                   plot(cvarlist(i)*ones(1,size(cvar_rvar_mu_group,1)),cvar_rvar_mu_group(:,AUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(2)
                   plot(cvarlist(i)*ones(1,size(cvar_rvar_mu_group,1)),cvar_rvar_mu_group(:,AUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(3)
                   plot(cvarlist(i)*ones(1,size(cvar_rvar_mu_group,1)),cvar_rvar_mu_group(:,AUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   plotlegend = [plotlegend,['rvar',num2str(rvarlist(j)),'mu',num2str(mulist(k))] ];
               end
               a=1;
           end
       end
    end
end
figure(1)
xlabel('mu')
ylabel('AUC_A')
title('shape for different mu, color for different reader var');
legend(plotlegend);
figure(2)
xlabel('mu')
ylabel('AUC_B')
title('shape for different mu, color for different reader var');
legend(plotlegend);
figure(3)
xlabel('mu')
ylabel('AUC_A-AUC_B')
title('shape for different mu, color for different reader var');
legend(plotlegend);


