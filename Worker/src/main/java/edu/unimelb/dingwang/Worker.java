package edu.unimelb.dingwang;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.SyncFailedException;

import com.google.gson.JsonObject;
import org.lightcouch.CouchDbClient;
import org.lightcouch.Response;

/**
 * Created by dingwang on 16/4/30.
 */
public class Worker {
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("wd");
        factory.setPassword("123456");
        factory.setHost("115.146.93.96");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        final CouchDbClient dbClient = new CouchDbClient("final", false, "http", "115.146.93.96", 5984, null, null);

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
//        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        channel.basicQos(1);

        final Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");

                JsonParser parser = new JsonParser();
                JsonObject json = parser.parse(message).getAsJsonObject();
//                System.out.print(json.get("id_str").getAsString());
//                System.out.println(" [x] Received '" + message + "'");
                try {


                    String tweetText = json.get("text").getAsString();
                    Sentiment sentimentAnalyzer = new Sentiment();
                    TweetWithSentiment tweetWithSentiment = sentimentAnalyzer
                            .findSentiment(tweetText);

                    String sentiment = tweetWithSentiment.getCssClass();

//                    System.out.println(json);

                    json.addProperty("sentiment", sentiment);
//                    System.out.println(json.get("sentiment").getAsString());
                    dbClient.update(json);



                }catch (Exception e){
                    System.out.println("error is " + e);
//                    e.printStackTrace();
                }
                finally {
//                    System.out.println(" [x] Done");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume(TASK_QUEUE_NAME, false, consumer);
    }

}
