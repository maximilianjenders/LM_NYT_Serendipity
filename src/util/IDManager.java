package util;
import java.util.ArrayList;

/***
 * Managing class for IDs - asynchronous Threads can retrieve an article ID to be processed next
 * @author Max
 *
 */
public class IDManager {
	private static ArrayList<Integer> nounArticleIDs;
	private static ArrayList<Integer> stemArticleIDs;

	/***
	 * Returns the next ID of a stemmed article to be used
	 * @return
	 */
	public static synchronized int getNextStemArticleID() {
		if (stemArticleIDs == null) {
			//Load article IDs
			DBConnection db = DBConnection.getInstance();
			stemArticleIDs = db.getNewsArticleIDs("stem");
			Helper.print("Starting. IDs remaining: " + stemArticleIDs.size());
		} else if (stemArticleIDs.size() == 0) {
			Helper.print("All IDs finished");
			return 0;
		}

		if (stemArticleIDs.size() % 1000 == 0) {
			Helper.print("GC running");
			System.gc();
			Helper.print("GC finished");
		}
		int id = stemArticleIDs.get(stemArticleIDs.size() - 1);
		stemArticleIDs.remove(stemArticleIDs.size() - 1);
		if (stemArticleIDs.size() % 100 == 0) Helper.print("IDs remaining: " + stemArticleIDs.size());
		return id;
	}
	
	
	/***
	 * Returns the next ID of a noun article to be used
	 * @return
	 */
	public static synchronized int getNextNounArticleID() {

		if (nounArticleIDs == null) {
			DBConnection db = DBConnection.getInstance();
			nounArticleIDs = db.getNewsArticleIDs("noun");
			Helper.print("Starting. IDs remaining: " + nounArticleIDs.size());
		} else if (nounArticleIDs.size() == 0) {
			Helper.print("All IDs finished");
			return 0;
		}

		if (nounArticleIDs.size() % 1000 == 0) {
			Helper.print("GC running");
			System.gc();
			Helper.print("GC finished");
		}
		int id = nounArticleIDs.get(nounArticleIDs.size() - 1);
		nounArticleIDs.remove(nounArticleIDs.size() - 1);
		if (nounArticleIDs.size() % 100 == 0) Helper.print("IDs remaining: " + nounArticleIDs.size());
		return id;
	}
}
