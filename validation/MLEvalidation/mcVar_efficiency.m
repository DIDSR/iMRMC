clc
clear all
close all
[num,txt,raw] = xlsread('input1.xlsx','sheet1');
plotcolor = {'r','b','k'};
plotsign = {'o','+','s'};
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

varAUCAcol = strmatch('McvarAUCA',txt(1,:),'exact');
varAUCBcol = strmatch('McvarAUCB',txt(1,:),'exact');
varAUCABcol = strmatch('McvarAUCAminusAUCB',txt(1,:),'exact');


colorlist = {'r','g','b'};
signlist = {'o','+','s'};
sortmcVarA=zeros(length(studygrouplist)-1,length(readersizelist)*length(casesizelist)*4);
sortmcVarB=sortmcVarA;
sortmcVarAB=sortmcVarA;
secondticklabel={};
realAUClist= [0.702,0.855,0.962];
firstticklabel={''};
for  i=1:length(mulist)
    count = 1;
     mu_group = num(find(num(:,mucol)==mulist(i)),:);
     for j= 1:length(readersizelist)
        mu_rs_group = mu_group(find(mu_group(:,readersizecol)==readersizelist(j)),:);
        for k = 1:length(casesizelist)
            mu_rs_cs_group = mu_rs_group(find(mu_rs_group(:,casesizecol)==casesizelist(k)),:);

           
                mu_rs_cs_gs_full = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(1)),:);
                
                
                hrhc_full= mu_rs_cs_gs_full(find(mu_rs_cs_gs_full(:,rvarcol)>0.01&mu_rs_cs_gs_full(:,cvarcol)==0.3),:);
                hrlc_full= mu_rs_cs_gs_full(find(mu_rs_cs_gs_full(:,rvarcol)>0.01&mu_rs_cs_gs_full(:,cvarcol)==0.1),:);
                lrhc_full= mu_rs_cs_gs_full(find(mu_rs_cs_gs_full(:,rvarcol)<0.01&mu_rs_cs_gs_full(:,cvarcol)==0.3),:);
                lrlc_full= mu_rs_cs_gs_full(find(mu_rs_cs_gs_full(:,rvarcol)<0.01&mu_rs_cs_gs_full(:,cvarcol)==0.1),:);
                
                mu_rs_cs_gs_2 = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(2)),:);
                hrhc_2= mu_rs_cs_gs_2(find(mu_rs_cs_gs_2(:,rvarcol)>0.01&mu_rs_cs_gs_2(:,cvarcol)==0.3),:);
                hrlc_2= mu_rs_cs_gs_2(find(mu_rs_cs_gs_2(:,rvarcol)>0.01&mu_rs_cs_gs_2(:,cvarcol)==0.1),:);
                lrhc_2= mu_rs_cs_gs_2(find(mu_rs_cs_gs_2(:,rvarcol)<0.01&mu_rs_cs_gs_2(:,cvarcol)==0.3),:);
                lrlc_2= mu_rs_cs_gs_2(find(mu_rs_cs_gs_2(:,rvarcol)<0.01&mu_rs_cs_gs_2(:,cvarcol)==0.1),:);
                
                mu_rs_cs_gs_3 = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(3)),:);
                hrhc_3= mu_rs_cs_gs_3(find(mu_rs_cs_gs_3(:,rvarcol)>0.01&mu_rs_cs_gs_3(:,cvarcol)==0.3),:);
                hrlc_3= mu_rs_cs_gs_3(find(mu_rs_cs_gs_3(:,rvarcol)>0.01&mu_rs_cs_gs_3(:,cvarcol)==0.1),:);
                lrhc_3= mu_rs_cs_gs_3(find(mu_rs_cs_gs_3(:,rvarcol)<0.01&mu_rs_cs_gs_3(:,cvarcol)==0.3),:);
                lrlc_3= mu_rs_cs_gs_3(find(mu_rs_cs_gs_3(:,rvarcol)<0.01&mu_rs_cs_gs_3(:,cvarcol)==0.1),:);
                
                sortmcVarA(1,count:count+3) = [hrlc_full(varAUCAcol)/hrlc_2(varAUCAcol),lrlc_full(varAUCAcol)/lrlc_2(varAUCAcol),hrhc_full(varAUCAcol)/hrhc_2(varAUCAcol),lrhc_full(varAUCAcol)/lrhc_2(varAUCAcol)];
                sortmcVarA(2,count:count+3) = [hrlc_full(varAUCAcol)/hrlc_3(varAUCAcol),lrlc_full(varAUCAcol)/lrlc_3(varAUCAcol),hrhc_full(varAUCAcol)/hrhc_3(varAUCAcol),lrhc_full(varAUCAcol)/lrhc_3(varAUCAcol)];
          
