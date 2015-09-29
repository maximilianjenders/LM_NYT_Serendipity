package util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import maths.functions.Gaussian;

import algorithms.GaussianMixtureModel;
import algorithms.GaussianMixtureModel.IterationLimit;

import com.nytlabs.corpus.NYTCorpusDocument;

import dataclass.ArticleFeatureMaintainer;
import dataclass.Article;
import dataclass.ArticleFeature;
import dataclass.Entity;
import dataclass.EntityMaintainer;
import dataclass.MI;



/***
 * Handles all Database interaction, i.e., all loading and storing of data
 * @author Maximilian
 *
 */
public class DBConnection {

	private static DBConnection instance = null;

	public static synchronized DBConnection getInstance() {
		if(instance == null) {
			instance = new DBConnection();
		}
		return instance;
	}

	private Connection _conn;

	@SuppressWarnings("unused")
	private int _modulo = 1;
	@SuppressWarnings("unused")
	private int _rest = 0;

	@SuppressWarnings("unused")
	private final int BATCHSIZE = 1500;

	private String _password = "INSERTPASSWORD";
	private String _user = "INSERTUSER";	

	//Lots of prepared statements
	private PreparedStatement docInsert;
	private PreparedStatement peopleInsert;
	private PreparedStatement locationInsert;
	private PreparedStatement organizationInsert;

	private PreparedStatement taxonomicInsert;

	private PreparedStatement docLookup;
	private PreparedStatement peopleLookup;
	private PreparedStatement locationLookup;
	private PreparedStatement organizationLookup;
	private PreparedStatement taxonomicLookup;

	private PreparedStatement getArticleTitleTopicSt;

	private PreparedStatement getNewsStemIDSt;
	private PreparedStatement insertNewsStemEntrySt;
	private PreparedStatement insertNewsStemSt;

	
	private HashMap<String, Integer> peopleIDs;
	private HashMap<String, Integer> locationIDs;
	private HashMap<String, Integer> organizationIDs;
	private HashMap<String, Integer> taxonomicIDs;

	private HashSet<Integer> parsedIDs;


	long stemIDsTime;
	long textTime;
	long insertTime;
	int counter;
	/***
	 * For single-threaded use
	 */
	public DBConnection() {
		this._modulo = 1;
		this._rest = 0;

		connect();
	}

	private HashMap<String, Integer> stemIDs;

	/***
	 * For multi-threaded purposes when large data is to be iterated over. Only data 
	 * for which mod(id, modulo) = rest will be touched. 
	 * @param modulo the modulo number
	 * @param rest the rest that is to remain after the modulo operation
	 */
	public DBConnection(int modulo, int rest) {
		this._modulo = modulo;
		this._rest = rest;

		connect();
	}


