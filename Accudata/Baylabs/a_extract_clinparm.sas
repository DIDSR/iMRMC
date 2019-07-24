* a_extract_clinparm;
* Bay Labs;
* 10 June 201 ;
* Douglas A. Milikien;;
* Read the spreadsheet and convert it to the MRMC binomial layout;
* fake data for Steve to check;
;


%LET path=C:\Documents and Settings\milikiend\Desktop\Bay Labs\EchoGPS\Raw Data\20190416;
%let pgm=a_extract_clinparm;


filename lst "&path.\&pgm..lst";
filename log "&path.\&pgm..log";

title1;

libname _clear_ ;

libname outdata "&path." ;

proc datasets lib=work memtype=data kill nolist;
quit ;

libname lib " " ;

data TEMP ;
   infile "&path.\STUDY_ECHO_CLINPARM.csv" delimiter = ',' MISSOVER DSD lrecl=32767 firstobs=2 ;

   informat  RECORD_ID     best32. ;
   informat  STUDY_NURSEID best32. ;
   informat  CLINPARM      $12. ;
   informat  CARDIO1       $100. ;
   informat  CARDIO2       $100. ;
   informat  CARDIO3       $100. ;
   informat  CARDIO4       $100. ;
   informat  CARDIO5       $100. ;

   format  RECORD_ID      best12. ;
   format  STUDY_NURSEID  best12. ;
   format  CLINPARM      $12. ;
   format  CARDIO1       $100. ;
   format  CARDIO2       $100. ;
   format  CARDIO3       $100. ;
   format  CARDIO4       $100. ;
   format  CARDIO5       $100. ;



   input 
		RECORD_ID
		STUDY_NURSEID
		CLINPARM
		CARDIO1
		CARDIO2
		CARDIO3
		CARDIO4
		CARDIO5
   
;
   if _ERROR_ then call symput('_EFIERR_',1);
run;

proc print;
run;
* Find the values with Image Quality Inadequate;
*See what would be a good test case ;

data trap;
	set temp;
	if TRIM(cardio1)="Image quality inadequate for visual assessment" 
		or
		TRIM(cardio2)="Image quality inadequate for visual assessment" 
		or
		TRIM(cardio3)="Image quality inadequate for visual assessment" 
		or
		TRIM(cardio4)="Image quality inadequate for visual assessment" 
		or
		TRIM(cardio5)="Image quality inadequate for visual assessment" 
		
;
run;
proc freq data=temp;
	tables clinparm ;
run;
* Create a series of Yes/No values representing Adequate/Inadequate quality ;
data clinparm(drop=i);
	set temp;
	array c{5} c1-c5;
	array reader{5} cardio1-cardio5 ;

	do i= 1 to 5 ;
			if TRIM(reader{i})="Image quality inadequate for visual assessment"
				then c{i}=0 ;
			else if TRIM(reader{i}) ^= "" 
				then c{i}=1 ;
			else
					c{i}= . ;
	end ;

	* Now summarize across the 5 Adequate/Inadequate ratings ;

	PCTADEQUATE= 100 * SUM(of c1-c5)/N(of c1-c5) ;
	FORMAT PCTADEQUATE 6.1;

	IF PCTADEQUATE > 49.999999999 THEN quality=1;
	else if PCTADEQUATE > .Z      THEN quality=0;

  run;
proc print data=clinparm;
	where record_id=2027;
run;

proc sort data=clinparm;
	by clinparm record_id ;
run;

data outdata.fake20190405;
	set clinparm(keep= record_id study_nurseid clinparm quality);
run;
proc print data=outdata.fake20190405;
run;
* First do it for LV and LAS clinparm;
data outdata.LV_20190405 
	 outdata.GLVF_20190405 ;
	set outdata.fake20190405 ;
	if      upcase(clinparm)="LV"  then output outdata.LV_20190405 ;
	else if upcase(clinparm)="GLVF" then output outdata.GLVF_20190405 ; 
RUN;

proc print data=outdata.LV_20190405;
run;

* Now transform it to the structure that MRMC app in Java is expecting ;
* per the Github instructions ;

data lv(DROP= RECORD_ID STUDY_NURSEID QUALITY);
	length ReaderID CaseID $ 12 modalityID $ 12 ;

	SET outdata.LV_20190405;

	readerid = "Reader"||LEFT( put(STUDY_NURSEID, z4.)) ;
	caseid =   "Actual"|| LEFT(put(RECORD_ID, 4. )    );
	modalityID = "modalityA";

	IF      QUALITY=1 THEN SCORE = 1 ;
	ELSE IF QUALITY=0 THEN SCORE = 0 ;
RUN;
PROC PRINT DATA=LV;
RUN;

* Create a "truth" companion record for each unique case, but  ;
* this time the recordID will be truth, the modalityID will be truth;
* and the score will be 1 ;

data LV2;
	set LV;
	output ;

	ReaderID="truth";
	modalityID="truth";
	score=1 ;
	output ;
run;
PROC PRINT DATA=LV2;
RUN;

* Create a "fake" companion record for each truth record you just created, 
* but change the CaseID to Fake1, Fake2, etc. and score to 0; 


data truthcase;
	set LV2(drop= score); 
	if  ReaderID="truth"; 

	numonly= substr(CaseID,7,4);
	CaseID="Fake"||left(numonly);
	score = 0 ;
run;
proc print data=truthcase; 
run;
* Insert those fake companion records into the dataset ;

data LV3;
	set LV2 
		truthcase 
;
RUN;

* Create a "fake" companion for each of your original ReaderID/CaseID results, but 
* this time switch the CaseID from Actual1, Actual2 to Fake1, Fake2, etc and assign a score 
* of 0.5 ; 
* Leave the ReaderID as is; 

data fake2(drop= numonly);
	set lv(drop=score);

	numonly= substr(CaseID,7,4);
	CaseID="Fake"||left(numonly);
	score = 0.5 ;
run;
proc print data=fake2;
run;

* Finally, Insert these fake records into the data stream ;


data lv4;
	set lv3
		fake2
		;
run;
proc print data=lv4;
run;


data _null_ ;
	set lv4 ;
	file "&path.\output_LV_20190405.csv" ;

	if _n_ = 1 then do;
		put "ReaderID" "," "CaseID" "," "ModalityID" "," "Score";
	end;

	put  ReaderID "," CaseID "," ModalityID "," Score ;
run;

data _null_ ;
	set lv ;
	file "&path.\input_LV_20190405.csv" ;
	
	if _n_ = 1 then do;
		put "Clinparm" "," "ReaderID" "," "CaseID" "," "ModalityID" "," "Score";
	end;

		put Clinparm "," ReaderID  "," CaseID "," ModalityID "," Score ;
run;
