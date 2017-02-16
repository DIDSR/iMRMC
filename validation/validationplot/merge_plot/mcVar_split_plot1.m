clc
clear all
close all
[num,txt,raw] = xlsread('input5.xlsx','sheet1');
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

varAUCAcol = strmatch('mcVarAUC_A',txt(1,:),'exact');
varAUCBcol = strmatch('mcVarAUC_B',txt(1,:),'exact');
varAUCABcol = strmatch('mcVarAUC_AB',txt(1,:),'exact');


colorlist = {'r','g','b'};
signlist = {'o','+','s'};
sortmcVarA=zeros(length(studygrouplist)-1,length(readersizelist)*length(casesizelist)*4);
sortmcVarB=sortmcVarA;
sortmcVarAB=sortmcVarA;
listfull = zeros(2,length(readersizelist)*length(casesizelist));
list2 = zeros(2,length(readersizelist)*length(casesizelist));
list3 = zeros(2,length(readersizelist)*length(casesizelist));
    count = 1;
     mu_group = num(find(num(:,mucol)==mulist(2)),:);
     for j= 1:length(readersizelist)
        mu_rs_group = mu_group(find(mu_group(:,readersizecol)==readersizelist(j)),:);
        for k = 1:length(casesizelist)
            mu_rs_cs_group = mu_rs_group(find(mu_rs_group(:,casesizecol)==casesizelist(k)),:);

           
                mu_rs_cs_gs_full = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(1)),:);
                
                

                lrhc_full= mu_rs_cs_gs_full(find(mu_rs_cs_gs_full(:,rvarcol)<0.01&mu_rs_cs_gs_full(:,cvarcol)==0.3),:);

                mu_rs_cs_gs_2 = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(2)),:);

                lrhc_2= mu_rs_cs_gs_2(find(mu_rs_cs_gs_2(:,rvarcol)<0.01&mu_rs_cs_gs_2(:,cvarcol)==0.3),:);

                
                mu_rs_cs_gs_3 = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(3)),:);

                lrhc_3= mu_rs_cs_gs_3(find(mu_rs_cs_gs_3(:,rvarcol)<0.01&mu_rs_cs_gs_3(:,cvarcol)==0.3),:);
                listfull(:,count) = [lrhc_full(varAUCAcol);readersizelist(j)*casesizelist(k)*2];
                list2(:,count)  = [lrhc_2(varAUCAcol);readersizelist(j)*casesizelist(k)/2*2];
                list3(:,count) = [lrhc_3(varAUCAcol);readersizelist(j)*casesizelist(k)/3*2];
                count = count+1;
        end
     end
     % plot for mod A
%     figure(i*3-2)
     figure(1)
     hold on
     plot(listfull(2,:),listfull(1,:),'bo');
     plot(list2(2,:),list2(1,:),'b+');
     plot(list3(2,:),list3(1,:),'bs');
     legend('fully crossed','2 split groups','3 split groups');
     totalx=4*length(readersizelist)*length(casesizelist);
     set(gca,'FontSize',16)
     xlable('obvervation size');
     ylabel('variance','FontSize',16);

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


     
     
     