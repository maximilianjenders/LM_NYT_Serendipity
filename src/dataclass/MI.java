package dataclass;

/***
 * Holder class for mutual information between two entities
 * @author Max
 *
 */
public class MI implements Comparable<MI> {

	private int e1ID;
	private int e2ID;
	private double mi;
	
	public MI(int e1, int e2, double mi) {
		if (e1 > e2) {
			e1ID = e2;
			e2ID = e1;
		} else {
			e1ID = e1;
			e2ID = e2;
		}
		this.mi = mi;
	}
	
	

	public int getE1ID() {
		return e1ID;
	}



	public int getE2ID() {
		return e2ID;
	}



	public double getMi() {
		return mi;
	}



	public int compareTo(MI otherMI) {
		if (this.mi > otherMI.getMi()) {
			return 1;
		} else if (this.mi < otherMI.getMi()) {
			return -1;
		}
		return 0;
	}


}
