clc
clear all
close all
[num,txt,raw] = xlsread('input2.xlsx','sheet1');
plotcolor = {'r','b','k'};
plotsign = {'o','*','s'};
plotlegend = {};
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));
studygrouplist = unique(num(:,studygroupcol));
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
for i = 1: length(mulist)
    mu_group = num(find(num(:,mucol)==mulist(i)),:);
    for j = 1: length(readersizelist)
       mu_rsize_group = mu_group(find(mu_group(:,readersizecol)==readersizelist(j)),:);
       if ~isempty(mu_rsize_group)
           for k = 1: length(casesizelist)
               mu_rsize_csize_group = mu_rsize_group(find(mu_rsize_group(:,casesizecol)==casesizelist(k)),:);
               if ~isempty(mu_rsize_csize_group)

                   %sub plot for study group
%                    for l = 1: length(studygrouplist)
%                        mu_rsize_csize_sg_group = mu_rsize_csize_group(find(mu_rsize_csize_group(:,studygroupcol)== studygrouplist(l)),:);
%                        figure(l)
%                        plot(mulist(i)*ones(1,size(mu_rsize_csize_sg_group,1)),mu_rsize_csize_sg_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
%                        title([num2str(l),' study group, shape for different case size, color for different reader size']);
%                        hold on
%                    end

                   %sub plot for reader var
%                    for l = 1: length(rvarlist)
%                        mu_rsize_csize_rvar_group = mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)== rvarlist(l)),:);
%                        figure(l)
%                        plot(mulist(i)*ones(1,size(mu_rsize_csize_rvar_group,1)),mu_rsize_csize_rvar_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
%                        title(['reader var = ',num2str(rvarlist(l)),' shape for different case size, color for different reader size']);
%                        hold on
%                    end

                   %sub plot for case var
%                    for l = 1: length(cvarlist)
%                        mu_rsize_csize_cvar_group = mu_rsize_csize_group(find(mu_rsize_csize_group(:,cvarcol)== cvarlist(l)),:);
%                        figure(l)
%                        plot(mulist(i)*ones(1,size(mu_rsize_csize_cvar_group,1)),mu_rsize_csize_cvar_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
%                        title(['reader var = ',num2str(cvarlist(l)),' shape for different case size, color for different reader size']);
%                        hold on
%                    end
                   % sub plot for H L var
                   hrhc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)>0.01&mu_rsize_csize_group(:,cvarcol)==0.3),:);
                   figure(1)
                   plot(mulist(i)*ones(1,size(hrhc,1)),hrhc(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   hrlc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)>0.01&mu_rsize_csize_group(:,cvarcol)==0.1),:);
                   figure(2)
                   plot(mulist(i)*ones(1,size(hrlc,1)),hrlc(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   lrhc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)<0.01&mu_rsize_csize_group(:,cvarcol)==0.3),:);
                   figure(3)
                   plot(mulist(i)*ones(1,size(lrhc,1)),lrhc(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   lrlc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)<0.01&mu_rsize_csize_group(:,cvarcol)==0.1),:);
                   figure(4)
                   plot(mulist(i)*ones(1,size(lrlc,1)),lrlc(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
    %                    figure(1)
    %                    plot(mulist(i)*ones(1,size(mu_rsize_csize_group,1)),mu_rsize_csize_group(:,varAUCAcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
    %                    hold on
    %                    figure(2)
    %                    plot(mulist(i)*ones(1,size(mu_rsize_csize_group,1)),mu_rsize_csize_group(:,varAUCBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
    %                    hold on
    %                    figure(3)
    %                    plot(mulist(i)*ones(1,size(mu_rsize_csize_group,1)),mu_rsize_csize_group(:,varAUCAminusBcol(1))',strjoin([plotcolor(j),plotsign(k)],''));
    %                    hold on
                       
                   plotlegend = [plotlegend,['rsize',num2str(readersizelist(j)),'csize',num2str(casesizelist(k))] ];
               end
               a=a+1;
           end
       end
    end
end
figure(1)
xlabel('mu')
ylabel('mcVarAUC_A')
title('hrhc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(2)
xlabel('mu')
ylabel('mcVarAUC_A')
title('hrlc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(3)
xlabel('mu')
ylabel('mcVarAUC_A')
title('lrhc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(4)
xlabel('mu')
ylabel('mcVarAUC_A')
title('lrlc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));



% figure(1)
% xlabel('mu')
% ylabel('varAUC_A')
% % title('shape for different case size, color for different reader size');
% legend(plotlegend);
% figure(2)
% xlabel('mu')
% ylabel('varAUC_A')
% % title('shape for different case size, color for different reader size');
% legend(plotlegend);
% figure(3)
% xlabel('mu')
% ylabel('varAUC_A')
% % title('shape for different case size, color for different reader size');
% legend(plotlegend);


