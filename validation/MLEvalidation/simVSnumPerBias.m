clc
clear all
%close all
[num,txt,raw] = xlsread('input1.xlsx','sheet1');


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



mcMeanvarAUCAcol = strmatch('McmeanvarA',txt(1,:),'exact');
mcMeanvarAUCBcol = strmatch('McmeanvarB',txt(1,:),'exact');
mcMeanvarAUCAminusBcol = strmatch('McmeanvarAUCAminusAUCB',txt(1,:),'exact');

numvarAUCAcol = strmatch('NumvarA',txt(1,:),'exact');
numvarAUCBcol = strmatch('NumvarB',txt(1,:),'exact');
numvarAUCAminusBcol = strmatch('NumvarAUCAminusAUCB',txt(1,:),'exact');

AUCAperBias = (num(:,mcMeanvarAUCAcol)-num(:,numvarAUCAcol))./num(:,numvarAUCAcol) *100;
AUCBperBias = (num(:,mcMeanvarAUCBcol)-num(:,numvarAUCBcol))./num(:,numvarAUCBcol) *100;
AUCABperBias = (num(:,mcMeanvarAUCAminusBcol)-num(:,numvarAUCAminusBcol))./num(:,numvarAUCAminusBcol) *100;
num=[num,AUCAperBias,AUCBperBias,AUCABperBias];


colorlist = {'r','g','b'};
signlist = {'o','+','s'};

sortPerBiasA=zeros(length(studygrouplist),length(readersizelist)*length(casesizelist)*4);
sortPerBiasB=sortPerBiasA;
sortPerBiasAB=sortPerBiasA;
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

            for l = 1:length(studygrouplist)
                mu_rs_cs_gs_group = mu_rs_cs_group(find(mu_rs_cs_group(:,studygroupcol)==studygrouplist(l)),:);
                hrhc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)>0.01&mu_rs_cs_gs_group(:,cvarcol)==0.3),:);
                hrlc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)>0.01&mu_rs_cs_gs_group(:,cvarcol)==0.1),:);
                lrhc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)<0.01&mu_rs_cs_gs_group(:,cvarcol)==0.3),:);
                lrlc= mu_rs_cs_gs_group(find(mu_rs_cs_gs_group(:,rvarcol)<0.01&mu_rs_cs_gs_group(:,cvarcol)==0.1),:);
                sortPerBiasA(l,count:count+3) = [hrlc(size(num,2)-2),lrlc(size(num,2)-2),hrhc(size(num,2)-2),lrhc(size(num,2)-2)];
                sortPerBiasB(l,count:count+3) = [hrlc(size(num,2)-1),lrlc(size(num,2)-1),hrhc(size(num,2)-1),lrhc(size(num,2)-1)];
                sortPerBiasAB(l,count:count+3) = [hrlc(size(num,2)),lrlc(size(num,2)),hrhc(size(num,2)),lrhc(size(num,2))];
            end
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
     for l = 1:length(studygrouplist)
        plot(1:size(sortPerBiasA,2),sortPerBiasA(l,:),strjoin([colorlist(3),signlist(l)],''),'MarkerSize',8);
     end
     legend('fully crossed','2 split groups','3 split gorups','FontSize',6);
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 max(sortPerBiasA(:))*1.1])
     ax = gca;
     ax.XTick = 0:totalx;
     ax.XTickLabel  = firstticklabel;
     totalx=4*length(readersizelist)*length(casesizelist);
     axis([0 totalx 0 140])
     xt=get(gca,'XTick');
     yl=get(gca,'YLim');
     set(gca,'FontSize',16)
     for k=1:length(readersizelist)*length(casesizelist)
         text(2.5+(k-1)*4,yl(1)-.25*diff(yl),secondticklabel(k,:),'Horizontalalignment','Center','FontSize',16);
         line([4.5+(k-1)*4 4.5+(k-1)*4], [0 140],'LineStyle','- -','color','k');
     end 
     ylabel('Percentage Bias','FontSize',16);

     %title(a,['Mod A mcVar for mu = ',num2str(realAUClist(i))]);
     hold off

end

subplot(3,1,1);
title('Percentage Bias of MC Mean and Numerical VarAUC','FontSize',16);
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
     







