package CoreCalculator.Parallel;

import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.swing.DebugGraphics;

import CoreCalculator.ProbabiliyCalculator;

import util.Helper;

import dataclass.Article;
import dataclass.ArticleFeature;
import dataclass.ArticleProbability;

/***
 * Class for parallel calculation of serendipitous articles
 * @author Max
 *
 */
public class ParallelSerendipitousArticleProbabilityCalculator implements Callable<ArticleProbability>{

	Article article;
	HashMap<Integer, Integer> thetaNumerator;
	HashMap<Integer, Integer> thetaDenominator;
	public ParallelSerendipitousArticleProbabilityCalculator(Article article, HashMap<Integer, Integer> thetaNominator, 
			HashMap<Integer, Integer> thetaDenominator) {
		this.article = article;
		this.thetaNumerator = thetaNominator;
		this.thetaDenominator = thetaDenominator;
	}
	

	public ArticleProbability call() throws Exception {
		double numeratorProb = ProbabiliyCalculator.calculateSmoothedLogProability(article, thetaNumerator);
		double denominatorProb = ProbabiliyCalculator.calculateSmoothedLogProability(article,thetaDenominator);
		
		ArticleProbability ap = new ArticleProbability(article, numeratorProb / denominatorProb);
		return ap;
	}
	


}
