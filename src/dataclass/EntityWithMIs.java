package dataclass;

import java.util.HashMap;

/***
 * Stores the mutual information between one entity and other entities
 * @author Max
 *
 */
public class EntityWithMIs {
	private int entityID;
	private HashMap<Integer, Double> mutualInformations; //entity -> MI
	
	public EntityWithMIs(int entityID) {
		this.entityID = entityID;
		mutualInformations = new HashMap<Integer, Double>();
	}
	
	public void addMI(int entityID, double mi) {
		mutualInformations.put(entityID, mi);
	}

	public int getEntityID() {
		return entityID;
	}

	public HashMap<Integer, Double> getMutualInformations() {
		return mutualInformations;
	}
	
	
}