	/***
	 * Connects to the database
	 */
	public void connect() {
		try {
			_conn = DriverManager.getConnection("INSERTJDBCPATHHERE", _user, _password);
			assert(_conn.isValid(2));


			setUp();
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}

	/***
	 * Sets up the prparedStatements
	 */
	private void setUp() {

		stemIDs = new HashMap<String, Integer>();

		try {
			docInsert = _conn.prepareStatement("" +
					"INSERT INTO nyt.article(headline, article_abstract, body, author, publication_date, word_count, news_desk, guid) " +
					"VALUES(?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

			peopleInsert = _conn.prepareStatement("INSERT INTO nyt.people(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
			locationInsert = _conn.prepareStatement("INSERT INTO nyt.location(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
			organizationInsert = _conn.prepareStatement("INSERT INTO nyt.organization(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
			taxonomicInsert = _conn.prepareStatement("INSERT INTO nyt.taxonomic_classifier(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);

			docLookup = _conn.prepareStatement("SELECT id FROM nyt.article WHERE guid = ?");
			peopleLookup= _conn.prepareStatement("SELECT id FROM nyt.people WHERE name LIKE ?");
			locationLookup= _conn.prepareStatement("SELECT id FROM nyt.location WHERE name LIKE ?");
			organizationLookup= _conn.prepareStatement("SELECT id FROM nyt.organization WHERE name LIKE ?");
			taxonomicLookup = _conn.prepareStatement("SELECT id FROM nyt.taxonomic_classifier WHERE name LIKE ?");


			getArticleTitleTopicSt = _conn.prepareStatement("" +
					"SELECT news_desk, headline " +
					"FROM nyt.article " +
					"WHERE id = ?");

			getNewsStemIDSt = _conn.prepareStatement("" +
					"SELECT id FROM nyt.stem " +
					"WHERE stem = ?");
			insertNewsStemSt = _conn.prepareStatement("" +
					"INSERT INTO nyt.stem(stem) " +
					"VALUES (?)");
			insertNewsStemEntrySt = _conn.prepareStatement("" +
					"INSERT INTO nyt.article_stem(article_id, stem_id, amount) " +
					"VALUES (?,?,?)");
		} catch (SQLException e) {
			handleSQLException(e);
		}

		peopleIDs = new HashMap<String, Integer>();
		locationIDs = new HashMap<String, Integer>();
		organizationIDs = new HashMap<String, Integer>();
		taxonomicIDs = new HashMap<String, Integer>();	
	}


	/***
	 * Prints out SQL Exceptions with a timestamp
	 * @param e: the SQLException
	 */
	private void handleSQLException(SQLException e) {
		//first of all, print everything to the console
		//		e.printStackTrace();
		Helper.printErr("DATABASE EXCEPION:");
		SQLException e2 = e.getNextException();
		Helper.printErr(e.getMessage());

		while (e2 != null) {
			Helper.printErr(e2.getMessage());
			e2 = e2.getNextException();
		}
	}



	/***
	 * destroys the object and closes the database connection
	 */
	public void destroy() {
		try {
			finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/***
	 * destroys the object and closes the database connection
	 */
	protected void finalize() throws Throwable {
		_conn.close();
		super.finalize();
	}

	/***
	 * Creates a java.sql.Timestamp objects of the current time
	 * @return the current time as a timestamp
	 */
	private Timestamp getCurrentTimestamp() {
		return new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
	}

	/***
	 * Date conversion from java.util.Date to java.sql.Date
	 */
	private java.sql.Date toSQLDate(java.util.Date date) {
		return new java.sql.Date(date.getTime());
	}

	/***
	 * Check whether a certain GUID is present
	 * @param guid to be checked
	 * @return boolean 
	 */
	private synchronized boolean isPresent(int guid) {
		if (parsedIDs.contains(guid)) return true;
		parsedIDs.add(guid);
		return false;
	}

	/***
	 * Inserts an articles into the database
	 * @param doc NYT Doc
	 */
	public void insertArticle(NYTCorpusDocument doc) {
		try {
			docLookup.clearParameters();
			docLookup.setInt(1, doc.getGuid());
			ResultSet rs1 = docLookup.executeQuery();
			//check whether this article was already inserted
			if (rs1.next()) {
				rs1.close();
				return; 
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
		if (isPresent(doc.getGuid())) return;


		int docID = insertDoc(doc);

		insertPeople(docID, doc.getPeople());
		insertLocations(docID, doc.getLocations());
		insertOrganizations(docID, doc.getOrganizations());
		insertPeople(docID, doc.getOnlinePeople());
		insertLocations(docID, doc.getOnlineLocations());
		insertOrganizations(docID, doc.getOnlineOrganizations());
		insertTaxonomicClassifiers(docID, doc.getTaxonomicClassifiers());
	}

	/***
	 * Inserts known person entities for a document
	 */
	private void insertPeople(int docID, List<String> people) {
		for (String person : people) {
			try {
				//person id
				int id = -1;
				if (peopleIDs.containsKey(person)) {
					id = peopleIDs.get(person);
				} else {
					peopleLookup.clearParameters();
					peopleLookup.setString(1, person);
					ResultSet rs1 = peopleLookup.executeQuery();
					if (rs1.next()) {
						id = rs1.getInt(1);
					} else {
						//Does not exist yet
						peopleInsert.clearParameters();
						peopleInsert.setString(1, person);
						peopleInsert.execute();
						ResultSet rs = peopleInsert.getGeneratedKeys();
						rs.next();
						id = rs.getInt(1);
						rs.close();
						peopleIDs.put(person, id);
					}
				}

				Statement st = _conn.createStatement();
				st.execute("INSERT INTO nyt.article_people(article_id, people_id) VALUES(" + docID + ", " + id + ")");
				st.close();
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}
	}
	/***
	 * Inserts known location entities for a document
	 */
	private void insertLocations(int docID, List<String> locations) {
		for (String location : locations) {
			try {
				//person id
				int id = -1;
				if (locationIDs.containsKey(location)) {
					id = locationIDs.get(location);
				} else {
					locationLookup.clearParameters();
					locationLookup.setString(1, location);
					ResultSet rs1 = locationLookup.executeQuery();
					if (rs1.next()) {
						id = rs1.getInt(1);
					} else {
						//Does not exist yet
						locationInsert.clearParameters();
						locationInsert.setString(1, location);
						locationInsert.execute();
						ResultSet rs = locationInsert.getGeneratedKeys();
						rs.next();
						id = rs.getInt(1);
						rs.close();
						locationIDs.put(location, id);
					}
				}

				Statement st = _conn.createStatement();
				st.execute("INSERT INTO nyt.article_location(article_id, location_id) VALUES(" + docID + ", " + id + ")");
				st.close();
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}
	}
	/***
	 * Inserts known organization entities for a document
	 */
	private void insertOrganizations(int docID, List<String> organizations) {
		for (String organization : organizations) {
			try {
				//person id
				int id = -1;
				if (organizationIDs.containsKey(organization)) {
					id = organizationIDs.get(organization);
				} else {
					organizationLookup.clearParameters();
					organizationLookup.setString(1, organization);
					ResultSet rs1 = organizationLookup.executeQuery();
					if (rs1.next()) {
						id = rs1.getInt(1);
					} else {
						//Does not exist yet
						organizationInsert.clearParameters();
						organizationInsert.setString(1, organization);
						organizationInsert.execute();
						ResultSet rs = organizationInsert.getGeneratedKeys();
						rs.next();
						id = rs.getInt(1);
						rs.close();
						organizationIDs.put(organization, id);
					}
				}

				Statement st = _conn.createStatement();
				st.execute("INSERT INTO nyt.article_organization(article_id, organization_id) VALUES(" + docID + ", " + id + ")");
				st.close();
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}
	}
	/***
	 * Inserts taxonomic classifiers for a document
	 */
	private void insertTaxonomicClassifiers(int docID, List<String> taxonomicClassifiers) {
		for (String taxClassifier : taxonomicClassifiers) {
			try {
				//person id
				int id = -1;
				if (taxonomicIDs.containsKey(taxClassifier)) {
					id = taxonomicIDs.get(taxClassifier);
				} else {
					taxonomicLookup.clearParameters();
					taxonomicLookup.setString(1, taxClassifier);
					ResultSet rs1 = taxonomicLookup.executeQuery();
					if (rs1.next()) {
						id = rs1.getInt(1);
					} else {
						//Does not exist yet
						taxonomicInsert.clearParameters();
						taxonomicInsert.setString(1, taxClassifier);
						taxonomicInsert.execute();
						ResultSet rs = taxonomicInsert.getGeneratedKeys();
						rs.next();
						id = rs.getInt(1);
						rs.close();
						taxonomicIDs.put(taxClassifier, id);
					}
				}

				Statement st = _conn.createStatement();
				st.execute("INSERT INTO nyt.article_taxonomic_classifier(article_id, taxonomic_classifier_id) VALUES(" + docID + ", " + id + ")");
				st.close();
			} catch (SQLException e) {
				handleSQLException(e);
			}
		}
	}

	/***
	 * Inserts a document
	 */
	private int insertDoc(NYTCorpusDocument doc) {
		try {
			docInsert.clearParameters();
			//headline, article_abstract, body, author, publication_date, word_count, news_desk
			docInsert.setString(1, doc.getHeadline());
			docInsert.setString(2, doc.getArticleAbstract());
			docInsert.setString(3, doc.getBody());
			docInsert.setString(4, doc.getNormalizedByline());
			docInsert.setDate(5,  toSQLDate(doc.getPublicationDate()));
			docInsert.setInt(6, doc.getWordCount() == null? -1 : doc.getWordCount());
			docInsert.setString(7, doc.getNewsDesk());
			docInsert.setInt(8, doc.getGuid());

			docInsert.executeUpdate();
			ResultSet rs = docInsert.getGeneratedKeys();
			rs.next();
			int id = rs.getInt(1);
			rs.close();
			return id;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Helper.printErr("Error in guid " + doc.getGuid());
			handleSQLException(e);

			return -1;
		}
	}

	/***
	 * Gets article IDs
	 * @param analyzer the analyer tybe (stem or noun)
	 * @return list of articel IDs
	 */
	public synchronized ArrayList<Integer> getNewsArticleIDs(String analyzer) {
		Statement st;
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			st = _conn.createStatement();
			ResultSet rs = st.executeQuery("" +
					"SELECT a.id " +
					"FROM nyt.article a " +
					"WHERE article_abstract IS NOT NULL " +
					"AND a.id NOT IN (" +
					"SELECT DISTINCT article_id " +
					"FROM nyt.article_" + analyzer +
					")");

			while (rs.next()) {
				ids.add(rs.getInt(1));
			}
			rs.close();
			st.close();
			return ids;
		} catch (SQLException e) {
			handleSQLException(e);
			return null;
		}
	}

	/***
	 * Retrieves the text of an article, adding the title to the body
	 * @param newsId ID of article to be fetched
	 * @return String containing article text
	 */
	public String getNewsText(int newsId) {
		long start = System.currentTimeMillis();
		Statement st;
		String str;
		try {
			st = _conn.createStatement();

			ResultSet rs = st.executeQuery("" +
					"SELECT body, headline " +
					"FROM nyt.article " +
					"WHERE id = " + newsId);

			rs.next();
			str = rs.getString(2) + System.lineSeparator() +  rs.getString(1);
			rs.close();
			st.close();
			long end = System.currentTimeMillis();
			textTime += end - start;
			return str;
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return null;
	}

	/***
	 * Inserts the stemmed words of article text into the database
	 * @param article News Article
	 */
	public void storeNewsStems(Article article) {
		try {
			counter++;
			if (counter % 500 == 0) {
				Helper.print("Text time: " + textTime);
				Helper.print("StemID lookup time: " + stemIDsTime);
				Helper.print("Insert time: " + insertTime);
			}
			int i = 0;
			for (ArticleFeature feature : article.getArticleFeatures()) {
				setNewsStemID(feature);

				i++;
				long start = System.currentTimeMillis();

				insertNewsStemEntrySt.setInt(1, feature.getArticleID());
				insertNewsStemEntrySt.setInt(2, feature.getFeatureID());
				insertNewsStemEntrySt.setInt(3, feature.getNumOccurrences());
				insertNewsStemEntrySt.addBatch();

				if (i % 100 == 0) {
					insertNewsStemEntrySt.executeBatch();
					insertNewsStemEntrySt.clearBatch();
				}
				long end = System.currentTimeMillis();
				insertTime += end - start;

			}
			long start = System.currentTimeMillis();

			insertNewsStemEntrySt.executeBatch();
			insertNewsStemEntrySt.clearBatch();
			long end = System.currentTimeMillis();
			insertTime += end - start;


		} catch (SQLException e) {
			handleSQLException(e);
		} 
	}

	/***
	 * Sets the stemID on an ArticleFeature, inserting it into the database if not already present
	 */
	private synchronized void setNewsStemID(ArticleFeature stem) {
		long start = System.currentTimeMillis();

		if (stem.getFeatureID() > 0) return;

		if (stemIDs.containsKey(stem)) {
			stem.setFeatureID(stemIDs.get(stem));
			long end = System.currentTimeMillis();
			stemIDsTime += end - start;
			return;
		}

		try {
			getNewsStemIDSt.clearParameters();
			getNewsStemIDSt.setString(1, stem.getFeatureName());
			ResultSet rs = getNewsStemIDSt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt(1);
				rs.close();
				stem.setFeatureID(id);

				stemIDs.put(stem.getFeatureName(), id);
			} else {
				//Insert Stem into database
				insertNewsStemSt.clearParameters();
				insertNewsStemSt.setString(1, stem.getFeatureName());
				insertNewsStemSt.execute();
				setNewsStemID(stem);
			}
			long end = System.currentTimeMillis();
			stemIDsTime += end - start;
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}

	/***
	 * Updates article data with topic and title
	 * @param article to be updated
	 */
	public void updateArticle(Article article) {
		try {

			getArticleTitleTopicSt.setInt(1, article.getID());
			ResultSet rs = getArticleTitleTopicSt.executeQuery();

			if (rs.next()) {
				article.setTopic(rs.getString(1));
				article.setTitle(rs.getString(2));
			}
			rs.close();
		} catch (SQLException e) {
			handleSQLException(e);
		} 
	} 


	/***
	 * Creates an ArticleFeatureMaintainer, loading data from file.
	 * @return an ArticleFeatureMaintainer with articles and features
	 */
	public ArticleFeatureMaintainer getArticleStems(String fileName) {
		Helper.print("Loading data from file");
		ArticleFeatureMaintainer fm = new ArticleFeatureMaintainer(ArticleFeatureMaintainer.STEMFEATURE);

		int count = 0;

		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				count++;
				String[] parts = line.split(",");
				int aID = Integer.parseInt(parts[0]);
				int sID = Integer.parseInt(parts[1]);
				int amount = Integer.parseInt(parts[2]);
				fm.addFeatureInstance(aID, sID, amount);
				if (count % 10000000 == 0) Helper.print("Count is now " + (count / 1000000) + " million");
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Helper.print("Done loading data from file");
		return fm;
	}

	/***
	 * Loads stems of an article into an ArticleFeatureMaintainer
	 * @param fm the ArticleFeatureMaintainer the data should be stored in
	 * @param articleID the article's ID
	 */
	public void getArticleStems(ArticleFeatureMaintainer fm, int articleID) {
		try {
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery("" +
					"SELECT article_id, stem_id, amount " +
					"FROM nyt.article_stem " +
					"WHERE article_id = " + articleID);
			while (rs.next()) {
				fm.addFeatureInstance(rs.getInt(1), rs.getInt(2), rs.getInt(3));
			}
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}


	/***
	 * Writes article data (articleid, stemid, #occurrences) to a file
	 * @param fileName
	 */
	public void writeDataToFile(String fileName) {
		try {
			Statement st = _conn.createStatement();
			Helper.print("Querying database");
			ResultSet rs = st.executeQuery("" +
					"SELECT article_id, stem_id, amount " +
					"FROM nyt.article_stem artst, nyt.article a " +
					"WHERE a.id = artst.article_id " +
					"AND a.article_abstract IS NOT NULL " +
					"AND a.word_count <= 1892 " + //AVG = 672, STDDEV = 610, AVG+2STDEV = 2026, -> 97%
					"AND a.word_count >= 62 " + //AVG - 1 STDEV
					"AND a.body IS NOT NULL " + 
					"AND a.headline IS NOT NULL " +
					"AND a.headline NOT LIKE '' " +
					"AND a.headline NOT LIKE 'Corrections' " +
					"AND a.headline NOT LIKE 'Correction' " +
					"AND EXTRACT(year FROM publication_date) >= 1998 " +
					"AND EXTRACT(year FROM publication_date) <= 2003"); 
			Helper.print("Writing data");
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			while (rs.next()) {
				writer.print(String.valueOf(rs.getInt(1)));
				writer.print(",");
				writer.print(String.valueOf(rs.getInt(2)));
				writer.print(",");
				writer.print(String.valueOf(rs.getInt(3)));
				writer.println();
			}
			writer.flush();
			writer.close();
			Helper.print("Finished writing");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/***
	 * Retrieves the stem for a given stem ID
	 * @param id the stem's ID
	 * @return the stem
	 */
	public String getFeatureName(int id) {
		String name = null;
		try {
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT stem FROM nyt.stem WHERE id = " + id);
			if (rs.next()) {
				name = rs.getString(1);
			} 
			st.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return name;
	}

	/***
	 * Prints article data to the console
	 * @param id the article's ID
	 */
	public void printArticle(int id) {
		try {
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery("SELECT headline, article_abstract, body FROM nyt.article WHERE id = " + id);
			if (rs.next()) {
				Helper.print("Title: " + rs.getString(1));
				Helper.print("Abstract: " + rs.getString(2));
				Helper.print("Text: " + rs.getString(3));
			} 
			st.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}

	/***
	 * Counts words and stores updated numbers in database
	 */
	public void updateWordCounts() {
		try {
			PreparedStatement pst = _conn.prepareStatement("" +
					"UPDATE nyt.article " +
					"SET word_count = ? " +
					"WHERE id = ?");
			Statement st = _conn.createStatement();
			ResultSet rs = st.executeQuery("" +
					"SELECT id, body " +
					"FROM nyt.article");
			int i = 0;

			while (rs.next()) {
				i++;
				if (i % 300000 == 0) Helper.print("Currently at row " + i);
				int id = rs.getInt(1);
				String text = rs.getString(2);


				pst.setInt(1, Helper.countWords(text));
				pst.setInt(2, id);
				pst.executeUpdate();
			}

			rs.close();
			st.close();
			//			pst.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}

	public void updateAssignments(int initialArticleID, HashSet<Article> articles, boolean advancedRun, String dataSlice) {
		clearAssignments(initialArticleID, advancedRun, dataSlice);
		storeAssignments(initialArticleID, articles, advancedRun, dataSlice);
	}

	/***
	 * Stores article assignments into the database
	 * @param initialArticleID
	 * @param articles Set of articles assigned
	 * @param advancedRun 
	 * @param dataSlice
	 */
	public void storeAssignments(int initialArticleID, HashSet<Article> articles,  boolean advancedRun, String dataSlice) {
		try {
			PreparedStatement st = _conn.prepareStatement("" +
					"INSERT INTO nyt.article_assignment( " +
					"initial_document_id , article_id , advanced_algorithm, data_slice) " +
					"VALUES (?, ?, ?, ?)");

			int i = 0;
			for (Article a : articles) {
				st.setInt(1, initialArticleID);
				st.setInt(2, a.getID());
				st.setBoolean(3, advancedRun);
				st.setString(4, dataSlice);
				st.addBatch();
				i++;
				if (i == 3000) {
					st.executeBatch();
					st.clearBatch();
					i = 0;
				}
			}
			st.executeBatch();
			st.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}
	
	/***
	 * Deletes assignment records from the database
	 */
	public void clearAssignments(int initialArticleID, boolean advancedRun, String dataSlice) {
		try {
			PreparedStatement st = _conn.prepareStatement("" +
					"DELETE FROM nyt.article_assignment " +
					"WHERE initial_document_id = ? " +
					"AND advanced_algorithm = ? " + 
					"AND data_slice = ? ");
			st.setInt(1, initialArticleID);
			st.setBoolean(2, advancedRun);
			st.setString(3, dataSlice);
			st.execute();
			st.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
	}


	/***
	 * Restores (loads) assignments stored in the database
	 * @param initialArticleID initial article
	 * @param dataSlice which data proportion
	 * @param advancedAlgorithm whether advance algorithm was used
	 * @return article IDs stored in DB
	 */
	public synchronized ArrayList<Integer> restoreAssignments(int initialArticleID, String dataSlice, boolean advancedAlgorithm) {
		ArrayList<Integer> articles = new ArrayList<Integer>();
		try {
			PreparedStatement st = _conn.prepareStatement("" +
					"SELECT article_id " +
					"FROM nyt. article_assignment " +
					"WHERE initial_document_id = ? " +
					"AND data_slice = ? " +
					"AND advanced_algorithm = ?" );
			st.setInt(1, initialArticleID);
			st.setString(2, dataSlice);
			st.setBoolean(3, advancedAlgorithm);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				articles.add(rs.getInt(1));
			}
			st.close();
		} catch (SQLException e) {
			handleSQLException(e);
		}
		return articles;
	}

	/***
	 * Writes entities to file
	 * @param fileName
	 */
	public void writeEntities(String fileName) {
		try {
			Statement st = _conn.createStatement();
			Helper.print("Querying database");
			ResultSet rs = st.executeQuery("" +
					"SELECT entity_id, article_id " +
					"FROM nyt.article_entity ae  " +
					"WHERE article_id IN (SELECT article_id FROM nyt.eligible_articles) " +
					"ORDER BY entity_id "); 
			Helper.print("Writing data");
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			while (rs.next()) {
				writer.print(String.valueOf(rs.getInt(1)));
				writer.print(",");
				writer.print(String.valueOf(rs.getInt(2)));
				writer.println();
			}
			writer.flush();
			writer.close();
			Helper.print("Finished writing");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/***
	 * Reads entity data from file
	 * @param fileName
	 * @return EntityMaintainer restored from file
	 */
	public EntityMaintainer getEntityData(String fileName) {
		Helper.print("Loading data from file");
		EntityMaintainer em = new EntityMaintainer();


		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			Entity e = null;
			HashSet<Integer> docIDs = new HashSet<Integer>();

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				int eID = Integer.parseInt(parts[0]);
				int aID = Integer.parseInt(parts[1]);
				docIDs.add(aID);

				if (e == null) {
					e = new Entity(eID);
					e.addDocument(aID);
				} else if (e.getId() == eID) {
					e.addDocument(aID);
				} else {
					em.addEntity(e);
					e = new Entity(eID);
					e.addDocument(aID);
				}
			}
			em.addEntity(e);
			em.setNumDocs(docIDs.size());
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Helper.print("Done loading data from file");
		return em;
	}


	/***
	 * Reads in entity data, orders it by their mutual information, writes data again
	 * @param input initial file
	 * @param output target file
	 */
	public void rewriteEntityDataRanked(String input, String output) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			String line;
			ArrayList<MI> mis = new ArrayList<MI>();

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length != 3) break;
				int e1 = Integer.parseInt(parts[0]);
				int e2 = Integer.parseInt(parts[1]);
				double mi = Double.parseDouble(parts[2]);
				if (e1 < e2) {
					mis.add(new MI(e1, e2, mi));
				}
			}
			br.close();
			Collections.sort(mis);

			PrintWriter writer = new PrintWriter(output, "UTF-8");

			int count = 1;
			for (MI mi : mis) {
				writer.println(count + "," + mi.getMi());
				count++;
			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * Rewrites data, sorting it into buckets according to their mutual information
	 * @param input the original data
	 * @param output target file
	 */
	public void rewriteEntityDataBucketed(String input, String output) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			String line;
			ArrayList<MI> mis = new ArrayList<MI>();

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length != 3) break;
				int e1 = Integer.parseInt(parts[0]);
				int e2 = Integer.parseInt(parts[1]);
				double mi = Double.parseDouble(parts[2]);
				if (e1 < e2) {
					mis.add(new MI(e1, e2, mi));
				}
			}
			br.close();
			Collections.sort(mis);
			double BUCKETSIZE = 0.0001;
			PrintWriter writer = new PrintWriter(output, "UTF-8");

			Helper.print("Bucketing. Have " + mis.size() + " MIs.");
			Helper.print("Smallest mi: " + mis.get(0).getMi() + "; biggest mi: " + mis.get(mis.size() - 1).getMi());
			double currentBucket = 0;
			int count = 0;
			for (MI mi : mis) {
				if (mi.getMi() < (currentBucket + BUCKETSIZE)) {
					count++;
				} else {
					if (count > 0) writer.println(Helper.reformatDouble(currentBucket) + "," + count);
					while (mi.getMi() >= (currentBucket + BUCKETSIZE)) {
						currentBucket += BUCKETSIZE;
					}
					count = 1;
				}
			}
			if (count > 0) {
				writer.println(Helper.reformatDouble(currentBucket) + "," + count);
			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
