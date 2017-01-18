clc
clear all
close all
[num,txt,raw] = xlsread('input2.xlsx','sheet1');
plotcolor = {'r','b'};
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
for i = 1: length(rvarlist)
    rvar_group = num(find(num(:,rvarcol)==rvarlist(i)),:);
    for j = 1: length(cvarlist)
       rvar_cvar_group = rvar_group(find(rvar_group(:,cvarcol)==cvarlist(j)),:);
       if ~isempty(rvar_cvar_group)
           for k = 1: length(mulist)
               rvar_cvar_mu_group = rvar_cvar_group(find(rvar_cvar_group(:,mucol)==mulist(k)),:);
               if ~isempty(rvar_cvar_mu_group)
                   figure(1)
                   plot(rvarlist(i)*ones(1,size(rvar_cvar_mu_group,1)),rvar_cvar_mu_group(:,AUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(2)
                   plot(rvarlist(i)*ones(1,size(rvar_cvar_mu_group,1)),rvar_cvar_mu_group(:,AUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(3)
                   plot(rvarlist(i)*ones(1,size(rvar_cvar_mu_group,1)),rvar_cvar_mu_group(:,AUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   plotlegend = [plotlegend,['mu',num2str(cvarlist(j)),'cvar',num2str(mulist(k))] ];
               end
               a=1;
           end
       end
    end
end
figure(1)
xlabel('reader var')
ylabel('mcMeanAUC_A')
title('shape for different mu, color for different case var');
legend(plotlegend);
figure(2)
xlabel('reader var')
ylabel('mcMeanAUC_B')
title('shape for different mu, color for different case var');
legend(plotlegend);
figure(3)
xlabel('reader var')
ylabel('mcMeanAUC_AB')
title('shape for different mu, color for different case var');
legend(plotlegend);