%                 sortmcVarB(l,count:count+3) = [hrlc(varAUCBcol),lrlc(varAUCBcol),hrhc(varAUCBcol),lrhc(varAUCBcol)];
%                 sortmcVarAB(l,count:count+3) = [hrlc(varAUCABcol),lrlc(varAUCABcol),hrhc(varAUCABcol),lrhc(varAUCABcol)];



%           
            count = count + 4;
            %secondticklabel = [secondticklabel;[{'Reader',num2str(readersizelist(j)),'/Case',num2str(casesizelist(k))}]];
            secondticklabel = [secondticklabel;[num2str(readersizelist(j)),'/',num2str(casesizelist(k))]];
            firstticklabel = [firstticklabel,'HL','LL','HH','LH'];
        end
     end
     % plot for mod A
%     figure(i*3-2)
     subplot(3,1,i);
     hold on
     for l = 1:2
        plot(1:size(sortmcVarA,2),sortmcVarA(l,:),strjoin([colorlist(3),signlist(l)],''),'MarkerSize',8);
     end
     legend('fully crossed/2 split groups','fully crossed/3 split gorups','FontSize',6);
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 max(sortmcVarA(:))*1.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     set(gca,'FontSize',16)
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.25*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center','FontSize',16);
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortmcVarA(:))+0.1],'LineStyle','- -','color','k');
     end 
     ylabel('Efficiency ratio','FontSize',16);

     %title(a,['Mod A mcVar for mu = ',num2str(realAUClist(i))]);
     hold off

%      % plot for mod B
%      figure(i*3-1)
%      hold on
%      for l = 1:length(studygrouplist)
%         plot(1:size(sortmcVarB,2),sortmcVarB(l,:),strjoin([colorlist(l),signlist(1)],''));
%      end
%      legend('fullly crossed','2 split groups','3 split gorups');
%      totalx=4*length(readersizelist)*length(casesizelist);
%      axis([0 totalx 0 max(sortmcVarB(:))*1.1])
%      ax = gca;
%      ax.XTick = 0:totalx;
%      ax.XTickLabel  = firstticklabel;
%      xt=get(gca,'XTick');
%      yl=get(gca,'YLim');
%      for k=1:length(readersizelist)*length(casesizelist)
%          text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
%          line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortmcVarB(:))+0.1],'LineStyle','- -','color','k');
%      end 
%      ylabel('mcVarB');
%      title(['Mod B mcVar for mu = ',num2str(mulist(i))]);
%      hold off
%      
%      % plot for mod AB
%      figure(i*3)
%      hold on
%      for l = 1:length(studygrouplist)
%         plot(1:size(sortmcVarAB,2),sortmcVarAB(l,:),strjoin([colorlist(l),signlist(1)],''));
%      end
%      legend('fullly crossed','2 split groups','3 split gorups');
%      totalx=4*length(readersizelist)*length(casesizelist);
%      axis([0 totalx 0 max(sortmcVarAB(:))*1.1])
%      ax = gca;
%      ax.XTick = 0:totalx;
%      ax.XTickLabel  = firstticklabel;
%      xt=get(gca,'XTick');
%      yl=get(gca,'YLim');
%      for k=1:length(readersizelist)*length(casesizelist)
%          text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
%          line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortmcVarAB(:))+0.1],'LineStyle','- -','color','k');
%      end 
%      ylabel('mcVarAB');
%      title(['Mod Diff mcVar for mu = ',num2str(mulist(i))]);
%      hold off
end
subplot(3,1,1);
title({'Efficiency ratio between fully-crossed and split-plot study';'for MC variance of AUC estimation'},'FontSize',16);
     dim = [.4 .7 .3 .1];
     str = ['Design AUC = ', num2str(realAUClist(1))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);
     
     dim = [.4 .4 .3 .1];
     str = ['Design AUC = ', num2str(realAUClist(2))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);
     
     dim = [.4 .1 .3 .1];
     str = ['Design AUC = ', num2str(realAUClist(3))];
     annotation('textbox',dim,'String',str,'FitBoxToText','on','FontSize',12);

        
     dim = [.05 .6 .1 .1];
     annotation('textbox',dim,'String','A.','EdgeColor','none','FontSize',16,'FontWeight','bold');
     
     dim = [.05 .3 .1 .1];
     annotation('textbox',dim,'String','B.','EdgeColor','none','FontSize',16,'FontWeight','bold');
    
     dim = [.05 .01 .1 .1];
     annotation('textbox',dim,'String','C.','EdgeColor','none','FontSize',16,'FontWeight','bold');
     
     
     