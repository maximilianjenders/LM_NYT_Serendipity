package dataclass;

/***
 * Holder class for the log probability of an article
 * @author Max
 *
 */
public class ArticleProbability {
	Article article;
	double logProbability;
	
	public ArticleProbability(Article article, double logProbability) {
		this.article = article;
		this.logProbability = logProbability;
	}

	public Article getArticle() {
		return article;
	}

	public double getLogProbability() {
		return logProbability;
	}
	
}
