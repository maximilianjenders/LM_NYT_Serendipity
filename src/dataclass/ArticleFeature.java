package dataclass;
import java.util.ArrayList;
import java.util.HashMap;

/***
 * represent a feature in an article. Can have multiple occurences, as noted in the numOccurrences attribute
 * @author Max
 *
 */
public class ArticleFeature implements Comparable<ArticleFeature> {
	int articleID;
	int featureID;
	String featureName;
	int numOccurrences;
	
	/***
	 * Constructor if multiple positions are known in advance
	 * @param articleID the article ID
	 * @param featureID the feature ID
	 * @param positions a list of all feature instance positions
	 * @param featureName the name of the feature
	 */
	public ArticleFeature(int articleID, int featureID, int numOccurrences, String featureName) {
		this.articleID = articleID;
		this.featureID = featureID;
		this.numOccurrences = numOccurrences;
		this.featureName = featureName;
	}
	
	/***
	 * Constructor if multiple positions are known in advance nad the featureName is not known
	 * @param articleID the article ID
	 * @param featureID the feature ID
	 * @param positions a list of all feature instance positions
	 */
	public ArticleFeature(int articleID, int featureID, int numOccurrences) {
		this.articleID = articleID;
		this.featureID = featureID;
		this.numOccurrences = numOccurrences;
		this.featureName = null;
	}
	
	/***
	 * Constructor only one instance is known
	 * @param articleID the article ID
	 * @param featureID the feature ID
	 * @param positions a list of all feature instance positions
	 * @param featureName the name of the feature
	 */
	public ArticleFeature(int articleID, int featureID,  String featureName, int numOccurrences) {
		this.articleID = articleID;
		this.featureID = featureID;
		this.numOccurrences = numOccurrences;
		this.featureName = featureName;
	}
	

	public ArticleFeature(int articleID) {
		this.articleID = articleID;
		this.numOccurrences = 0;
	}

	public int getArticleID() {
		return articleID;
	}
	
	public void setFeatureID(int id) {
		this.featureID = id;
	}
	public int getFeatureID() {
		return featureID;
	}

	public int getNumOccurrences() {
		return numOccurrences;
	}
	//Alias for numOccurrences
	public int getFrequency() {
		return numOccurrences;
	}
	
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getFeatureName() {
		return featureName;
	}

	@Override
	/***
	 * Sorting so that the highest frequencies will be at the first positions
	 */
	public int compareTo(ArticleFeature af) {
		if (af.getNumOccurrences() > getNumOccurrences()) {
			return 1;
		} else if (af.getNumOccurrences() < getNumOccurrences()) {
			return -1;
		} return 0;
	}

	/***
	 * This method is only to be used when using the Stemmer to create the dataset
	 */
	public void addInstance() {
		numOccurrences++;
	}
}
