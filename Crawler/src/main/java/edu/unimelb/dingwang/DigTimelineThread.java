package edu.unimelb.dingwang;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;
import twitter4j.*;



public class DigTimelineThread implements Runnable {
    private long id;
    private CouchDbClient dbClient;
    private Channel channel;
    private Twitter twitter;

    public DigTimelineThread(Long userID, Channel channel, CouchDbClient dbClient, Twitter twitter) {
        this.id = userID;
        this.channel = channel;
        this.twitter = twitter;
        this.dbClient = dbClient;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

            try {

                int sleeptime = 1;

                Thread.sleep(sleeptime * 1000);
                Paging page = new Paging(1,300);
                ResponseList<Status> statuses = twitter.getUserTimeline(id, page);

                for (Status status : statuses) {
                    JsonParser parser = new JsonParser();
                    JsonObject json = parser.parse(TwitterObjectFactory.getRawJSON(status)).getAsJsonObject();
                    json.addProperty("_id", json.get("id_str").getAsString());

                    Response resp = dbClient.save(json);
//                    System.out.println(json.get("id_str").getAsString());
                    json.addProperty("_rev", resp.getRev());

                    NewTask.sendTask(json,this.channel);


                }

            } catch (TwitterException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }
}
