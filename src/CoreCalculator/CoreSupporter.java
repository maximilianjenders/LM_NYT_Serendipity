package CoreCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import util.DBConnection;
import util.Helper;


import dataclass.Article;
import dataclass.ArticleFeature;
import dataclass.ArticleFeatureMaintainer;
import dataclass.Feature;


/***
 * Helper functions for CoreCalculator
 * @author Max
 *
 */
public class CoreSupporter {
	
	ArrayList<Feature> features1 = null;
	int features1occurences = 0;
	ArrayList<Feature> features2 = null;
	int features2occurences = 0;
	ArrayList<Integer> intersection = null; 
	
	/***
	 * Loads and stores ArticleFeatures for given articles
	 */
	public void setArticleFeatures(int article1ID, int article2ID) {
		
		ArticleFeatureMaintainer fm = new ArticleFeatureMaintainer(ArticleFeatureMaintainer.STEMFEATURE);
		
		DBConnection.getInstance().getArticleStems(fm, article1ID);
		DBConnection.getInstance().getArticleStems(fm, article2ID);

		setArticleFeatures(fm.getArticle(article1ID), fm.getArticle(article2ID));
	}
	
	/***
	 * Loads and stores ArticleFeatures for given articles
	 */
	public void setArticleFeatures(Article article1, Article article2) {
		ArrayList<ArticleFeature> feat1 = article1.getArticleFeatures();
		ArrayList<ArticleFeature> feat2 = article2.getArticleFeatures();
		
		prepareData(feat1, feat2);
	}
	
	/***
	 * Loads ArticleFeatures for given articles
	 */
	public void setArticleFeatures(ArrayList<Article> articles1, Article article2) {
		ArrayList<ArticleFeature> feat1 = new ArrayList<ArticleFeature>();
		for (Article a : articles1) {
			for (ArticleFeature af : a.getArticleFeatures()) {
				feat1.add(af);
			}
		}
		prepareData(feat1, article2.getArticleFeatures());
	}
	
	/***
	 * Loads ArticleFeatures for given articles
	 */
	public void setArticleFeatures(ArrayList<Article> articles1, ArrayList<Article> articles2) {
		ArrayList<ArticleFeature> feat1 = new ArrayList<ArticleFeature>();
		for (Article a : articles1) {
			for (ArticleFeature af : a.getArticleFeatures()) {
				feat1.add(af);
			}
		}
		
		ArrayList<ArticleFeature> feat2 = new ArrayList<ArticleFeature>();
		for (Article a : articles2) {
			for (ArticleFeature af : a.getArticleFeatures()) {
				feat2.add(af);
			}
		}
		
		prepareData(feat1, feat2);
	}
	
	/***
	 * Extracts features, calculates feature occurrences
	 * @param feat1
	 * @param feat2
	 */
	private void prepareData(ArrayList<ArticleFeature> feat1, ArrayList<ArticleFeature> feat2) {
		features1 = extractFeatures(feat1);
		features2 = extractFeatures(feat2);
		Collections.sort(features1);
		Collections.sort(features2);
		
		intersection = new ArrayList<Integer>();
		
		for (Feature f1 : features1) {
			for (Feature f2 : features2) {
				if (f1.getID() == f2.getID()) {
					intersection.add(f1.getID());
				}
			}
		}
		
		features1occurences = 0;
		for (Feature f : features1) {
			features1occurences += f.getNumOccurences();
		}
		
		features2occurences = 0;
		for (Feature f : features2) {
			features2occurences += f.getNumOccurences();
		}
	}
	
	
	/***
	 * Given ArticleFeatures, extracts Features and returns them
	 * @param afs ArticleFeatures
	 * @return all extracted Features
	 */
	private ArrayList<Feature> extractFeatures(ArrayList<ArticleFeature> afs) {
		HashMap<Integer, Feature> features = new HashMap<Integer, Feature>();
		for (ArticleFeature af : afs) {
			int featureID = af.getFeatureID();
			if (features.containsKey(featureID)) {
				features.get(featureID).addArticleFeature(af);
			} else {
				Feature f = new Feature(featureID, af.getFeatureName());
				f.addArticleFeature(af);
				features.put(featureID, f);
			}
		}
		
		return new ArrayList<Feature>(features.values());
	}
	
	/***
	 * Some data output to console for testing purposes
	 */

	public void printArticle1Data() {
		Helper.print(" ------   Data for Article1 set -----");
		Helper.print(" ------   Data for Article1 set -----");
		Helper.print(" ------   Data for Article1 set -----");
		for (Feature f : features1) {
 			Helper.print(f.getName() + ": ABS: " + f.getAbsoluteFrequency() + ", REL: " + f.getRelativeFrequency(features1occurences));
		}
	}
	/***
	 * Some data output to console for testing purposes
	 */

	public void printArticle2Data() {
		Helper.print(" ------   Data for Article2 set -----");
		Helper.print(" ------   Data for Article2 set -----");
		Helper.print(" ------   Data for Article2 set -----");
		for (Feature f : features2) {
			Helper.print(f.getName() + ": ABS: " + f.getAbsoluteFrequency() + ", REL: " + f.getRelativeFrequency(features2occurences));
		}
	}
	/***
	 * Some data output to console for testing purposes
	 */
	public void printIntersectionData() {
		Helper.print(" ------   Data for the intersection -----");
		Helper.print(" ------   Data for the intersection -----");
		Helper.print(" ------   Data for the intersection -----");
		for (int featureID : intersection) {
			Feature f1 = null;
			Feature f2 = null;

			for (Feature f : features1) {
				if (f.getID() == featureID) {
					f1 = f;
					break;
				}
			}
			for (Feature f : features2) {
				if (f.getID() == featureID) {
					f2 = f;
					break;
				}
			}
			Helper.print(f1.getName() + ": " + 
					"ABS_1: " + f1.getAbsoluteFrequency() + ", " +
					"ABS_2: " + f2.getAbsoluteFrequency() + "; " + 
					"REL_1: " + f1.getRelativeFrequency(features1occurences) + ", " + 
					"REL_2: " + f1.getRelativeFrequency(features2occurences)
					);
		}
		
		
	}
}
