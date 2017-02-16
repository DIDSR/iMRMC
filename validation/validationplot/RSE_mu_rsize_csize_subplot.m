clc
clear all
close all
[num,txt,raw] = xlsread('input3.xlsx','sheet1');
plotcolor = {'r','b','k'};
plotsign = {'o','*','s'};
plotlegend = {};
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));
studygrouplist = unique(num(:,studygroupcol));
mucol = strmatch('uA',txt(1,:));
rvarcol = strmatch('AR0',txt(1,:));
cvarcol = strmatch('AC0',txt(1,:));
AUCAcol = strmatch('varAUC_A',txt(1,:),'exact');
AUCBcol = strmatch('varAUC_B',txt(1,:),'exact');
AUCAminusBcol = strmatch('totalVar',txt(1,:),'exact');
mulist = unique(num(:,mucol));
rvarlist = unique(num(:,rvarcol));
cvarlist = unique(num(:,cvarcol));

readersizecol = strmatch('nr',txt(1,:));
casesizecol = strmatch('n1',txt(1,:));
studygroupcol = strmatch('Num of Split-Plot Groups',txt(1,:));
readersizelist = unique(num(:,readersizecol));
casesizelist = unique(num(:,casesizecol));
studygrouplist = unique(num(:,studygroupcol));

mcvarvarAUCAcol = strmatch('mcVarvarAUC_A',txt(1,:),'exact');
mcvarvarAUCBcol = strmatch('mcVarvarAUC_B',txt(1,:),'exact');
mcvarvarAUCAminusBcol = strmatch('mcVartotalVar',txt(1,:),'exact');

NumvarAUCAcol = strmatch('NumvarAUC_A',txt(1,:),'exact');
NumvarAUCBcol = strmatch('NumvarAUC_B',txt(1,:),'exact');
NumvarAUCAminusBcol = strmatch('NumvarAUC_AB',txt(1,:),'exact');

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
                   REShrhc =  sqrt(hrhc(:,mcvarvarAUCAcol(1)))./hrhc(:,NumvarAUCAcol(1));
                   plot(mulist(i)*ones(1,size(REShrhc,1)),REShrhc',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   hrlc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)>0.01&mu_rsize_csize_group(:,cvarcol)==0.1),:);
                   figure(2)
                   REShrlc =  sqrt(hrlc(:,mcvarvarAUCAcol(1)))./hrlc(:,NumvarAUCAcol(1));
                   plot(mulist(i)*ones(1,size(REShrlc,1)),REShrlc',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   lrhc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)<0.01&mu_rsize_csize_group(:,cvarcol)==0.3),:);
                   figure(3)
                   RESlrhc =  sqrt(lrhc(:,mcvarvarAUCAcol(1)))./lrhc(:,NumvarAUCAcol(1));
                   plot(mulist(i)*ones(1,size(RESlrhc,1)),RESlrhc',strjoin([plotcolor(j),plotsign(k)],''));
                   hold on
                   lrlc= mu_rsize_csize_group(find(mu_rsize_csize_group(:,rvarcol)<0.01&mu_rsize_csize_group(:,cvarcol)==0.1),:);
                   figure(4)
                   RESlrlc =  sqrt(lrlc(:,mcvarvarAUCAcol(1)))./lrlc(:,NumvarAUCAcol(1));
                   plot(mulist(i)*ones(1,size(RESlrlc,1)),RESlrlc',strjoin([plotcolor(j),plotsign(k)],''));
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
ylabel('Mod A Relative SE')
title('hrhc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(2)
xlabel('mu')
ylabel('Mod A Relative SE')
title('hrlc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(3)
xlabel('mu')
ylabel('Mod A Relative SE')
title('lrhc shape for different case size, color for different reader size');
legend(plotlegend(1:j*k));
figure(4)
xlabel('mu')
ylabel('Mod A Relative SE')
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


