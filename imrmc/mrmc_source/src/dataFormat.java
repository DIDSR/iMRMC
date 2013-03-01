public class dataFormat {

	String fmtHelp;
	String sampleFmt;

	public String getInfo() {
		return fmtHelp;
	}

	public String getSample() {
		return sampleFmt;
	}

	public dataFormat() {
		fmtHelp = "The file format has two parts: the study description at the top followed by a list of the ROC ratings. The\n"
				+ "study description can include any information as free text. It must include three lines corresponding to the\n"
				+ "size of the experiment and then conclude with a line stating �BEGIN DATA�. We demonstrate the formatting of\n"
				+ "these lines in an example. If the study has 9 readers, 55 diseased patients, and 75 nondiseased patients, \n"
				+ "and 2 modalities, then a legitimate study description can be nothing more than the following lines.\n"
				+ "N0: 75\n"
				+ "N1: 55\n"
				+ "NR: 9\n"
				+ "NM: 2\n"
				+ "BEGIN DATA\n"
				+ "... ...\n"
				+ "The list of ROC ratings has a row for each case, reader, and modality index in the study, in any order\n"
				+ "The current version of iMRMC expects fully-crossed data from two modalities: every reader reads every case in both modalities. \n"
				+ "Consequently, there should be NR*(N0+N1)*3 rows. Each row has four fields: the reader id (integer), case id \n"
				+ "(integer), modality index (0=truth, 1=modality 1, 2 = modality 2), and the score corresponding to value of the modality index \n"
				+ "These would be 0 or 1 for a truth modality index (indicating non-diseased or diseased) or a value (integer or float) \n"
				+ "for modality 1 or 2 (modality indexes 1 and 2). High ratings should indicate high likelihood or confidence of disease and low ratings \n"
				+ "should indicate less likelihood or confidence of disease. For example, the first six rows could look like the following:\n\n"
				+ "1             1             0            0\n"
				+ "1             1             1            1\n"
				+ "1             1             2            3\n"
				+ "2             1             0            0\n"
				+ "2             1             1            1.87\n"
				+ "2             1             2            2\n"
				+ "In the example, we see the ROC ratings from three readers. The first line shows reader 1 \n"
				+ "reading case 1, which is non-diseased. The second and third lines show reader 1's scores \n"
				+ "for modality 1 and modality 2 respectively. The fourth line shows reader 2 reading non-diseased\n"
				+ "case 1. The fifth and sixth lines show the ratings from reader 2 for modality 1 and 2 respectively.\n";

		sampleFmt = "\n";

	}
}
