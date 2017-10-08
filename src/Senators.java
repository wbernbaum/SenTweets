import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.jhenrique.manager.TweetManager;
import me.jhenrique.manager.TwitterCriteria;
import me.jhenrique.model.Tweet;

public class Senators {
	// map from senator name to twitter handle
	private static Map<String, String> senatorHandles;
	// map from senator name to String containing original csv data: state,name,handle
	private static Map<String, String> originalTSV;
	
	private final static String input = "2017_senator_names_handles.tsv";
	private final static String dateLowerBound = "2016-10-8";
	private final static String dateUpperBound = "2017-10-8";
	
	// reads Senators and Twitter Handles from file
	// creates map from name to handle
	public static void parseSenators() {
		BufferedReader reader = null;
		try {
		    reader = new BufferedReader(new FileReader(input));
		    String line;
		    while ((line = reader.readLine()) != null) {
		        String[] tokens = line.split("\t");
		        senatorHandles.put(tokens[1], tokens[2]);
		        originalTSV.put(tokens[1], line);
		    }
		} catch (IOException e) {
		    System.err.println(e.toString());
		} finally {
		    if (reader != null) {
		        try {
		            reader.close();
		        } catch (IOException e) {
		            System.err.println(e.toString());
		        }
		    }
		
		}
	}
	
	//return a list of Strings in the format tweet,hashtag containing
	//all the tweets of a given Senator
	public static List<String> singleSenator(String name, int max) {
		TwitterCriteria criteria = TwitterCriteria.create()
                .setUsername(name)
                .setSince(dateLowerBound)
                .setUntil(dateUpperBound)
                .setMaxTweets(max);
		List<Tweet> tweets = TweetManager.getTweets(criteria);
		List<String> results = new ArrayList<String>();
		for (Tweet t : tweets) {
			String r = t.getText() + "\t" + t.getHashtags();
			results.add(r);
		}
		return results;
	}
	
	//iterate over keyset of Senators and populate output file with tweets
	//each tweet should be printed on a separate line
	public static void main (String[] args) {
		senatorHandles = new TreeMap<String, String>();
		originalTSV = new TreeMap<String, String>();
		parseSenators();
		
	    for (String key : senatorHandles.keySet()) {
	    	String handle = senatorHandles.get(key);
	    	String state = originalTSV.get(key).split("\t")[0].replace(" ", "_");
			String lname = originalTSV.get(key).split("\t")[1].split(" ")[1];
			
			Path curPath = Paths.get(System.getProperty("user.dir"));
			Path outPath = Paths.get(curPath.toString(), "data", state + "_" + lname + ".txt");
			List<String> tweets = singleSenator(handle, 1000000000);
			
			Writer writer = null;
			try {
			    writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(outPath.toString()), "utf-8"));
				for (String tweet : tweets) {
					writer.write(tweet + "\n");
				}
			} catch (IOException ex) {
			  // report
			} finally {
			   try {writer.close();} catch (Exception ex) {/*ignore*/}
			}
		}
	    System.exit(0);
	}
}
