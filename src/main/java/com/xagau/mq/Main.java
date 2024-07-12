/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import com.xagau.atomiic.Log;

import java.sql.Timestamp;

/**
 *
 * @author Sean
 */
public class Main {
    public static void main(String[] args) throws Exception {
        /*
        String uri = System.getenv("CLOUDAMQP_URL");
        if (uri == null) uri = "amqp://ydmpjtps:XXXXXXXXXXXXXXXXXXXXXXXX@crane.rmq.cloudamqp.com/ydmpjtps";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);

        //Recommended settings
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String queue = "hello";     //queue name
        boolean durable = false;    //durable - RabbitMQ will never lose the queue if a crash occurs
        boolean exclusive = false;  //exclusive - if queue only will be used by one connection
        boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

        channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
        

                
        
        String message = "Hello CloudAMQP!";

        String exchangeName = "";
        String routingKey = "hello";
        channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        Log.info(" [x] Sent '" + message + "'");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queue, true, consumer);

        while (true) {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          message = new String(delivery.getBody());
          Log.info(" [x] Received '" + message + "'");
        }
        */
        Log.info("Run either Consume or Publish [E|F]");
    }
}
