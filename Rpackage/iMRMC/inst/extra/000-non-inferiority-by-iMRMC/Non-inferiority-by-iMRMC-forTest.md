How to perform a non-inferiority analysis with the iMRMC software
================

# Issue recap

In response to a question posed in the “How do I do a non-inferiority
study?” issue on the package’s Github repository
(<https://github.com/DIDSR/iMRMC/issues/31#issuecomment-1368940679>), we
have developed an example guide using the R package that demonstrates
using the iMRMC software to help future users. This wiki page is based
on an R markdown file that contains the R code for reproducing the
results: [LINK to R markdown
file](https://github.com/DIDSR/iMRMC/blob/master/Rpackage/iMRMC/inst/extra/000-non-inferiority-by-iMRMC/Non-inferiority-by-iMRMC.rmd).

The issue asked how to find the p-value of a noninferiority study
comparing split-plot radiologist reads against an AI reading all cases.
The work of Chen, Petrick, and Sahiner (2012) addresses this issue, but
the method they present uses the OR model (Obuchowski and Rockette
(1995)) and assumes the data are fully crossed (all readers read all
cases). The iMRMC software can produce the OR variance components only
when the data is fully crossed, so the issue is asking for a solution
that makes use of the iMRMC software for alternate study designs, and
the split-plot study in particular (Obuchowski, Gallas, and Hillis
(2012)).

The measure of diagnostic accuracy used is AUC, where the experimental
modality is a single AI algorithm and the conventional modality is the
radiologists’ reads. The margin parameter is set to a level of
$\delta = 0.05$. Further, the significance level is $\alpha = 0.05$.

# Viper Example - Set up

To answer this question, we will use the ViperObservation data from the
ViperData package (<https://github.com/DIDSR/viperData>). This data
assesses radiologists’ diagnostic performance using screen film
mammography (SFM) and full field digital mammography (FFDM) under
varying study designs, which is outlined by Gallas et al. (2019).
Similar to the issue posed, the diagnostic accuracy of each modality is
measured by AUC from a split-plot study. In this manuscript, the
experimental modality being tested is AI simulated from the FFDM data,
compared to the conventional modality of SFM. We will use a margin
parameter of $\delta = 0.05$ and a significance level of
$\alpha = 0.05$. To address the question of comparing an AI reading all
cases to radiologists’ split-plot reads, we will simulate an AI modality
based on FFDM data and assess its scores against the SFM scores.

# Viper Example - Hypotheses

For noninferiority testing, the null and alternative hypotheses have
been described in Chen, Petrick, and Sahiner (2012) and are copied here:

$$H_0: \theta_e - \theta_c = -\delta \text{ or } H_0: \theta_e - \theta_c + \delta = 0$$

$$H_1: \theta_e - \theta_c > -\delta \text{ or } H_1: \theta_e - \theta_c + \delta > 0$$

where $\theta_e$ is the diagnostic accuracy of experimental modality, or
the AUC of AI in the current example. Similarly, $\theta_c$, or the
diagnostic accuracy of the conventional modality, represents the AUC of
SFM in the current example. Chen, Petrick, and Sahiner (2012) describe
this set of hypotheses as testing that “the performance of the new
modality is no worse than $\delta$ below that of the conventional
modality”.

Therefore, if we can reject $H_0$, then we have shown that that the
experimental modality (ie. AI) is not inferior to the conventional
modality (ie. SFM). If we fail to reject $H_0$, then the experimental
modality may be inferior to the conventional modality.

# Viper Example - Load Data

To use the viperData package, we can download the package in its
entirety here: <https://didsr.github.io/viperData/>, as a tar.gz or zip
file and install the package from the computer’s local library. This is
possible using the Tools dropdown, selecting “Install packages…”, and
changing the “Install From:” option to “Package Archive File”. From here
we can browse and select the downloaded viperData package to install and
load into our Environment.

``` r
#Load iMRMC package
library(iMRMC)

#Load Viper data 
library(viperData)
viperObs <- viperData::viperObs


#Choose one sub-study => screeningHighP
screeningHighP <- viperObs[viperObs$subStudyLabel == "screeningHighP", , drop = FALSE]
screeningHighP$caseID <- as.character(screeningHighP$caseID)
```

# Viper Example - Simulate AI

The iMRMC function that will perform the analysis expects the input
dataframe to consist of 4 columns: `readerID`, `caseID`, `modalityID`,
and `score`. The `truth` modality in the resulting iMRMC dataframe
represents the “ground truth” on the state of any case as diseased
($N_1$) or non-diseased ($N_0$). More information regarding these
variables are available in the package documentation here:
<http://didsr.github.io/iMRMC/000_iMRMC/userManualHTML/index.htm>. To
shape the original data into this form, use the `createIMRMCdf()`
function specifying the variable columns mentioned above.

We begin preparing the data for analysis by separating the FFDM data
from the SFM data. We will use the FFDM data to simulate an AI reader.

``` r
#Create iMRMC dataframe
dfMRMC <- createIMRMCdf(screeningHighP, 
                         keyColumns = list(readerID = "readerID",
                                           caseID = "caseID",
                                           modalityID = "modalityID",
                                           score = "score",
                                           truth = "cancerStatus455"),
                         truePositiveFactor = "1")

#Subset FFDM reads from all readers
dfMRMC_onlyFFDM <- subset(dfMRMC, modalityID == "FFDM")

#Subset SFM reads from all readers
dfMRMC_onlySFM <- subset(dfMRMC, modalityID == "SFM")

#Subset truth reads from all readers
dfMRMC_onlyTruth <- subset(dfMRMC, modalityID == "truth")
```

To simulate data from an AI reading all cases, both as modality and
reader, we will combine data from multiple readers. We do this by
cycling though all readers in the `dfMRMC_onlyFFDM` dataframe and adding
the first instance of each case to the new dataframe `dfMRMC_AImod`.
Once we have just one score for each case under this modality, the
`modalityID` is changed to “AI” and the `readerID` for these scores is
renamed to “AI.1”. Now we have simulated the score results of AI reads
for all cases.

``` r
#Create a dataframe for AI simulated reads from FFDM modality
dfMRMC_AImod <- data.frame()

#Loop through all cases in caseID and isolate each case from the chosen readers just once
for(i in unique(dfMRMC_onlyFFDM$readerID)) { 
  remainingCases <- dfMRMC_onlyFFDM[!dfMRMC_onlyFFDM$caseID %in% 
                                      dfMRMC_AImod$caseID, , drop = FALSE]
  dfMRMC_AImod <- rbind(dfMRMC_AImod, subset(remainingCases, readerID == i))
}

#Change modalityID to "AI" for all cases
dfMRMC_AImod$modalityID <- "AI"

#Change readerID to "AI.1" for all cases
dfMRMC_AImod$readerID <- "AI.1"
```

For the purpose of the software, which expects multiple reader data, we
need to duplicate those results for a second reader that we will call
“AI.2”. This process and reasoning is covered on the iMRMC repository
wiki here: <https://github.com/DIDSR/iMRMC/wiki/Single-reader-analysis>.
By duplicating these scores on the same cases, there is no reader
variability for the AI “readers”.

``` r
#Duplicate those cases and rename with readerID "AI.2" 
dfMRMC_AImod2 <- dfMRMC_AImod
dfMRMC_AImod2$readerID <- "AI.2"

#Bind dataframe with 2 AI "readers" with duplicate scores on all cases
dfMRMC_AImod <- rbind(dfMRMC_AImod, dfMRMC_AImod2)

## Bind simulated AI reads with split-plot SFM reads 

#Bind full dataframe with only SFM, truth and AI rows 
dfMRMC_AI <- rbind(dfMRMC_onlyTruth, dfMRMC_onlySFM, dfMRMC_AImod)
```

# Viper Example - Run iMRMC

Once the dataframe is shaped into iMRMC’s expected format, we run the
`doIMRMC` function which provides 5 results. These include:

1.  `perReader`: per-reader AUC results

2.  `Ustat`: reader-averaged AUC results using U-statistics

3.  `MLEstat`: reader-averaged AUC results using maximum likelihood
    estimation

4.  `ROC`: modality-specific ROC curves, and

5.  `varDecomp`: 5 variance decompositions for the reader-averaged AUCs.

``` r
#Run iMRMC analysis function
results <- doIMRMC(dfMRMC_AI)
```

# Viper Example - P-value calculation

As Chen, Petrick, and Sahiner (2012) describe, the p-value for a
noninferiority test is $$P = 2(1-F(t;{df}_0|H_0)),$$ where
$F(t; {df}_0|H_0)$ is the cumulative distribution function of the test
statistic under the null hypothesis. The test statistic is
$$t = \frac{\hat\theta_e - \hat\theta_c + \delta}{\hat{sd}},$$

where $\hat{sd}$ is the standard deviation of the difference between the
diagnostic accuracy of each modality and the hats indicate estimates of
population parameters. The accuracies of each modality
($\theta_e \text{ and } \theta_c$ or `modA` and `modB`, respectively)
are reader-averaged. The iMRMC function provides these reader-averaged
estimates in the object `uStat`. Here we show how to calculate the test
statistic, and we present the results for the simulation in *Table 1*.

``` r
#Isolate data frame of reader-averaged U-stats based performance results
uStat <- results[['Ustat']] 

#Capture degrees of freedom
#The third row we are calling here assesses the accuracy of modA against modB
dfBDG <- uStat[3, 'dfBDG']

#Capture variance of the difference between modalities, ie. the total variance
varAUCAminusAUCB <- uStat[3, 'varAUCAminusAUCB']

#Define all variables used in the t-statistic calculation 
marginParam = 0.05
auc.SFM = uStat[3, 'AUCA'] 
auc.AI = uStat[3, 'AUCB'] 
se = sqrt(varAUCAminusAUCB)
#Calculate t statistic
tStat <- (auc.AI - auc.SFM + marginParam) / se

#Calculate p-value from tStat 
pValue <- 2*(1 - pt(tStat, df = dfBDG))
```

| auc.AI | auc.SFM | marginParameter | stdErr | tStat  | pValue | alpha  |
|:------:|:-------:|:---------------:|:------:|:------:|:------:|:------:|
| 0.7500 | 0.7379  |     0.0500      | 0.0296 | 2.0956 | 0.0380 | 0.0500 |

*Table 1: Non-inferiority statistical analysis results.*

The p value of 0.038 is less than our significance level of 0.05,
indicating that we reject $H_0$ and the simulated AI is not inferior to
SFM.

# Viper Example - BDG variance decomposition

While the total variance of the difference is provided by the doIMRMC
function in the `Ustat` dataframe as `varAUCAminusAUCB`, we will also
use the `varDecomp` results from the same function to find this variance
for the BDG variance components separately to demonstrate the
relationship. Understanding this relationship between coefficients and
components allows us to explore different study designs.

In the BDG variance decomposition, the total variance of the difference
between modalities is split into 8 second-order U-statistic moments and
their respective coefficients. The total variance is then the linear
combinations of these coefficients and component terms (Gallas 2006):

<!-- Comment: When output is a pdf document, delete the $$ at the beginning and end. -->

$$
\begin{equation*}
\begin{split}
\hat V & =
  \frac{1}{R}
  \left(
      c_1 \hat{M_1} + c_2 \hat{M_2} + c_3 \hat{M_3} + c_4 \hat{M_4}
  \right)
  \\ & +
  \frac{R-1}{R}
  \left(
      c_5 \hat{M_5} + c_6 \hat{M_6} + c_7 \hat{M_7} + c_8 \hat{M_8}
  \right) - \hat{M_8}.
\end{split}
\end{equation*}
$$

Further reading of that paper will provide a more detailed description
of how the coefficients are calculated based on number of readers,
non-diseased cases, and diseased cases when the data is fully crossed
(every reader reads every case in all modalities). If the study design
is not fully crossed, as it is in the split-plot Viper study, the
coefficients are more complicated and can be calculated with the iMRMC
software (Chen, Gong, and Gallas 2018). Gallas and Brown (2008) offer
explanations on these non-fully-crossed coefficients.

Here we compose two data frames of the BDG coefficients
`BDG_AI.SFM_coeff` and components `BDG_AI.SFM_comp` from the iMRMC
output.

``` r
#List of methods of total variance decompositions
listVarDecomp <- results[['varDecomp']] 

#Isolate BDG variance decomposition coefficients and component data frames from list
# BDG_AI.SFM_coeff <- listVarDecomp[["BDG"]]$Ustat$coeff$AI.SFM
# BDG_AI.SFM_comp <- listVarDecomp[["BDG"]]$Ustat$comp$AI.SFM
BDG_AI.SFM_coeff <- listVarDecomp[["BDG"]]$Ustat$coeff
BDG_AI.SFM_comp <- listVarDecomp[["BDG"]]$Ustat$comp
```

These data frames (printed below as *Table 2* and *Table 3*) each
provide 3 rows. The first two rows correspond to the single-modality
variance of each modality and the covariance of the difference between
the two modalities. Values in these tables have been rounded for
conciseness.

Briefly, the coefficients are scale factors determined by the number of
readers, non-diseased, and diseased cases within the data-collection
study design. Since there is no reader overlap across modalities, the
coefficients and moments corresponding to the same reader are
identically 0. This phenomena is further explained in Gallas and Brown
(2008).

| modalityA | modalityB | $c_1$ | $c_2$ | $c_3$ | $c_4$ | $c_5$ | $c_6$ | $c_7$ | $(1-c_8)$ |
|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| SFM | SFM | 5.66e-05 | 1.78e-03 | 1.49e-03 | 4.67e-02 | 2.27e-04 | 7.13e-03 | 5.96e-03 | -6.34e-02 |
| AI | AI | 3.53e-05 | 4.55e-03 | 3.81e-03 | 4.92e-01 | 3.53e-05 | 4.55e-03 | 3.81e-03 | -5.08e-01 |
| SFM | AI | 0 | 0 | 0 | 0 | 7.06e-05 | 9.10e-03 | 7.62e-03 | -1.68e-02 |

*Table 2: Coefficients of the moments based on numbers of reader and
cases.*

The components are the U-statistic moment estimates. $M_1 - M_7$ are the
success moments, or “naturally-occurring second moments” of the score
for the random effects of readers, non-diseased cases, and diseased
cases as given in **Table 1** of Gallas et al. (2009). $M_8$ is the mean
squared, which will be subtracted from the success moments to calculate
the covariances. Notice that in the AI modality
$M_5 \text { through }M_8$ are identical to $M_1 \text { through } M_4$
because the readers are perfectly duplicated.

| modalityA | modalityB | $M_1$ | $M_2$ | $M_3$ | $M_4$ | $M_5$ | $M_6$ | $M_7$ | $M_8$ |
|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|
| SFM | SFM | 7.34e-01 | 6.15e-01 | 5.92e-01 | 5.43e-01 | 5.98e-01 | 5.77e-01 | 5.57e-01 | 5.44e-01 |
| AI | AI | 7.49e-01 | 6.48e-01 | 5.95e-01 | 5.61e-01 | 7.49e-01 | 6.48e-01 | 5.95e-01 | 5.61e-01 |
| SFM | AI | 0 | 0 | 0 | 0 | 5.91e-01 | 5.83e-01 | 5.62e-01 | 5.53e-01 |

*Table 3: Components of second-order U-statistic moment estimates.*

The code below will execute the linear combination and summation to
arrive at the total variance of the difference.

``` r
#Create a new data frame of linear combination of coefficient and component moments
contributionsToVariance <- BDG_AI.SFM_coeff[, 3:10] * BDG_AI.SFM_comp[, 3:10]
rownames(contributionsToVariance) <- paste(
  "cov(", BDG_AI.SFM_coeff$modalityID.1, ", ", BDG_AI.SFM_coeff$modalityID.2, ")", sep = ""
)

#Sum moments to get the total Var contributions for each modality and interaction
covariances <- as.data.frame(rowSums(contributionsToVariance))

#Sum variance of each modality and subtract the 2*covariance term
totalVarianceofDiff <- as.numeric(covariances[1,1] + covariances[2, 1] - 2 * covariances[3,1])
```

|                       |          |
|:----------------------|:--------:|
| var(AUC_AI)           | 5.06e-04 |
| var(AUC_SFM)          | 1.06e-03 |
| cov(AUC_AI, AUC_SFM)  | 3.43e-04 |
| var(AUC_AI - AUC_SFM) | 8.78e-04 |

*Table 4: Total variance for each modality and interaction.*

The standard error of the difference is 0.0296, which is the same as
given in *Table 1*.

# Authors

$\text{Gardecki, Emma}^1 \text{ and } \text{Gallas, Brandon D.}^1$

$^1$ FDA/CDRH/OSEL/Division of Imaging, Diagnostics, and Software
Reliability, Silver Spring, Maryland, United States

# References

<div id="refs" class="references csl-bib-body hanging-indent"
entry-spacing="0">

<div id="ref-chen2018" class="csl-entry">

Chen, Weijie, Qi Gong, and Brandon D. Gallas. 2018. “Paired Split-Plot
Designs of Multireader Multicase Studies.” *Journal of Medical Imaging*
5: 031410. <https://doi.org/10.1117/1.JMI.5.3.031410>.

</div>

<div id="ref-chen2012" class="csl-entry">

Chen, Weijie, Nicholas A. Petrick, and Berkman Sahiner. 2012.
“Hypothesis Testing in Noninferiority and Equivalence MRMC ROC Studies.”
*Academic Radiology* 19: 1158–65.
<https://doi.org/10.1016/j.acra.2012.04.011>.

</div>

<div id="ref-gallas2006" class="csl-entry">

Gallas, Brandon D. 2006. “One-Shot Estimate of MRMC Variance: AUC.”
*Academic Radiology* 13: 353–62.
<https://doi.org/10.1016/j.acra.2005.11.030>.

</div>

<div id="ref-iMRMCgithub" class="csl-entry">

Gallas, Brandon D. 2017. “<span class="nocase">IMRMC-R v1.2.4:
Application for Analyzing and Sizing MRMC Reader Studies.</span>”
<https://cran.r-project.org/web/packages/iMRMC/index.html>.

</div>

<div id="ref-gallas2009" class="csl-entry">

Gallas, Brandon D., Andriy Bandos, Frank W. Samuelson, and Robert F.
Wagner. 2009. “A Framework for Random-Effects ROC Analysis: Biases with
the Bootstrap and Other Variance Estimators.” *Communications in
Statistics—Theory and Methods* 38: 2586–2603.
<https://doi.org/10.1080/03610920802610084>.

</div>

<div id="ref-gallas2008" class="csl-entry">

Gallas, Brandon D., and David G. Brown. 2008. “Reader Studies for
Validation of CAD Systems.” *Neural Networks Special Conference Issue*
21: 387–97. <https://doi.org/10.1016/j.neunet.2007.12.013>.

</div>

<div id="ref-gallas2019" class="csl-entry">

Gallas, Brandon D., Weijie Chen, Elodia Cole, Robert Ochs, Nicholas A.
Petrick, Etta D. Pisano, Berkman Sahiner, Frank W. Samuelson, and Kyle
J. Myers. 2019. “Impact of Prevalence and Case Distribution in Lab-Based
Diagnostic Imaging Studies.” *Journal of Medical Imaging* 6: 015501.
<https://doi.org/10.1117/1.JMI.6.1.015501>.

</div>

<div id="ref-obuchowski2012" class="csl-entry">

Obuchowski, Nancy A., Brandon D. Gallas, and Stephen L. Hillis. 2012.
“Multi-Reader ROC Studies with Split-Plot Designs: A Comparison of
Statistical Methods.” *Academic Radiology* 19 (12): 1508–17.
<https://doi.org/10.1016/j.acra.2012.09.012>.

</div>

<div id="ref-obuchowski1995" class="csl-entry">

Obuchowski, Nancy A., and H. E. Rockette. 1995. “Hypothesis Testing of
Diagnostic Accuracy for Multiple Readers and Multiple Tests: An ANOVA
Approach with Dependent Observations.” *Commun Stat B-Simul* 24 (2):
285–308. <https://doi.org/10.1080/03610919508813243>.

</div>

</div>
