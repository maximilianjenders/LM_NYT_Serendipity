package util;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

import dataclass.Article;


/**
 * Little helper methods
 * @author Maximilian
 *
 */
public class Helper {
	
	private static Random rand = null;
	/***
	 * Prints a message with the current timestamp
	 * @param message
	 */
	public static void print(String message) {
		System.out.println(Calendar.getInstance().getTime().toString() + " " +  message);
	}
	public static void printErr(String message) {
		System.err.println(Calendar.getInstance().getTime().toString() + " ERROR: " +  message);
	}
	
	public static String readFile(String path) throws IOException {
		return Helper.readFile(path, StandardCharsets.UTF_8);
	}
	public static String readFile(String path, Charset encoding) throws IOException {
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return encoding.decode(java.nio.ByteBuffer.wrap(encoded)).toString();
	}
	
	/***
	 * Calculates the logarithm of n!
	 * log(n!) = log(n) + log(n-1) + ... + log(2)
	 * @param number the n for which log(n!) should be calculated
	 * @return log(n!)
	 */
	public static double logFactorial(int number) {
		double logFactorial = 0;
		for (int i = 2; i <= number; i++) {
			logFactorial += Math.log(i);
		}
		return logFactorial;
	}
	
	/***
	 * Returns English stop words
	 */
	public static String[] getStopWords() {
		String list = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
		
		return list.split(",");
	}
	/***
	 * Returns stemmed English stop words
	 */
	public static String[] getStemmedStopWords() {
		String list = "a,abl,about,across,after,all,almost,also,am,among,an,and,ani,ar,as,at,be,becaus,been,but,by,can,cannot,could,dear,did,do,doe,either,els,ever,everi,for,from,get,got,had,ha,have,he,her,her,him,hi,how,howev,i,if,in,into,is,it,it,just,least,let,like,like,mai,me,might,most,must,my,neither,no,nor,not,of,off,often,on,onli,or,other,our,own,rather,said,sai,sai,she,should,sinc,so,some,than,that,the,their,them,then,there,these,thei,thi,ti,to,too,twa,us,want,wa,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
		return list.split(",");
	}
	
	/***
	 * Counts the number of word occuring in a string
	 * @param s String to be analyzed
	 * @return number of words
	 */
	public static int countWords(String s){
		if (s == null) return 0;
		
	    int wordCount = 0;	

	    boolean word = false;
	    int endOfLine = Math.max(0,  s.length() - 1);

	    for (int i = 0; i < s.length(); i++) {
	        // if the char is a letter, word = true.
	        if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
	            word = true;
	            // if char isn't a letter and there have been letters before,
	            // counter goes up.
	        } else if (!Character.isLetter(s.charAt(i)) && word) {
	            wordCount++;
	            word = false;
	            // last word of String; if it doesn't end with a non letter, it
	            // wouldn't count without this.
	        } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
	            wordCount++;
	        }
	    }
	    return wordCount;
	}
	
	/***
	 * Returns a random integer between 0 (inclusive) and maxNumber (exclusive)
	 */
	public static int getRandomNumber(int maxNumber) {
		if (rand == null) rand = new Random();
		return rand.nextInt(maxNumber);
	}
	
	/***
	 * Shuffles the ordering of a set of articles
	 * @param articles set of articles to be shuffled
	 * @return an ArrayList with randomized ordering
	 */
	public static ArrayList<Article> shuffleArticleOrder(HashSet<Article> articles) {
		ArrayList<Article> shuffled = new ArrayList<Article>(articles.size());
		for (Article a  : articles) {
			shuffled.add(a);
		}
		Collections.shuffle(shuffled);
		return shuffled;
	}
	
	//TODO positive exponent
	/***
	 * reformat of doubles from E notation (e.g. 0.3E-5) to "normal" notation (0.000003)
	 * @param d the double
	 * @return a String with different formatation
	 */
	public static String reformatDouble(double d) {
		String s = String.valueOf(d);
		//see if we have E notation
		int eIndex = s.indexOf("E-");
		if (eIndex < 0) {
			return String.valueOf(d);
		}
		
		int eNumber = Integer.parseInt(s.substring(eIndex + 2));
		s = s.substring(0, eIndex);
		
		s = s.replace(".", "");
		s = s.replace(",", "");
		
		for (int i = 1; i < eNumber; i++) {
			
			s = "0" + s;
		}
		s = "0." + s;
		return s;
	}
}
