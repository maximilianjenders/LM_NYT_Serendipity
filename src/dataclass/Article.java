package dataclass;
import java.util.ArrayList;
import java.util.HashMap;

import util.DBConnection;



/***
 * Represents an article
 * @author Max
 *
 */
public class Article {
	private ArrayList<ArticleFeature> articleFeatures;
	private int id;
	//The total number of feature occurrences
	private int numFeatureOccurences;
	private String title = null;
	private String topic = null;
	
	private int numOccurrences;
	HashMap<Integer, Integer> occurrences = null;
	
	public Article(int articleID) {
		this.id = articleID;
		this.articleFeatures = new ArrayList<ArticleFeature>(500);
	}
	
	/***
	 * This method is only to be used if ID is not know yet, so featureID will be set to -1
	 */
	public void addArticleFeatures(String featureName, int amount) {
		
		//try to find feature
		for (ArticleFeature feature : articleFeatures) {
			if (feature.getFeatureName().equals(featureName)) {
				System.err.println("Feature already exists");
				System.exit(-1);
			}
		}
		//feature does not exist yet
		ArticleFeature feature = new ArticleFeature(id, -1, featureName, amount);
		articleFeatures.add(feature);
		numFeatureOccurences+= amount;
	}
	
	/***
	 * This method is only to be used if ID is not know yet, so featureID will be set to -1
	 */
	public void addArticleFeatures(int featureID, int amount) {
		
		//try to find feature
		for (ArticleFeature feature : articleFeatures) {
			if (feature.getFeatureID() == featureID) {
				System.err.println("Feature already exists");
				System.exit(-1);
			}
		}
		//feature does not exist yet
		ArticleFeature feature = new ArticleFeature(id, featureID, null, amount);
		articleFeatures.add(feature);
		numFeatureOccurences+= amount;
	}

	/***
	 * This method is only to be used when using the Stemmer to create the dataset
	 */
	public void addArticleFeatureInstance(String featureName) {
		numFeatureOccurences+= 1;
		
		//try to find feature
		for (ArticleFeature feature : articleFeatures) {
			if (feature.getFeatureName().equals(featureName)) {
				feature.addInstance();
				return;
			}
		}
		//feature does not exist yet
		ArticleFeature feature = new ArticleFeature(id, -1, featureName, 1);
		articleFeatures.add(feature);
		
	}
	
	/***
	 * Adds a feature instance
	 * @param featureName the name of the feature
	 * @param featureID the id of the feature
	 * @param amount the number of times the feature occurs in the article
	 */
	public void addArticleFeature(String featureName, int featureID, int amount) {
		//try to find feature
		for (ArticleFeature feature : articleFeatures) {
			if (feature.getFeatureID() == featureID) {
				System.err.println("Feature already exists");
				System.exit(-1);
			}
		}
		//Feature does not exist yet
		ArticleFeature feature = new ArticleFeature(id, featureID, featureName, amount);
		articleFeatures.add(feature);
		numFeatureOccurences+= amount;
	}
	
	/***
	 * Adds a feature instance without specifying a name
	 * @param featureID the id of the feature
	 * @param amount the number of times the feature occurs in the article
	 */
	public void addArticleFeature(int featureID, int amount) {
		//try to find feature
//		for (ArticleFeature feature : articleFeatures) {
//			if (feature.getFeatureID() == featureID) {
//				System.err.println("Feature already exists");
//				System.exit(-1);
//			}
//		}
		//Feature does not exist yet
		ArticleFeature feature = new ArticleFeature(id, featureID, amount);
		articleFeatures.add(feature);
		numFeatureOccurences+= amount;
	}
	
		
	public ArrayList<ArticleFeature> getArticleFeatures() {
		return articleFeatures;
	}
	public int getID() {
		return id;
	}
	//For every feature in this article, there is one ArticleFeauture object
	public int getNumFeatures() {
		return articleFeatures.size();
	}

	public int getNumFeatureOccurences() {
		return numFeatureOccurences;
	}

	public synchronized String getTitle() {
		if (title == null) DBConnection.getInstance().updateArticle(this);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTopic() {
		if (topic == null) DBConnection.getInstance().updateArticle(this);
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getNumOccurrences() {
		return numOccurrences;
	}

	public void setNumOccurrences(int numOccurrences) {
		this.numOccurrences = numOccurrences;
	}

	public synchronized HashMap<Integer, Integer> getOccurrences() {
		if (occurrences == null) {
			occurrences = new HashMap<Integer, Integer>(); //map: FeatureID -> count

			numOccurrences = 0;
			//First, count feature frequencies for the article
			for (ArticleFeature feat : getArticleFeatures()) {
				int featureID = feat.getFeatureID();
				if (occurrences.containsKey(featureID)) {
					occurrences.put(featureID, feat.getNumOccurrences() + occurrences.get(featureID));
				} else {
					occurrences.put(featureID, feat.getNumOccurrences());
				}
				numOccurrences += feat.getNumOccurrences();
			}
		}
		
		return occurrences;
	}

	public void setOccurrences(HashMap<Integer, Integer> occurrences) {
		this.occurrences = occurrences;
	}
	
	

}
