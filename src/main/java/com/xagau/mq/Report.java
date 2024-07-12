/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.atomiic.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.xagau.mq.reports.Transaction;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sean
 */
public class Report extends BasicMQ {

    public Report() {
        ensureConnection();
    }

    public String reply()
    {
        ensureConnection();
        Channel channel = null;
        try {
            channel = connection.createChannel();
            String queue = "replies-" + Server.getProperty("hostname");     //queue name
            Log.info("Receive from Queue:" + queue);
            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            boolean flag = true;
            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();
                    // Decryption
                    //AesGcmJce agjDecryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] decrypted = agjDecryption.decrypt(encrypted, aad.getBytes("ISO-8859-1"));

                    json = new String(encrypted);
                    return json;
                } catch(Exception ex) {
                    Utility.lag();
                    ex.printStackTrace();
                } 
            } 
        } catch(Exception ex) { 
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (IOException | TimeoutException ex) {
                Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    public void request(Request r) {
        ensureConnection();
        Channel channel = null;
        try {
            channel = connection.createChannel();

            String mqueue = "requests-" + Server.getProperty("hostname");     //queue name
            Log.info("Write to Queue:" + mqueue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();
            // 2. Java object to JSON string
            String request = gson.toJson(r);
            try {
                channel.queueDeclare(mqueue, durable, exclusive, autoDelete, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));
            String exchangeName = "";
            String routingKey = "requests-" + Server.getProperty("hostname");
            Log.info("Routing-Key:" + mqueue);
            channel.basicPublish(exchangeName, routingKey, null, request.getBytes());
            Log.info(" [x] Sent Request '" + new String(request) + "'");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (IOException | TimeoutException ex) {
                Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public String getTransactions() {
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(new File(Server.getProperty("listtransactions")));
            reader = new BufferedReader(new InputStreamReader(fis));
            String buf = null;
            String retVal = "";
            while ((buf = reader.readLine()) != null) {
                //Log.info("gt:" + buf);
                retVal += buf;
            }            
            return retVal;
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(Report.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                reader.close();
                fis.close();
                Utility.lag();
            } catch(Exception ex) { 
                ex.printStackTrace();
            }
        }
        return null;
    }

    public void process() {

        Channel channel = null;

        try {

            Log.info("Start process()");
            ensureConnection();

            channel = connection.createChannel();
            
            String queue = "requests-"  + Server.getProperty("hostname");  //queue name
            Log.info("Process queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            try {
                channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            
            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            Gson gson = new Gson();

            boolean flag = true;

            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();
                    // Decryption
                    //AesGcmJce agjDecryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] decrypted = agjDecryption.decrypt(encrypted, aad.getBytes("ISO-8859-1"));

                    json = new String(encrypted);
                    
                    Log.info("Received Request '" + json + "'");

                    Request r = gson.fromJson(json, Request.class);

                    // if r ... do different types of reports?

                    String txs = getTransactions();

                    //com.xagau.mq.reports.Transaction[] transactions = (com.xagau.mq.reports.Transaction[]) gson.fromJson(txs, com.xagau.mq.reports.Transaction[].class);
                    Channel nchannel = connection.createChannel();

                    String mqueue = "replies-"  + Server.getProperty("hostname");     //queue name
                    Log.info("Process Queue:" + mqueue);
            
                    // 2. Java object to JSON string
                    //String replyJson = gson.toJson(transactions);
                    try {
                        nchannel.queueDeclare(mqueue, durable, exclusive, autoDelete, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));
                    String exchangeName = "";
                    String routingKey = "replies-" + Server.getProperty("hostname");
                    Log.info("Routing Key:" + routingKey);
                    nchannel.basicPublish(exchangeName, routingKey, null, txs.getBytes());
                    Log.info(" [x] Sent Response '" + new String(txs) + "'");

                    nchannel.close();
                    
                } catch (Exception ex) {
                    Utility.lag();

                    Log.info(ex);
                    ex.printStackTrace();
                }
                
            }

        } catch (Exception ex) {

            Log.info(ex);
            ex.printStackTrace();
        } finally {
            if( channel.isOpen() ){
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
            Utility.lag();
        }
    }

    public void heartbeat() {

        Channel channel = null;

        try {

            Log.info("Start process()");
            ensureConnection();

            channel = connection.createChannel();

            String queue = "requests-"  + Server.getProperty("hostname");  //queue name
            Log.info("Process queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            try {
                channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            Gson gson = new Gson();

            boolean flag = true;

            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();
                    // Decryption
                    //AesGcmJce agjDecryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] decrypted = agjDecryption.decrypt(encrypted, aad.getBytes("ISO-8859-1"));

                    json = new String(encrypted);

                    Log.info("Received Request '" + json + "'");

                    Request r = gson.fromJson(json, Request.class);

                    // if r ... do different types of reports?
                    String response = "";
                    if( r.getPage().equals("pong")) {
                        response = "ping";
                    }

                    //com.xagau.mq.reports.Transaction[] transactions = (com.xagau.mq.reports.Transaction[]) gson.fromJson(txs, com.xagau.mq.reports.Transaction[].class);
                    Channel nchannel = connection.createChannel();

                    String mqueue = "replies-"  + Server.getProperty("hostname");     //queue name
                    Log.info("Process Queue:" + mqueue);

                    // 2. Java object to JSON string
                    //String replyJson = gson.toJson(transactions);
                    try {
                        nchannel.queueDeclare(mqueue, durable, exclusive, autoDelete, null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));
                    String exchangeName = "";
                    String routingKey = "replies-" + Server.getProperty("hostname");
                    Log.info("Routing Key:" + routingKey);
                    nchannel.basicPublish(exchangeName, routingKey, null, response.getBytes());
                    Log.info(" [x] Sent Response '" + new String(response) + "'");

                    nchannel.close();

                } catch (Exception ex) {
                    Utility.lag();

                    Log.info(ex);
                    ex.printStackTrace();
                }

            }

        } catch (Exception ex) {

            Log.info(ex);
            ex.printStackTrace();
        } finally {
            if( channel.isOpen() ){
                try {
                    channel.close();
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
            Utility.lag();
        }
    }
}
