package edu.unimelb.dingwang;

/**
 * Created by dingwang on 16/4/30.
 */
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class NewTask {

    private static final String TASK_QUEUE_NAME = "task_queue";


    public static void sendTask(JsonObject jsonObject, Channel channel) throws Exception {



        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);


        String message = jsonObject.toString();

        channel.basicPublish("", TASK_QUEUE_NAME,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes("UTF-8"));
//        System.out.println(" [x] Sent '" + message + "'");


    }

}
