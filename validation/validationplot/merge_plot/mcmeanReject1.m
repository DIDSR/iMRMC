clc
clear all
close all
[num,txt,raw] = xlsread('input5.xlsx','sheet1');
plotcolor = {'r','b','k'};
plotsign = {'o','*','s'};
plotlegend = {};

mucol = strmatch('uA',txt(1,:));
rvarcol = strmatch('AR0',txt(1,:));
cvarcol = strmatch('AC0',txt(1,:));

readersizecol = strmatch('nr',txt(1,:));
casesizecol = strmatch('n1',txt(1,:));
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));

readersizelist = unique(num(:,readersizecol));
casesizelist = unique(num(:,casesizecol));
studygrouplist = unique(num(:,studygroupcol));
mulist = unique(num(:,mucol));
rvarlist = unique(num(:,rvarcol));
cvarlist = unique(num(:,cvarcol));

Normalcol = strmatch('mcMeanRejectNormal',txt(1,:),'exact');
BDGcol = strmatch('mcRejectBDG',txt(1,:),'exact');
Hilliscol = strmatch('mcRejectHillis',txt(1,:),'exact');


colorlist = {'r','g','b'};
signlist = {'o','*','s'};
sortNormal=zeros(length(studygrouplist),length(readersizelist)*length(casesizelist)*4);
sortBDG=sortNormal;
secondticklabel={};
firstticklabel={''};
for  i=1:length(mulist)
    count = 1;
     mu_group = num(find(num(:,mucol)==mulist(i)),:);
     for j= 1:length(readersizelist)
        mu_rs_group = mu_group(find(mu_group(:,readersizecol)==readersizelist(j)),:);
        for k = 1:length(casesizelist)
            mu_rs_cs_group = mu_rs_group(find(mu_rs_group(:,casesizecol)==casesizelist(k)),:);

            for l = 1:length(studygrouplist)
                mu_rs_cs_gs_group = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(l)),:);
                hrhc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)>0.01&mu_rs_cs_gs_group(:,cvarcol)==0.3),:);
                hrlc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)>0.01&mu_rs_cs_gs_group(:,cvarcol)==0.1),:);
                lrhc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)<0.01&mu_rs_cs_gs_group(:,cvarcol)==0.3),:);
                lrlc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)<0.01&mu_rs_cs_gs_group(:,cvarcol)==0.1),:);
                sortNormal(l,count:count+3) = [hrlc(Normalcol),lrlc(Normalcol),hrhc(Normalcol),lrhc(Normalcol)];
                sortBDG(l,count:count+3) = [hrlc(BDGcol),lrlc(BDGcol),hrhc(BDGcol),lrhc(BDGcol)];
            end
            count = count + 4;
            secondticklabel = [secondticklabel;['Reader',num2str(readersizelist(j)),'/Case',num2str(casesizelist(k))]];
            firstticklabel = [firstticklabel,'HL','LL','HH','LH'];
        end
     end
     % plot for normal reject
     %figure(i*2-1)
%      figure(1)
%      hold on
%      for l = 1:length(studygrouplist)
%         plot(1:size(sortNormal,2),sortNormal(l,:),strjoin([colorlist(l),signlist(i)],''));
%      end
%      legend('fullly crossed','2 split groups','3 split gorups');
%      totalx=4*length(readersizelist)*length(casesizelist);
%      axis([0 totalx 0 0.1])
%      ax = gca;
%      ax.XTick = 0:totalx;
%      ax.XTickLabel  = firstticklabel;
%      xt=get(gca,'XTick');
%      yl=get(gca,'YLim');
%      for k=1:length(readersizelist)*length(casesizelist)
%          text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
%          line([4.5+(k-1)*4 4.5+(k-1)*4], [0 0.1],'LineStyle','- -','color','k');
%      end 
%      line([0 totalx],[0.04,0.04],'LineStyle',':','color','m');
%      line([0 totalx],[0.06,0.06],'LineStyle',':','color','m');
%      ylabel('Tpye 1 Error Rate');
%      title(['MC mean Normal Reject for mu = ',num2str(mulist(i))]);
%      hold off
     % plot for BDG reject
     %figure(i*2)
     figure(2)
     hold on
     for l = 1:length(studygrouplist)
        plot(1:size(sortBDG,2),sortBDG(l,:),strjoin([colorlist(l),signlist(i)],''));
     end
     hold off

end
    figure(2)
     legend('mu:0.75, fullly crossed','mu:0.75, 2 split groups','mu:0.75, 3 split gorups','mu:1.5, fullly crossed','mu:1.5, 2 split groups','mu:1.5, 3 split gorups','mu:2.5, fullly crossed','mu:2.5, 2 split groups','mu:2.5, 3 split gorups');
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 0.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 0.1],'LineStyle','- -','color','k');
     end 
     line([0 totalx],[0.04,0.04],'LineStyle',':','color','m');
     line([0 totalx],[0.06,0.06],'LineStyle',':','color','m');
     ylabel('Tpye 1 Error Rate');
     title('MC mean BDG Reject');
     hold off
