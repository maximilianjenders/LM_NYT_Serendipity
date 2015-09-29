package dataclass;
import java.util.ArrayList;

import util.DBConnection;
import util.Helper;

/***
 * Holder class for a feature
 * @author Max
 *
 */
public class Feature implements Comparable<Feature> {
	private ArrayList<ArticleFeature> articleFeatures;
	private int id;
	private String name;

	private int numOccurences;
	

	/***
	 * Constructor
	 * @param featureID the ID
	 * @param featureName the name
	 */
	public Feature(int featureID, String featureName) {
		this.id = featureID;
		this.name = featureName;
		this.articleFeatures = new ArrayList<ArticleFeature>();
	}
	
	/***
	 * Constructor with unknown featureName
	 * @param featureID the ID
	 */
	public Feature(int featureID) {
		this.id = featureID;
		this.name = null;
		this.articleFeatures = new ArrayList<ArticleFeature>();
	}
	

	/***
	 * Adds a feature occurence in an article
	 * @param articleID the article ID
	 * @param position the position of the instance
	 */
	public void addArticleFeature(int articleID, int amount) {

//		try to find feature
		for (ArticleFeature aFeature : articleFeatures) {
			if (aFeature.getArticleID() == articleID) {
				System.err.println("ArticleFeature already exists");
				System.exit(-1);
			}
		}
		//Feature does not exist yet
		ArticleFeature aFeature = new ArticleFeature(articleID, id, name, amount);
		articleFeatures.add(aFeature);
		numOccurences+= amount;
	}
	
	/***
	 * Adds a complete ArticleFeature object
	 * @param af the ArticleFeature to add
	 */
	public void addArticleFeature(ArticleFeature af) {
		//try to find feature
		for (ArticleFeature aFeature : articleFeatures) {
			if (aFeature.getArticleID() == af.getArticleID()) {
				Helper.printErr("ArticleFeature already exists");
				System.exit(-1);
			}
		}

		articleFeatures.add(af);
		numOccurences += af.getNumOccurrences();
	}
	
	public void addInstance() {
		numOccurences++;
	}

	/***
	 * Returns an int[] with the number of feature instances for each article containing this feature
	 * @return
	 */
	public double[] getArticleOccurences() {
		double[] occurences = new double[getNumArticles()];
		for (int i = 0; i < getNumArticles(); i++) {
			occurences[i] = articleFeatures.get(i).getNumOccurrences();
		}
		return occurences;
	}

	public ArrayList<ArticleFeature> getArticleFeatures() {
		return articleFeatures;
	}
	public int getID() {
		return id;
	}

	//For each article with this feature, there is one ArticleFeature object
	public int getNumArticles() {
		return articleFeatures.size();
	}
	public int getNumOccurences() {
		return numOccurences;
	}
	public String getName() {
		if (name == null) {
			name = DBConnection.getInstance().getFeatureName(id);
		}
		return name;
	}
	
	
	/***
	 * Sorting so that the highest frequencies will be at the first positions
	 */
	@Override
	public int compareTo(Feature f) {
		if (f.getNumOccurences() > numOccurences) {
			return 1;
		} else if (f.getNumOccurences() < numOccurences) {
			return -1;
		} return 0;
	}


	public int getAbsoluteFrequency() {
		return getNumOccurences();
	}

	public double getRelativeFrequency(int totalOccurrences) {
		return numOccurences * 1.0 / totalOccurrences;
	}




}
