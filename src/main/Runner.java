package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import util.DBConnection;
import util.Helper;
import util.Stemmer;


import CoreCalculator.AdvancedCoreCalculator;
import CoreCalculator.CoreSupporter;
import CoreCalculator.SimpleCoreCalculator;
import CoreCalculator.Parallel.AdvancedCalculatorThread;

import com.nytlabs.corpus.NYTCorpusDocument;
import com.nytlabs.corpus.NYTCorpusDocumentParser;

import dataclass.Article;
import dataclass.ArticleFeatureMaintainer;
import dataclass.EntityMaintainer;
import dataclass.MI;


/***
 * Main class for starting
 * @author Max
 *
 */
public class Runner {
	
	int counter = 0;
	
	/***
	 * Walks through all files, and imports all NYT articles
	 * @param path
	 */
	public void walk(String path) {
		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null) return;

		for ( final File file : list ) {
			if ( file.isDirectory() ) {
				Helper.print("Traversing folder: " + file.getAbsolutePath() + ". Files so far: " + counter);
				walk( file.getAbsolutePath() );
			}
			else {
				try {
					counter++;
					NYTCorpusDocumentParser p = new NYTCorpusDocumentParser();
					NYTCorpusDocument d = p.parseNYTCorpusDocumentFromFile(file, false);
					DBConnection.getInstance().insertArticle(d);
				} catch (Exception e) {
					Helper.printErr("ERROR FOR FILE: " + file.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
	}
	
	/***
	 * Stemming
	 */
	public void getStems() {

		for (int i = 1; i <= 24; i++) {
			new Thread(new Stemmer()).start();
		}
	}
	
	/***
	 * Starts the serendipity calculation with the advanced mode (2 LMs)
	 * @param fm
	 * @param id
	 * @param numRecommendations
	 * @param sliceName
	 */
	public void runAdvancedSerendip(ArticleFeatureMaintainer fm, int id, int numRecommendations, String sliceName) {
//		AdvancedCoreCalculator c = new AdvancedCoreCalculator(fm);
		AdvancedCalculatorThread c = new AdvancedCalculatorThread(id, numRecommendations, fm);
		c.setPrintInitialDocumentData(true);
		c.setPrintEntropyData(false);
		c.setPrintCoreArticleTitles(true);
		c.setPrintEntropyData(false);
		c.setDataSlice(sliceName);
//		c.run(id, numRecommendations);
		c.run();
	}

	/***
	 * Starts the serendipity calculation with simple mode (1 LM)
	 * @param fm
	 * @param id
	 * @param numRecommendations
	 * @param sliceName
	 */
	public void runSimpleSerendip(ArticleFeatureMaintainer fm, int id, int numRecommendations, String sliceName) {
		SimpleCoreCalculator c = new SimpleCoreCalculator(fm);
		c.setPrintInitialDocumentData(true);
		c.setPrintEntropyData(false);
		c.setPrintCoreArticleTitles(true);
		c.setPrintEntropyData(false);
		c.setDataSlice(sliceName);
		c.run(id, numRecommendations);
		
	}
	
	
	public void runAdvancedSerendip(String dataFileName) {
		ArticleFeatureMaintainer fm = DBConnection.getInstance().getArticleStems(dataFileName);
		Runner r = new Runner();
		int id;
		int[] ids = {931599, 545384, 264622, 829455, 874982, 1723870};
		for (int i : ids) {
			Helper.print("");
			r.runAdvancedSerendip(fm, i, 10, dataFileName);
		}
	}
	
	public void runSimpleSerendip(String dataFileName) {
		ArticleFeatureMaintainer fm = DBConnection.getInstance().getArticleStems(dataFileName);
		Runner r = new Runner();
		int id;
		int[] ids = {931599, 545384, 264622, 829455, 874982, 1723870};
		for (int i : ids) {
			Helper.print("");
			r.runSimpleSerendip(fm, i, 10, dataFileName);
		}
	}
		
		public void testNonIntersection(String dataFileName) {
//			Sample IDs:
//			 545384 : "The Ghosts Are Glorious Or Otherwise" - "Sports Desk"
//			 874982 : "Beware The Hedgehogs" - "Editorial Desk"
//			 829455 : "Past Recalled for Japanese-Americans"  - "National Desk"
//			 264622 : "The Great Domain Name Hunt"	- "Circuits"
//			 931599 : "Deported Killer Returns to U.S., Kills Brother Of First Victim" - "Metropolitan Desk"
			// 1723870 : "Just Money" - "Magazine Desk"
			
			ArticleFeatureMaintainer fm = DBConnection.getInstance().getArticleStems(dataFileName);
			int[] ids = {931599, 545384, 264622, 829455, 874982, 1723870};
			for (int i : ids) {
				Helper.print("");
				SimpleCoreCalculator c = new SimpleCoreCalculator(fm);
				c.addInitialDocument(i);
				HashSet<Article> nonIntersecting = c.findNonIntersectingCandidates(fm.getArticle(i));
				Helper.print("ID " + i + " has a total of " + nonIntersecting.size() + " articles with an empty intersection.");
			}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Runner r = new Runner();
//		r.runAdvancedSerendip("data19982003_serial.csv");
//		r.runSimpleSerendip("data.csv");
//		r.printWordsForK();
//		DBConnection db = DBConnection.getInstance();
//		db.load();
//		db.loadFromFile();
	
//		DBConnection.getInstance().writeDataToFile();
		
		
//		DBConnection.getInstance().printArticle(931599);
////	
//		ArticleFeatureMaintainer fm = new ArticleFeatureMaintainer(ArticleFeatureMaintainer.STEMFEATURE);
//		
//		DBConnection.getInstance().getArticleStems(fm, 931599);
//		DBConnection.getInstance().getArticleStems(fm, 1313656);

//		ArticleFeatureMaintainer fm = DBConnection.getInstance().getArticleStems("data19982003.csv");
//////		
//		int[] ids = {931599, 545384, 264622, 829455, 874982, 1723870};
//		for (int i : ids) {
//			SimpleCoreCalculator c = new SimpleCoreCalculator(fm);
//			c.setPrintCoreArticleTitles(true);
//			c.setPrintProbabilityData(true);
//			c.setPrintInitialDocumentData(true);
//			c.addInitialDocument(i);
//			c.findInitialDocRecommendationPosition();
//
//		}
//		
//		
//		
//		EntityMaintainer em = DBConnection.getInstance().getEntityData("entities.csv");
//		em.calculateMutualInformationSingleThreaded();

//		DBConnection.getInstance().runMM("misRanked.csv", "misMM.txt");
		DBConnection.getInstance().rewriteEntityDataBucketed("misSerial.csv", "misBucketed.csv");
		
		
		
		
		
		
		Helper.print("Finished");
		System.exit(0);
//		
		
		//DBConnection.getInstance().updateWordCounts();
		//Runner r = new Runner();
		//r.printWordsForK();
//		ArticleFeatureMaintainer fm = new ArticleFeatureMaintainer(ArticleFeatureMaintainer.STEMFEATURE);
//		
//		DBConnection.getInstance().getArticleStems(fm, 931599);
//		DBConnection.getInstance().getArticleStems(fm, 1313656);
//		
//		SimpleCoreCalculator c = new SimpleCoreCalculator(fm);
//		c.addInitialDocument(931599);
//		c.setPrintProbabilityData(true);
//		c.extractCoreArticles();
		
//		CoreSupporter c = new CoreSupporter();
//		c.setArticleFeatures(829455, 1313656);
//		c.printIntersectionData();
		
//		ArticleFeatureMaintainer fm = DBConnection.getInstance().getArticleStems();
//		
//		SimpleCoreCalculator c = new SimpleCoreCalculator(fm);
//		c.setPrintInitialDocumentData(false);
//		c.setPrintEntropyData(false);
//		c.setPrintCoreArticleTitles(false);
//		c.setPrintProbabilityData(true);
//		c.addInitialDocument(264622);
//		c.getMostProbableDocument();
//		c.getLeastProbableDocument();
//		
//		
//		c = new SimpleCoreCalculator(fm);
//		c.setPrintInitialDocumentData(false);
//		c.setPrintEntropyData(false);
//		c.setPrintCoreArticleTitles(false);
//		c.setPrintProbabilityData(true);
//		c.addInitialDocument(931599);
//		c.getMostProbableDocument();
//		c.getLeastProbableDocument();
//		
				

//		
		
//		if (args.length == 0) {
//			Scanner scan = new Scanner(System.in);
//			while (true) {
//				Helper.print("Please specify an article ID");
//				id = Integer.parseInt(scan.nextLine());
//				r.runSerendip(fm, id);
//			} 
//		} else {
//			id = Integer.parseInt(args[0]);
//			r.runSerendip(fm, id);
//		}
//			
//
//			
//		}
		
		
	}

}
