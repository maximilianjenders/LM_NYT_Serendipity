package CoreCalculator.Parallel;

import CoreCalculator.AdvancedCoreCalculator;
import dataclass.ArticleFeatureMaintainer;

public class AdvancedCalculatorThread extends Thread {
	private int initialDocumentID;
	private int numRecommendations;
	private ArticleFeatureMaintainer fm;
	
	private boolean printInitialDocumentData = false;
	private boolean printEntropyData = false;
	private boolean printCoreArticleTitles= false;
	private boolean printProbabilityData = false;
	private String dataSlice = null;
	
	public AdvancedCalculatorThread(int initialDocumentID, int numRecommendations, ArticleFeatureMaintainer fm) {
		this.initialDocumentID = initialDocumentID;
		this.numRecommendations = numRecommendations;
		this.fm = fm;
	}
	
	public void run() {
		AdvancedCoreCalculator c = new AdvancedCoreCalculator(fm);
		c.setPrintInitialDocumentData(printEntropyData);
		c.setPrintEntropyData(printEntropyData);
		c.setPrintCoreArticleTitles(printCoreArticleTitles);
		c.setPrintProbabilityData(printProbabilityData);
		if (dataSlice != null) c.setDataSlice(dataSlice);
//		c.restoreFromDB(initialDocumentID);
//		c.findSerendipitousDocs(numRecommendations);
		c.run(initialDocumentID, numRecommendations);
	}

	public void setPrintInitialDocumentData(boolean b) {
		printInitialDocumentData = b;
	}
	
	public void setDataSlice(String slice) {
		this.dataSlice = slice;
	}

	public void setPrintEntropyData(boolean b) {
		printEntropyData = b;
	}

	public void setPrintCoreArticleTitles(boolean b) {
		printCoreArticleTitles = b;
	}
	
	public void setPrintProbabilityData(boolean b) {
		printProbabilityData = b;
	}
}
