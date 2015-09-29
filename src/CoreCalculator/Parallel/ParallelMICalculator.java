package CoreCalculator.Parallel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import CoreCalculator.ProbabiliyCalculator;

import dataclass.ArticleProbability;
import dataclass.Entity;
import dataclass.EntityWithMIs;

/***
 * Class for parallel calculation of mutual information
 * @author Max
 *
 */
public class ParallelMICalculator implements Callable<EntityWithMIs> {
	
	ArrayList<Entity> entities;
	int startEntityID;
	int numDocs;
	
	public ParallelMICalculator(ArrayList<Entity> entities, int startEntityID, int numDocs) {
		this.entities = entities;
		this.startEntityID = startEntityID;
		this.numDocs =  numDocs;
	}

	public EntityWithMIs call() throws Exception {
		EntityWithMIs emi = new EntityWithMIs(startEntityID);
		
		
		for (int i = startEntityID; i < entities.size() - 1; i++) {
			for (int j = i + 1; j < entities.size(); j++) {
				double mi = calculateMutualInformation(entities.get(i),  entities.get(j));
				if (mi > 0) {
					emi.addMI(j, mi);
				}
			}
		}
		return emi;
	}
	
	public double calculateMutualInformation(Entity e1, Entity e2) {
		double e1Prob = e1.getProbability();
		double e2Prob = e2.getProbability();
		
		Set<Integer> intersection = new HashSet<Integer>(e1.getDocuments());
		intersection.retainAll(e2.getDocuments());
		double jointProb = (intersection.size() * 1.0) / numDocs;
		
		double mi = 0;
		
		if (jointProb > 0) {
			mi = jointProb * (Math.log(jointProb) - (Math.log(e1Prob) + Math.log(e2Prob)));
		}
		return mi;
	}
}
