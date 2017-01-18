clc
clear all
close all
[num,txt,raw] = xlsread('input2.xlsx','sheet1');
plotcolor = {'r','g','b','k'};
plotsign = {'o','*','s'};
plotlegend = {};
% mucol = strmatch('uA',txt(1,:));
% rvarcol = strmatch('AR0',txt(1,:));
% cvarcol = strmatch('AC0',txt(1,:));
% AUCAcol = strmatch('varAUC_A',txt(1,:),'exact');
% AUCBcol = strmatch('varAUC_B',txt(1,:),'exact');
% AUCAminusBcol = strmatch('totalVar',txt(1,:),'exact');
% mulist = unique(num(:,mucol));
% rvarlist = unique(num(:,rvarcol));
% cvarlist = unique(num(:,cvarcol));

readersizecol = strmatch('nr',txt(1,:));
casesizecol = strmatch('n1',txt(1,:));
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));
readersizelist = unique(num(:,readersizecol));
casesizelist = unique(num(:,casesizecol));
studygrouplist = unique(num(:,studygroupcol));
varAUCAcol = strmatch('mcVarAUC_A',txt(1,:),'exact');
varAUCBcol = strmatch('mcVarAUC_B',txt(1,:),'exact');
varAUCAminusBcol = strmatch('mcVarAUC_AB',txt(1,:),'exact');
for i = 1: length(casesizelist)
    csize_group = num(find(num(:,casesizecol)==casesizelist(i)),:);
    for j = 1: length(readersizelist)
       csize_rsize_group = csize_group(find(csize_group(:,readersizecol)==readersizelist(j)),:);
       if ~isempty(csize_rsize_group)
           for k = 1: length(studygrouplist)
               csize_rsize_sg_group = csize_rsize_group(find(csize_rsize_group(:,studygroupcol)==studygrouplist(k)),:);
               if ~isempty(csize_rsize_sg_group)
                   figure(1)
                   plot(casesizelist(i)*ones(1,size(csize_rsize_sg_group,1)),csize_rsize_sg_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(2)
                   plot(casesizelist(i)*ones(1,size(csize_rsize_sg_group,1)),csize_rsize_sg_group(:,varAUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(3)
                   plot(casesizelist(i)*ones(1,size(csize_rsize_sg_group,1)),csize_rsize_sg_group(:,varAUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   plotlegend = [plotlegend,['rsize',num2str(readersizelist(j)),'stuytgroup',num2str(studygrouplist(k))] ];
               end
               a=1;
           end
       end
    end
end
figure(1)
xlabel('case size')
ylabel('mcVarAUC_A')
title('shape for different study group, color for different reader size');
legend(plotlegend);
figure(2)
xlabel('case size')
ylabel('mcVarAUC_B')
title('shape for different study group, color for different reader size');
legend(plotlegend);
figure(3)
xlabel('case size')
ylabel('mcVarAUC_AB')
title('shape for different study group, color for different reader size');
legend(plotlegend);


