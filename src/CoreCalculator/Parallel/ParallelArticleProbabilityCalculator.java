package CoreCalculator.Parallel;

import java.util.HashMap;
import java.util.concurrent.Callable;

import CoreCalculator.ProbabiliyCalculator;

import util.Helper;

import dataclass.Article;
import dataclass.ArticleFeature;
import dataclass.ArticleProbability;

/***
 * Class for parallel calculation of article probabilities given LM
 * @author Max
 *
 */
public class ParallelArticleProbabilityCalculator implements Callable<ArticleProbability>{

	Article article;
	HashMap<Integer, Integer> theta;
	public ParallelArticleProbabilityCalculator(Article article, HashMap<Integer, Integer> theta) {
		this.article = article;
		this.theta = theta;
	}
	

	public ArticleProbability call() throws Exception {
		double prob = ProbabiliyCalculator.calculateSmoothedLogProability(article, theta);

		ArticleProbability ap = new ArticleProbability(article, prob);
		return ap;
	}


}
