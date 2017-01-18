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
mulist = unique(num(:,mucol));
rvarlist = unique(num(:,rvarcol));
cvarlist = unique(num(:,cvarcol));

readersizecol = strmatch('nr',txt(1,:));
casesizecol = strmatch('n1',txt(1,:));
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));
readersizelist = unique(num(:,readersizecol));
casesizelist = unique(num(:,casesizecol));
studygrouplist = unique(num(:,studygroupcol));
varAUCAcol = strmatch('mcVarAUC_A',txt(1,:),'exact');
varAUCBcol = strmatch('mcVarAUC_B',txt(1,:),'exact');
varAUCAminusBcol = strmatch('mcVarAUC_AB',txt(1,:),'exact');
a=0;
for i = 1: length(readersizelist)
    rsize_group = num(find(num(:,readersizecol)==readersizelist(i)),:);
    for j = 1: length(casesizelist)
       rsize_csize_group = rsize_group(find(rsize_group(:,casesizecol)==casesizelist(j)),:);
       if ~isempty(rsize_csize_group)
           for k = 1: length(mulist)
               rsize_csize_sg_group = rsize_csize_group(find(rsize_csize_group(:,mucol)==mulist(k)),:);
               if ~isempty(rsize_csize_sg_group)
                   figure(1)
                   plot(readersizelist(i)*ones(1,size(rsize_csize_sg_group,1)),rsize_csize_sg_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(2)
                   plot(readersizelist(i)*ones(1,size(rsize_csize_sg_group,1)),rsize_csize_sg_group(:,varAUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   figure(3)
                   plot(readersizelist(i)*ones(1,size(rsize_csize_sg_group,1)),rsize_csize_sg_group(:,varAUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   plotlegend = [plotlegend,['csize',num2str(casesizelist(j)),'mu',num2str(mulist(k))] ];
               end
               a=a+1;
           end
       end
    end
end
figure(1)
xlabel('reader size')
ylabel('mcVarAUC_A')
title('shape for different mu, color for different case size');
legend(plotlegend);
figure(2)
xlabel('reader size')
ylabel('mcVarAUC_B')
title('shape for different mu, color for different case size');
legend(plotlegend);
figure(3)
xlabel('reader size')
ylabel('mcVarAUC_AB')
title('shape for different mu, color for different case size');
legend(plotlegend);


