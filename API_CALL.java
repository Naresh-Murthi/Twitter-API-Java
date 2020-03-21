/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Twitter_API;

import Twitter_Events.SQLconnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author java1
 */
public class API_CALL {

    public static String hashTag = "";
    static final int count = 150;
    static long sinceId = 0;
    static long numberOfTweets = 0;

    public void main(String[] args) {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("*****")
                .setOAuthConsumerSecret("*****")
                .setOAuthAccessToken("*******")
                .setOAuthAccessTokenSecret("****");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        Query querySince = new Query(hashTag);
        querySince.setCount(count);
        querySince.setSinceId(sinceId);
        getTweets(querySince, twitter, "sinceId");
        querySince = null;
        //checkIfSinceTweetsAreAvaliable(twitter);

    }

    public static void getTweets(Query query, Twitter twitter, String mode) {
        boolean getTweets = true;
        long maxId = 0;
        long whileCount = 0;
        while (getTweets) {
            if (numberOfTweets <= 150) {
                try {
                    QueryResult result = twitter.search(query);
                    if (result.getTweets() == null || result.getTweets().isEmpty()) {
                        getTweets = false;
                    } else {
                        System.out.println("***********************************************");
                        System.out.println("Gathered " + result.getTweets().size() + " tweets");
                        int forCount = 0;

                        for (Status status : result.getTweets()) {
                            if (numberOfTweets <= 150) {
                                if (whileCount == 0 && forCount == 0) {
                                    sinceId = status.getId();//Store sinceId in database
                                    System.out.println("sinceId= " + sinceId);
                                }
                                System.out.println("Id= " + status.getId());
                                String user_name = "@" + status.getUser().getScreenName();
                                long id = status.getId();
                                String users = status.getUser().getName();
                                int getFavoriteCount = status.getFavoriteCount();
                                int getRetweetCount = status.getRetweetCount();
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                Date date = new Date();
                                String time = dateFormat.format(status.getCreatedAt());
                                Connection con = SQLconnection.getconnection();
                                Statement st = con.createStatement();
                                String tweet = status.getText();
                                TFIDF calculator = new TFIDF();
                                String num = tweet;
                                double tfidf=0;
      String tag=hashTag;
      String htag[] = tag.split(" ");
                String str[] = num.split(" ");
	List<String> al = new ArrayList<String>();
	al = Arrays.asList(str);
	for(String s: al){
	   System.out.println(s);
	}
        List<List<String>> documents = Arrays.asList(al);
        for (int i = 0; i < htag.length; i++) {
            // accessing each element of array 
             tfidf = calculator.tfIdf(al, htag[i]);
            tfidf+=tfidf;
        }
        System.out.println("TF-IDF" + tfidf);
        double score =tfidf;
                                if(String.valueOf(score)=="NaN")
                                {
                                    score = 0;
                                }
                                System.out.println(score);
                                
                                
                                
                                String myStatement = "insert into tweets(getId, getUser, getName, getText, getFavoriteCount, getRetweetCount, getCreatedAt,hashtag,tfidf) values (?,?,?,?,?,?,?,?,?)";
                                PreparedStatement statement = con.prepareStatement(myStatement);
                                statement.setLong(1, id);
                                statement.setString(2, user_name);
                                statement.setString(3, users);
                                statement.setString(4, tweet);
                                statement.setInt(5, getFavoriteCount);
                                statement.setInt(6, getRetweetCount);
                                statement.setString(7, time);
                                statement.setString(8, hashTag);
                                statement.setDouble(9, score);
                                statement.executeUpdate();
//                                int i = st.executeUpdate("insert into tweets(getId, getUser, getName, getText, getFavoriteCount, getRetweetCount, getCreatedAt) values ('"+ status.getId()+"','"+ status.getUser().getScreenName()+"','" + status.getUser().getName() +"','" + status.getText() +"','" + status.getFavoriteCount()+"','" + status.getRetweetCount() +"','" + status.getCreatedAt()+"') ");
                                System.out.println("@" + status.getUser().getScreenName() + " : " + status.getUser().getName() + "--------" + status.getText() + "--------" + status.getFavoriteCount() + "--------" + status.getRetweetCount() + "--------" + status.getCreatedAt());
                                if (forCount == result.getTweets().size() - 1) {
                                    maxId = status.getId();
                                    System.out.println("maxId= " + maxId);
                                }
                                System.out.println("");
                            } else {
                                getTweets = false;
                                break;
                            }
                        }

                        numberOfTweets = result.getTweets().size() + 150;
                        query.setMaxId(maxId);
                    }

                } catch (TwitterException te) {
                    System.out.println("Couldn't connect: " + te);
                   // System.exit(-6);
                } catch (Exception e) {
                    System.out.println("Something went wrong: " + e);
                   // System.exit(-5);
                }
                whileCount++;
            } else {
                getTweets = false;
                break;
            }
        }

        System.out.println("Total tweets count=======" + (numberOfTweets - 150));
    }
    
}
