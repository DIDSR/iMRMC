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


mcvarvarAUCAcol = strmatch('mcVarvarAUC_A',txt(1,:),'exact');
mcvarvarAUCBcol = strmatch('mcVarvarAUC_B',txt(1,:),'exact');
mcvarvarAUCABcol = strmatch('mcVarvarAUC_AB',txt(1,:),'exact');

NumvarAUCAcol = strmatch('NumvarAUC_A',txt(1,:),'exact');
NumvarAUCBcol = strmatch('NumvarAUC_B',txt(1,:),'exact');
NumvarAUCABcol = strmatch('NumvarAUC_AB',txt(1,:),'exact');



colorlist = {'r','g','b'};
signlist = {'o','*','s'};
sortRSEAUCA=zeros(length(studygrouplist),length(readersizelist)*length(casesizelist)*4);
sortRSEAUCB=sortRSEAUCA;
sortRSEAUCAB=sortRSEAUCA;
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
                RSEhrhcA =  sqrt(hrhc(:,mcvarvarAUCAcol(1)))./hrhc(:,NumvarAUCAcol(1));
                RSEhrlcA =  sqrt(hrlc(:,mcvarvarAUCAcol(1)))./hrlc(:,NumvarAUCAcol(1));
                RSElrhcA =  sqrt(lrhc(:,mcvarvarAUCAcol(1)))./lrhc(:,NumvarAUCAcol(1));
                RSElrlcA =  sqrt(lrlc(:,mcvarvarAUCAcol(1)))./lrlc(:,NumvarAUCAcol(1));
                sortRSEAUCA (l,count:count+3) = [RSEhrlcA,RSElrlcA,RSEhrhcA,RSElrhcA];
                
                RSEhrhcB =  sqrt(hrhc(:,mcvarvarAUCBcol(1)))./hrhc(:,NumvarAUCBcol(1));
                RSEhrlcB =  sqrt(hrlc(:,mcvarvarAUCBcol(1)))./hrlc(:,NumvarAUCBcol(1));
                RSElrhcB =  sqrt(lrhc(:,mcvarvarAUCBcol(1)))./lrhc(:,NumvarAUCBcol(1));
                RSElrlcB =  sqrt(lrlc(:,mcvarvarAUCBcol(1)))./lrlc(:,NumvarAUCBcol(1));
                sortRSEAUCB (l,count:count+3) = [RSEhrlcB,RSElrlcB,RSEhrhcB,RSElrhcB];
                
                RSEhrhcAB =  sqrt(hrhc(:,mcvarvarAUCABcol(1)))./hrhc(:,NumvarAUCABcol(1));
                RSEhrlcAB =  sqrt(hrlc(:,mcvarvarAUCABcol(1)))./hrlc(:,NumvarAUCABcol(1));
                RSElrhcAB =  sqrt(lrhc(:,mcvarvarAUCABcol(1)))./lrhc(:,NumvarAUCABcol(1));
                RSElrlcAB =  sqrt(lrlc(:,mcvarvarAUCABcol(1)))./lrlc(:,NumvarAUCABcol(1));
                sortRSEAUCAB (l,count:count+3) = [RSEhrlcAB,RSElrlcAB,RSEhrhcAB,RSElrhcAB];
            end
            count = count + 4;
            secondticklabel = [secondticklabel;['Reader',num2str(readersizelist(j)),'/Case',num2str(casesizelist(k))]];
            firstticklabel = [firstticklabel,'HL','LL','HH','LH'];
        end
     end
     % plot for mod A
     figure(i*3-2)
     hold on
     for l = 1:length(studygrouplist)
        plot(1:size(sortRSEAUCA,2),sortRSEAUCA(l,:),strjoin([colorlist(l),signlist(1)],''));
     end
     legend('fullly crossed','2 split groups','3 split gorups');
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 max(sortRSEAUCA(:))+0.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortRSEAUCA(:))+0.1],'LineStyle','- -','color','k');
     end 
     ylabel('RSE');
     title(['Mod A Relative SE for mu = ',num2str(mulist(i))]);
     hold off

     % plot for mod B
     figure(i*3-1)
     hold on
     for l = 1:length(studygrouplist)
        plot(1:size(sortRSEAUCB,2),sortRSEAUCB(l,:),strjoin([colorlist(l),signlist(1)],''));
     end
     legend('fullly crossed','2 split groups','3 split gorups');
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 max(sortRSEAUCB(:))+0.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortRSEAUCB(:))+0.1],'LineStyle','- -','color','k');
     end 
     ylabel('RSE');
     title(['Mod B Relative SE for mu = ',num2str(mulist(i))]);
     hold off
     
     % plot for mod AB
     figure(i*3)
     hold on
     for l = 1:length(studygrouplist)
        plot(1:size(sortRSEAUCAB,2),sortRSEAUCAB(l,:),strjoin([colorlist(l),signlist(1)],''));
     end
     legend('fullly crossed','2 split groups','3 split gorups');
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 max(sortRSEAUCAB(:))+0.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.075*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center');
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 max(sortRSEAUCAB(:))+0.1],'LineStyle','- -','color','k');
     end 
     ylabel('RSE');
     title(['Mod Diff Relative SE for mu = ',num2str(mulist(i))]);
     hold off
end
