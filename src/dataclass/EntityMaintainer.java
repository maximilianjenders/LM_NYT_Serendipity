package dataclass;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import CoreCalculator.Parallel.ParallelMICalculator;
import CoreCalculator.Parallel.ParallelSerendipitousArticleProbabilityCalculator;

import util.Helper;

/***
 * Holder class for entities that allows for calculation and writing of mututal information between entities.
 * Calculation can be done single-threaded or with multiple threads
 * @author Max
 *
 */
public class EntityMaintainer {
	private ArrayList<Entity> entities;
	private int numDocs;
	private static int NUMTHREADS = 24;
	private PrintWriter writer;
	private int count = 0;
	
	ExecutorService executorPool;
	
	public EntityMaintainer() {
		entities = new ArrayList<Entity>();
		executorPool = Executors.newFixedThreadPool(NUMTHREADS);
		try {
			writer = new PrintWriter("misSerial.csv", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * Calculates all mutual information multi-threadedly
	 */
	public void calculateMutualInformation() {
		Set<Future<EntityWithMIs>> mis = new HashSet<Future<EntityWithMIs>>();
		for (int i = 0; i < entities.size() - 1; i++) {
			Callable<EntityWithMIs> callable = new ParallelMICalculator(entities, i, numDocs);
			Future<EntityWithMIs> future = executorPool.submit(callable);
			mis.add(future);
		}
		
		for (Future<EntityWithMIs> future : mis) {
			try {
				EntityWithMIs mi = future.get();
				writeMIs(mi);
			} catch (InterruptedException e) {
				Helper.printErr("ERROR WHEN CALCULATING MUTUAL INFORMATION:");
				e.printStackTrace();
			} catch (ExecutionException e) {
				Helper.printErr("ERROR WHEN CALCULATING MUTUAL INFORMATION:");
				e.printStackTrace();
			}
		}
	}
	
	/***
	 * Calculates the mutual information between all entities and writes them to file
	 */
	public void calculateMutualInformationSingleThreaded() {
		for (int i = 0; i < entities.size() - 1; i++ ) {
			for (int j = i + 1; j < entities.size(); j++) {
				double mi = calculateMutualInformation(entities.get(i), entities.get(j));
				if (mi > 0) {
					writer.write(i + ",");
					writer.write(j + ",");
					writer.write(String.valueOf(mi));
					writer.println();
				}
				count++;

				if (count % 1000000 == 0) Helper.print("Calculated " + count / 1000000+ " million MIs so far");
			}
		}
	}
	
	/***
	 * Calculates the mutual information between two entities
	 * @return the mututal information
	 */
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


	/***
	 * Writes mututal information of provided entities to file
	 * @param mi the "base" entity
	 */
	synchronized private void writeMIs(EntityWithMIs mi) {
		HashMap<Integer, Double> mis = mi.getMutualInformations();
		int e1 = mi.getEntityID();

		for (int e2 : mis.keySet()) {
			writer.write(e1 + ",");
			writer.write(e2 + ",");
			writer.write(String.valueOf(mis.get(e2)));
			writer.println();
			count++;

			if (count % 1000000 == 0) Helper.print("Calculated " + count / 1000000+ " million MIs so far");
		}
		writer.flush();
	}

	//NO check yet whether it already exists
	public void addEntity(Entity e) {
		if (entities.contains(e)) Helper.print("Warning: Inserting entry " + e.getId() + " into EntityMaintainer although it already existed.");
		entities.add(e);
	}

	public int getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(int numDocs) {
		this.numDocs = numDocs;
		for (Entity e : entities) {
			e.calculateProbability(numDocs);
		}
	}

	public ArrayList<Entity> getEntities() {
		return entities;
	}
	
	
}
