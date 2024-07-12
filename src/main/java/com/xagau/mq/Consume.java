/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.mq.vtm.Transaction;
import com.xagau.atomiic.Coin;
import com.xagau.atomiic.Engine;
import com.xagau.atomiic.Log;
import com.google.crypto.tink.subtle.AesGcmJce;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.xagau.notification.Messenger;
import com.xagau.notification.WebhookDirectory;

import java.security.Security;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean
 */
public class Consume extends BasicMQ {

    public Consume(boolean force, boolean force2){
        ensureConnection();
    }    

    public Consume(boolean force){
        ensureConnection();
    }    
    public Consume(){
        ensureConnection();
    }    
    
    // Requires refactoring.
    public void consume() {
        
        Channel channel = null;
        Publish publish = new Publish();
                            
        try {
           
            ensureConnection();
            
            channel = connection.createChannel();

            String queue = "transactions-" + Server.getProperty("hostname");     //queue name
            Log.info("Consume from Queue:" + queue);
            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            boolean flag = true;
            Gson gson = new Gson();
            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();
                    // Decryption
                    //AesGcmJce agjDecryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] decrypted = agjDecryption.decrypt(encrypted, aad.getBytes("ISO-8859-1"));

                    json = new String(encrypted);
                    Transaction t = gson.fromJson(json, Transaction.class);

                    Log.info(" [x] Received '" + json + "'");

                    try {

                        DecimalFormat df = new DecimalFormat("0.00000000");
                        Log.info("Looking up currency:" + t.getCurrency());
                        Coin c = Engine.getCoinByName(t.getCurrency());
                        Log.info("Found currency:" + t.getCurrency());
                        String str = Server.getProperty("bypass");
                        boolean bypass = true;
                        if( str == null ){
                            bypass = true;
                        } else {
                            bypass = Boolean.parseBoolean(str);
                        }
                        Log.info("Passed Bypass Check");

                        if( !OTP.isValid(t.getOtp(), bypass)) {
                            Log.info("Failed OTP check");
                            continue;
                        }
                        try {
                            double d = Double.parseDouble(t.getAmount());
                            double max = Globals.MAX_PAYOUT;
                            String userMax = Server.getProperty("max");
                            if( userMax != null ){
                                try {
                                    max = Double.parseDouble(userMax);
                                } catch(Exception ex) {
                                   Log.info("Max could not be parsed from server. Properties");
                                }
                            }
                            if( d > max ){
                                Log.info("Size too large:" + df.format(d));
                                continue;
                            }
                        } catch(Exception ex) {
                            Log.info("Size too large");
                        }
                        Log.info("Processing transaction send " + t.getAmount() + " to -> " + t.getRecipient());
                        String txid = c.send(t.getRecipient(), Double.parseDouble(t.getAmount()));
                        Log.info("TXID:" + txid);
                        try {
                            Receipt r = new Receipt();
                            r.setTransactionId(t.getTransactionId());
                            r.setAmount(Double.parseDouble(t.getAmount()));
                            r.setTerminalId(r.getTerminalId());
                            r.setTxid(txid);
                            r.setClientId(t.getClientId());
                            r.setCurrency(t.getCurrency());
                            r.setOtp(t.getOtp());
                            r.setBalance(c.getBalance());
                            r.setRecipient(t.getRecipient());
                            publish.publish(r);
                        } catch (Exception ex) {
                            Log.info(ex);
                            ex.printStackTrace();
                        }
                        Thread.sleep(1000 * 30);
                    } catch (Exception ex) {
                        Log.info("ERROR-CONSUME:" + ex.getMessage());
                        ex.printStackTrace();
                    }

                } catch (Exception ex) {
                    Log.info(ex);
                } finally {
                    Log.info("Consume transaction got here.");
                    Utility.lag();
                }
            }
        } catch (Exception ex) {
            Log.info("ERROR: Consume failed:" + ex.getMessage());
        } finally {
            try { 
                channel.close();
            } catch(Exception ex) {} 
            Utility.lag();
        }

    }

    public Receipt receive() {
        Channel channel = null;
        
        try {
            
            ensureConnection();
            
            channel = connection.createChannel();

            String queue = "receipts-" + Server.getProperty("hostname");     //queue name
            Log.info("Receive from Queue:" + queue);
            
            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);

            boolean flag = true;
            Gson gson = new Gson();
            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();
                    // Decryption
                    //AesGcmJce agjDecryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
                    //byte[] decrypted = agjDecryption.decrypt(encrypted, aad.getBytes("ISO-8859-1"));

                    json = new String(encrypted);
                    Receipt receipt = gson.fromJson(json, Receipt.class);

                    //Log.info(" [x] Received '" + new String(encrypted, "ISO-8859-1") + "'");
                    Log.info(" [x] Received '" + json + "'");

                    return receipt;

                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    Utility.lag();
                }
            }
        } catch (Exception ex) {
            Log.info("ERROR: Receive failed:" + ex.getMessage());
            Log.info(ex);
        } finally {
            try { 
                channel.close();
            } catch(Exception ex) {
                Log.info("Channel exception" + ex.getMessage());
            }
            Utility.lag();
        }
        return null;
    }
    
    public static void main(String[] args) throws Exception {

        Log.info("NOTICE: THIS FUNCTION WILL RUN THE CONSUMER AND REPORT [A|B|C|D|E|F|G]");
        Thread consumeThread = new Thread() { 
            public void run() { 
                try {
                    //Messenger.notify(new String[]{"lousedoan@gmail.com",  "yuriylee@hotmail.com", "bullionxpress@gmail.com"}, "Consumer Queue Started", "This Queue has started and is ready to start accepting transactions." );
                    String wh = WebhookDirectory.lookup("discord-atomiic-messages");
                    Messenger.notify(wh, "Consumer Queue Started", "Consumer Queue Started and is ready to start accepting transactions.");
                    Thread.sleep(100);
                    Consume consume = new Consume();
                    consume.consume();
                } catch(Exception ex) {
                    try {
                        String wh = WebhookDirectory.lookup("discord-atomiic-messages");
                        //Messenger.notify(wh, "Consumer Queue Failed", "This Queue has failed. Do not use send any transactions to this queue until it has been restarted and is ready to start accepting transactions.");
                        //Messenger.notify(new String[]{"lousedoan@gmail.com", "yuriylee@hotmail.com", "bullionxpress@gmail.com"}, "Consumer Queue Failed", "This Queue has failed. Do not use send any transactions to this queue until it has been restarted and is ready to start accepting transactions." );
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(Consume.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                } 
            }
        }; consumeThread.start();


        Thread reportThread = new Thread() { 
            public void run() { 
                while( true ) { 
                    try { 
                        Thread.sleep(100);
                        Report report = new Report();
                        report.process();
                    } catch(Exception ex) {
                        try {
                            Thread.sleep(30 * 1000);
                        } catch (InterruptedException ex1) {
                            Logger.getLogger(Consume.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    } 
                    
                }
            }
        }; reportThread.start();
        
    }
}
