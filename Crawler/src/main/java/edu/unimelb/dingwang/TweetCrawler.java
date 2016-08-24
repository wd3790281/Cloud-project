package edu.unimelb.dingwang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.lightcouch.CouchDbClient;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TweetCrawler {

    public static void main(String[] args) {
        double lat1 = 144.32048918720676;
        double longt1 = -38.50832671063084;
        double lat2 = 145.89702727284993;
        double longt2 = -37.16581373796361;
        String consumerKey = "Lxh5sg1An1QIRZZqYQMtfJUgc";
        String consumerSecret = "FJqyyPgZVjCqdxOPZBpqYmRdQug3qzcO5FcQKn07LCNCUWZykN";
        String accessToken = "723039685421895680-DXSrlD8gagpR07N0HwCE8If41YFWs9y";
        String accessSecret = "ZB1vb7lXyJqvYvgjJb8GaQ8JkNzddjLVM1n5BogCB0Wfb";
        if (args.length != 0){
            lat1 = Double.parseDouble(args[0]);
            longt1 = Double.parseDouble(args[1]);
            lat2 = Double.parseDouble(args[2]);
            longt2 = Double.parseDouble(args[3]);
            consumerKey = args[4];
            consumerSecret = args[5];
            accessToken = args[6];
            accessSecret = args[7];
        }
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
        final CouchDbClient dbClient = new CouchDbClient("final", false, "http", "115.146.93.96", 5984, null, null);
        try {


            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername("wd");
            factory.setPassword("123456");
            factory.setHost("115.146.93.96");
            Connection connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setJSONStoreEnabled(true);
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(accessToken)
                    .setOAuthAccessTokenSecret(accessSecret);
            Configuration configuration = cb.build();
            final TwitterStream twitterStream = new TwitterStreamFactory(configuration).getInstance();
            final Twitter twitter = new TwitterFactory(configuration).getInstance();
            RawStreamListener listener = new RawStreamListener() {
                @Override
                public void onMessage(String rawString) {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonObject json = parser.parse(rawString).getAsJsonObject();
                        long userID = json.get("user").getAsJsonObject().get("id").getAsLong();
                        DigTimelineThread digThread = new DigTimelineThread(userID,channel, dbClient, twitter);

                        executor.execute(digThread);
                        long cursor = -1;
                        IDs ids;
                        do{
                            ids = twitter.getFriendsIDs(userID,cursor);
                            for (long id : ids.getIDs()) {
                                DigTimelineThread digThreadFriends = new DigTimelineThread(id, channel, dbClient,twitter);
                                executor.execute(digThreadFriends);
                            }
                        } while ((cursor = ids.getNextCursor()) != 0);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onException(Exception e) {
                    e.printStackTrace();
                }
            };

            FilterQuery filterQuery = new FilterQuery();
            double[][] locations = {{lat1, longt1}, {lat2, longt2}};
            filterQuery.locations(locations);
            twitterStream.addListener(listener);
            twitterStream.filter(filterQuery);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
	  


	