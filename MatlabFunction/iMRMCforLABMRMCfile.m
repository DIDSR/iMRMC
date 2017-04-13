%iMRMCforLABMRMCfile: run LABMRMC by iMRMC software
%input one file of the LABMRMC format, convert it to an “.imrmc” file, call iMRMC.jar using the MATLAB “system” function, 
%and read the iMRMCoutput into a MATLAB structure. 

%input argument
%iMRMCsoftwarePath(string): full path and name for imrmc software
%LABMRMCfullPath(string): full path and name for LABMRMC file
%deleteTemp(int 0 or 1): flag for whether save temp .imrmc file and analysis .csv results

%output variable
%output(struct): save all .csv analysis results. 
function  output  = iMRMCforLABMRMCfile(iMRMCsoftwarePath,LABMRMCfullPath,deleteTemp )
% set read in format
    delimiter = {'\t',' '};
    formatSpec = '%q%q%q%[^\n\r]';
    % load LABMRMC file
    fileID = fopen(LABMRMCfullPath,'r');
    dataArray = textscan(fileID, formatSpec, 'Delimiter', delimiter, 'ReturnOnError', false);
    fclose(fileID);
    raw = repmat({''},length(dataArray{1}),length(dataArray)-1);
    for col=1:length(dataArray)-1
        raw(1:length(dataArray{col}),col) = dataArray{col};
    end

    % find all L and star sign in input file
    lineLlogical = strcmpi(raw(:,1),'L');
    lineL = find(lineLlogical ==1);
    lineStarlogical = strcmpi(raw(:,1),'*');
    lineStar = find(lineStarlogical ==1);
    datastr = {};  %store reading scores
    readers = [];    %store reader ID
    datastrLine = 1;    %keep trick each line
    mods = [];       %store MOD ID
    N0s=[];          %store N0 ID
    N1s=[];          %store N1 ID


    %read LABMRMC line by line and create datastr which is the iMRMC format for each observation 
    for l =1:length(lineL)
        % find L to seperate reader
        tempModLine = raw(lineL(l),:);
        % find * to seperate normal and disease case
        tempFirstStarLine = lineStar(l*2-1);
        tempSecondStarLine = lineStar(l*2);
        tempReader = raw{lineL(l)-2,1};
        readers = [readers,raw(lineL(l)-2,1)];
        for m = 1:length(tempModLine)-1
            tempMod = raw(lineL(l)-1,m);
            mods = [mods,tempMod];
        end
        % load normal cases
        for datai = lineL(l)+1:tempFirstStarLine-1        
            tempCase = raw{datai,end};
            N0s=[N0s,raw(datai,end)];
            for m = 1:length(tempModLine)-1
                tempMod = raw{lineL(l)-1,m};
                tempScore = raw{datai,m}; 
                newline = {tempReader,tempCase,tempMod,tempScore};
                datastr(datastrLine,:) = newline;
                datastrLine = datastrLine +1;
            end
        end
        % load disease cases
         for datai = tempFirstStarLine+1:tempSecondStarLine-1        
             tempCase = raw{datai,end};
             N1s=[N1s,raw(datai,end)];
             for m = 1:length(tempModLine)-1
                 tempMod = raw{lineL(l)-1,m};
                 tempScore = raw{datai,m}; 
                 newline = {tempReader,tempCase,tempMod,tempScore};
                 datastr(datastrLine,:) = newline;
                 datastrLine = datastrLine +1;
             end
         end
    end
    % use mods N0s N1s to generate truth lines
    mods = unique(mods);
    N0s = unique(N0s);
    N1s = unique(N1s);
    truthStr = {};
    truthStrLine = 1;
    for truthi = 1 : length(N0s)
        newline = {'truth',N0s{truthi},'0','0'};
        truthStr(truthStrLine,:) = newline;
        truthStrLine = truthStrLine+1;
    end
    for truthi = 1 : length(N1s)
        newline = {'truth',N1s{truthi},'0','1'};
        truthStr(truthStrLine,:) = newline;
        truthStrLine = truthStrLine+1;
    end
    % use mods N0s N1s to generate summary lines
    NRline = {['NR: ',num2str(length(readers))],'','',''};
    N0line = {['N0: ',num2str(length(N0s))],'','',''};
    N1line = {['N1: ',num2str(length(N1s))],'','',''};
    NMline = {['NM: ',num2str(length(mods))],'','',''};
    Beginline = {'BEGIN DATA:','','',''};
    % put all lines together and output file
    finalStr = [NRline;N0line;N1line;NMline;Beginline;truthStr;datastr];
    fileID = fopen('tempInputFile.imrmc','w');
    for outi = 1 : size(finalStr,1)
        fprintf(fileID,'%s,',finalStr{outi,1:end-1});
        fprintf(fileID, '%s\n', finalStr{outi,end}) ;
    end
    fclose(fileID);
    % run imrmc software
    system(['java -jar ',iMRMCsoftwarePath,' tempInputFile.imrmc tempDir']);
    
    % collect output data
    tempFolderName = 'tempDir';
    fileList = dir(tempFolderName);
    for i = 3 : length(fileList)
        tempFile = fileList(i);
        tempFileName = tempFile.name;
        tempFileNameOnly = tempFileName(1: strfind(tempFileName,'.csv')-1);
        [num_data text_data raw] = xlsread([tempFolderName,'\',tempFileName]);  
        eval([[tempFolderName,'.',tempFileNameOnly] '= raw;']);
        if deleteTemp
            delete([tempFolderName,'\',tempFileName]); 
        end
    end
    output = tempDir;
    if deleteTemp
        delete('tempInputFile.imrmc'); 
        rmdir('tempDir');
    end
end

