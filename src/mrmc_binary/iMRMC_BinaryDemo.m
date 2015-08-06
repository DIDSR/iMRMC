% demo.m

% r = [r_c1,r_c2,r_r1,r_r2,r_t,r_tc,r_tr] 
disp('arbitrary parameters for testing the programs:');
r = [.007 .007 .25 .25 .50 .005 .20] %arbitrary parameters for testing the program
PC = [.85 .85]
Nr = 10
Nc = 300

%Generate one dataset and analyze it using the OR method
[S1,S2] = iSimuBinaryMRMC(r,PC,Nr,Nc);
ret = iAnalyzeBinaryMRMC_OR(S1,S2); 
disp('the estimated 95% confidence interval for PC(2)-PC(1) is:');
ret.CI95

anaMethod = 'iAnalyzeBinaryMRMC_OR';
nexp = 20000; % number of Monte Carlo trials; this number can be lowered for a speedy demo
%validation of the OR method in terms of the empirical coverage probability
%of the estimated 95% confidence intervals
disp('start Monte Carlo validation:') 
disp('it may take a couple of minutes... (set smaller number of MC trials for a speedy demo)')
disp('..........................');
prob = iValidateBinaryMRMC(anaMethod,Nr, Nc, r, PC, nexp);
disp(['the empirical coverage probability estimated from ',num2str(nexp),' Monte Carlo trials is: ' num2str(prob)]);


%Power calculation for a hypothetical non-inferiority test
nim = .04; % non-inferiority margin
disp('start Monte Carlo power calculation for non-inferiority test:')
disp('it may take a couple of minutes... (set smaller number of MC trials for a speedy demo)')
disp('..........................');
pow = iPowerBinaryMRMC('iAnalyzeBinaryMRMC_OR',Nr, Nc, r, PC, nim, nexp);
disp(['the empirical statistical power estimated from ',num2str(nexp),' Monte Carlo trials is: ' num2str(pow)]);