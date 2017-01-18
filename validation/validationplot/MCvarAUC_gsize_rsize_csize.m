clc
clear all
close all
[num,txt,raw] = xlsread('input2.xlsx','sheet1');
plotcolor = {'r','g','b','k'};
plotsign = {'o','*'};
plotlegend = {};
% mucol = strmatch('uA',txt(1,:));
% rvarcol = strmatch('AR0',txt(1,:));
% cvarcol = strmatch('AC0',txt(1,:));
%mulist = unique(num(:,mucol));
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

for i = 1: length(studygrouplist)
    sg_group = num(find(num(:,studygroupcol)==studygrouplist(i)),:);
    for j = 1: length(readersizelist)
       sg_rsize_group = sg_group(find(sg_group(:,readersizecol)==readersizelist(j)),:);
       if ~isempty(sg_rsize_group)
           for k = 1: length(casesizelist)
               sg_rsize_csize_group = sg_rsize_group(find(sg_rsize_group(:,casesizecol)==casesizelist(k)),:);
               if ~isempty(sg_rsize_csize_group)
                   figure(1)
                   plot(studygrouplist(i)*ones(1,size(sg_rsize_csize_group,1)),sg_rsize_csize_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(2)
                   plot(studygrouplist(i)*ones(1,size(sg_rsize_csize_group,1)),sg_rsize_csize_group(:,varAUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(3)
                   plot(studygrouplist(i)*ones(1,size(sg_rsize_csize_group,1)),sg_rsize_csize_group(:,varAUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   plotlegend = [plotlegend,['rsize',num2str(readersizelist(j)),'csize',num2str(casesizelist(k))] ];
               end
               a=1;
           end
       end
    end
end
figure(1)
xlabel('# of study group')
ylabel('mcVarAUC_A')
title('shape for different case size, color for different reader size');
legend(plotlegend);
figure(2)
xlabel('# of study group')
ylabel('mcVarAUC_B')
title('shape for different case size, color for different reader size');
legend(plotlegend);
figure(3)
xlabel('# of study group')
ylabel('mcVarAUC_AB')
title('shape for different case size, color for different reader size');
legend(plotlegend);


