clc
clear all
%close all
[num,txt,raw] = xlsread('input1.xlsx','sheet1');
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

Normalcol = strmatch('McmeanrejectNormal',txt(1,:),'exact');
BDGcol = strmatch('McmeanrejectBDG',txt(1,:),'exact');
Hilliscol = strmatch('McmeanrejectHillis',txt(1,:),'exact');
realAUClist= [0.702,0.855,0.962];
colorlist = {'r','g','b'};
signlist = {'o','+','s','x'};
sortNormal=zeros(length(studygrouplist),length(readersizelist)*length(casesizelist)*4);
sortBDG=sortNormal;
secondticklabel={};
thirdticklabel={};
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
                
                if l ==1
                    sortHillis(1,count:count+3) = [hrlc(Hilliscol),lrlc(Hilliscol),hrhc(Hilliscol),lrhc(Hilliscol)];
                end
            end
            count = count + 4;
           % secondticklabel = [secondticklabel;['Reader',num2str(readersizelist(j)),'/Case',num2str(casesizelist(k))]];
            secondticklabel = [secondticklabel;[num2str(readersizelist(j)),'/',num2str(casesizelist(k))]];
            firstticklabel = [firstticklabel,'HL','LL','HH','LH'];
        end
     end
%      % plot for normal reject
%      figure(i*2-1)
%      hold on
%      for l = 1:length(studygrouplist)
%         plot(1:size(sortNormal,2),sortNormal(l,:),strjoin([colorlist(l),signlist(1)],''));
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
%      ylabel('Observed Type 1 Error Rate');
%      %title(['MC mean Normal Reject for mu = ',num2str(mulist(i))]);
%      title({'Degrees of Freedom by Normal Approximation';['AUC = ',num2str(AUClist(i))];'(100,000 MC trials)'});
%      hold off
     % plot for BDG reject
%     figure(i*2)
     sortall = [sortBDG;sortHillis];
     subplot(3,1,i);
     hold on
%      for l = 1:length(studygrouplist)+1
%         plot(1:size(sortall,2),sortall(l,:),strjoin([colorlist(3),signlist(l)],''),'MarkerSize',8);
%      end
     plot((1:size(sortall,2))-0.1,sortall(1,:),strjoin([colorlist(3),signlist(1)],''),'MarkerSize',8);
     plot((1:size(sortall,2))+0.1,sortall(2,:),strjoin([colorlist(3),signlist(2)],''),'MarkerSize',8);
     plot((1:size(sortall,2))+0.1,sortall(3,:),strjoin([colorlist(3),signlist(3)],''),'MarkerSize',8);
     plot((1:size(sortall,2))-0.1,sortall(4,:),strjoin([colorlist(3),signlist(4)],''),'MarkerSize',8);
     legend('BDG fullly crossed','BDG 2 split groups','BDG 3 split gorups','Hills fully crossed');
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx+0.15 0 0.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     set(gca,'FontSize',12);
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.18*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center','FontSize',16);
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 0.1],'LineStyle','- -','color','k');
     end 
     line([0 totalx+0.15],[0.04,0.04],'LineStyle',':','color','m');
     line([0 totalx+0.15],[0.06,0.06],'LineStyle',':','color','m');
     ylabel({'Observed Type 1'; 'Error Rate'});
     %title(['MC mean BDG Reject for mu = ',num2str(mulist(i))]);
    % title({['AUC = ',num2str(AUClist(i))];'(100,000 MC trials)'});
     hold off
end
subplot(3,1,1);
title('MC Mean BDG and Hillis Model Observed Type 1 Error Rates','FontSize',16);
     dim = [.4 .8 .3 .1];
     str = ['Designed AUC = ', num2str(realAUClist(1))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);
     
     dim = [.4 .5 .3 .1];
     str = ['Designed AUC = ', num2str(realAUClist(2))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);
     
     dim = [.4 .2 .3 .1];
     str = ['Designed AUC = ', num2str(realAUClist(3))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);

        
     dim = [.05 .67 .01 .01];
     annotation('textbox',dim,'String','A.','EdgeColor','none','FontSize',16,'FontWeight','bold');
     
     dim = [.05 .37 .01 .01];
     annotation('textbox',dim,'String','B.','EdgeColor','none','FontSize',16,'FontWeight','bold');
    
     dim = [.05 .07 .01 .01];
     annotation('textbox',dim,'String','C.','EdgeColor','none','FontSize',16,'FontWeight','bold');
     