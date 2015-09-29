package dataclass;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import util.DBConnection;


/***
 * Holding class that stores the mapping between feature / article IDs and the features / articles
 * @author Max
 *
 */
public class ArticleFeatureMaintainer {
	
	public static String NOUNFEATURE = "noun";
	public static String STEMFEATURE = "stem";
	
	//Mapping: ID -> Object
	HashMap<Integer, Article> articles;
	HashMap<Integer, Feature> features;
	String featureType;
	
	public ArticleFeatureMaintainer(String featureType) {
		this.features = new HashMap<Integer, Feature>();
		this.articles = new HashMap<Integer, Article>(700000);
		this.featureType = featureType;
	}
	
	/***
	 * Adds an instance (a single feature in an article) to the ArticleFeatureMaintainer. 
	 * @param articleID the id of the article
	 * @param featureID the id of the feature
	 * @param featureName the name of the feature
	 * @param amount the amount of times the feature occurs
	 */
	public void addFeatureInstance(int articleID, int featureID, String featureName, int amount) {
		//articles
		if (articles.containsKey(articleID)) {
			articles.get(articleID).addArticleFeature(featureName, featureID, amount);
		} else {
			Article a = new Article(articleID);
			a.addArticleFeature(featureName, featureID, amount);
			articles.put(articleID, a);
		}
		
		//features
		if (features.containsKey(featureID)) {
			features.get(featureID).addArticleFeature(articleID, amount);
		} else {
			Feature f = new Feature(featureID, featureName);
			f.addArticleFeature(articleID, amount);
			features.put(featureID, f);
		}
	}
	
	/***
	 * Adds an instance (a single feature in an article) to the ArticleFeatureMaintainer. 
	 * @param articleID the id of the article
	 * @param featureID the id of the feature
	 * @param amount the amount of times the feature occurs
	 */
	public void addFeatureInstance(int articleID, int featureID, int amount) {
		//articles
		if (articles.containsKey(articleID)) {
			articles.get(articleID).addArticleFeature(featureID, amount);
		} else {
			Article a = new Article(articleID);
			a.addArticleFeature(featureID, amount);
			articles.put(articleID, a);
		}
		
		//features
		if (features.containsKey(featureID)) {
			features.get(featureID).addArticleFeature(articleID, amount);
		} else {
			Feature f = new Feature(featureID);
			f.addArticleFeature(articleID, amount);
			features.put(featureID, f);
		}
	}
	
	
	public ArrayList<Feature> getFeatures() {
		return new ArrayList<Feature>(features.values());
	}
	public ArrayList<Article> getArticles() {
		return new ArrayList<Article>(articles.values());
	}
	public HashSet<Article> getArticlesAsSet() {
		return new HashSet<Article>(articles.values());
	}
	public Feature getFeature(int featureID) {
		return features.get(featureID);
	}
	public Article getArticle(int articleID) {
		return articles.get(articleID);
	}
	
	
}
