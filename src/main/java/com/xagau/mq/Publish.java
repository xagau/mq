/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.mq.vtm.Transaction;
import com.xagau.atomiic.Coin;
import com.xagau.atomiic.Log;
import com.google.crypto.tink.subtle.AesGcmJce;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author Sean
 */
public class Publish extends BasicMQ {
    
    public Publish(boolean force)
    {
        ensureConnection();
    }
    
    public Publish()
    {
        
        ensureConnection();
    }

    public void publish(Transaction t)
    {
        Channel channel = null;
        try {
            ensureConnection();
            
            channel = connection.createChannel();

            String queue = "transactions-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(t);


            try {
                channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            } catch(Exception ex) { }

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "transactions-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + new String(json) + "'");

            channel.close();
        } catch(Exception ex) { 
            Log.info("ERROR: publish failed:" + ex.getMessage());
        } finally {
            try { 
                channel.close();
                Utility.lag();
            } catch(Exception ex) {} 
        }
    }
    
    public void publish(Receipt r)
    {
        Channel channel = null;
        try {
            ensureConnection();
            
            channel = connection.createChannel();

            String queue = "receipts-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(r);


            try {
                channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            } catch(Exception ex) { }

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "receipts-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + new String(json) + "'");

            channel.close();
        
        } catch(Exception ex) { 
            Log.info("ERROR: publish failed:" + ex.getMessage());
        } finally {
            try { 
                channel.close();
                Utility.lag();
            } catch(Exception ex) {} 
        }
    }
    
    
    public static void main(String[] args) throws Exception {

        Log.info("WARNING: THIS METHOD SENDS A LIVE TEST TRANSACTION [F]");

        try {
            System.in.read();
        } catch(Exception ex) { }
        Transaction t = new Transaction();
        t.setAmount("0.00005");
        t.setCurrency("BTC");
        t.setRecipient("3K531qHTY1MRaPCFgSyJrHcdckpFnamFC3");
        t.setOtp(OTP.generate());
        t.setTs(new Timestamp(System.currentTimeMillis()));
        t.setTransactionId("XXXXXXXX");
        
        Publish publish = new Publish();
        publish.publish(t);
        System.exit(0);
        
    }
}
