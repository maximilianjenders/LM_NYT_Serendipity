package dataclass;

import java.util.HashSet;

/***
 * Holder class for an entity that occurs in one or more documents
 * @author Max
 *
 */
public class Entity {
	private int id;
	private HashSet<Integer> documents;
	private double probability;
	
	public Entity(int id) {
		this.id = id;
		documents = new HashSet<Integer>();
	}
	public void addDocument(int docID) {
		documents.add(docID);
	}
	public int getId() {
		return id;
	}
	public HashSet<Integer> getDocuments() {
		return documents;
	}
	public double getProbability() {
		return probability;
	}
	public void calculateProbability(int totalNumberDocuments) {
		this.probability = (1.0 * documents.size()) / totalNumberDocuments;
	}
	
}
