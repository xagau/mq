package com.xagau.mq;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;

import com.rabbitmq.client.QueueingConsumer;
import com.xagau.atomiic.Coin;
import com.xagau.atomiic.Engine;
import com.xagau.atomiic.Instrument;
import com.xagau.atomiic.Log;
import com.xagau.atomiic.feed.Feed;
import com.xagau.atomiic.feed.GenericFeed;
import com.xagau.mq.reports.Action;
import com.xagau.mq.reports.Command;

import com.xagau.notification.Messenger;
import com.xagau.notification.WebhookDirectory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Address extends BasicMQ {
    public Address() {
        ensureConnection();
    }
    Engine engine = new Engine();

    public static void main(String[] args) {
        Log.info("WARNING: CONSIDER PURGE ALL TRANSACTIONS FROM QUEUE AFTER REBOOTING [E|F|G]");
        Log.info("++++=================================================================++++");

        Thread atomiicThread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    Address atomiic = new Address();
                    atomiic.process();
                    String wh = WebhookDirectory.lookup("discord-atomiic-messages");
                    Messenger.notify(wh, "Atomiic Restart", "Queue has been restarted for Atomiic");
                } catch (Exception ex) {
                    try {
                        Thread.sleep(30 * 1000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(Consume.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        };
        atomiicThread.start();

    }

    public Command receive() {
        Channel channel = null;

        try {

            ensureConnection();

            channel = connection.createChannel();

            String queue = "command-" + Server.getProperty("hostname");     //queue name
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
                    Command command = gson.fromJson(json, Command.class);

                    //Log.info(" [x] Received '" + new String(encrypted, "ISO-8859-1") + "'");
                    Log.info(" [x] Received '" + json + "'");

                    return command;

                } catch (Exception ex) {
                    Log.info(ex);
                } finally {
                    Utility.lag();
                }
            }
        } catch (Exception ex) {
            Log.info("ERROR: Receive failed:" + ex.getMessage());
        } finally {
            try {
                if( channel.isOpen() ) {
                    channel.close();
                }
            } catch(Exception ex) {}
            Utility.lag();
        }
        return null;
    }

    /*

    THIS WHOLE AREA NEEDS TO BE REFACTORED

     */

    public Command getNewAddress() {
        ensureConnection();
        Channel channel = null;
        try {
            channel = connection.createChannel();

            String queue = "getnewaddress-" + Server.getProperty("hostname");     //queue name
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
                    Log.info("Received command:" + json);
                    Gson gson = new Gson();
                    Command c = gson.fromJson(json, Command.class);

                    Coin coin = Engine.getCoinByName(c.getSymbol());
                    if( c.getName() != null && c.getName().equals("getbalancebyaddress")){
                        DecimalFormat df = new DecimalFormat("0.00000000");
                        String balance = df.format(coin.getBalanceByAddress(c.getParameter()));
                        Log.info("Sending back balance:" + balance);
                        c.setResult(balance);
                    }
                   return c;

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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
        return null;
    }

    public void send(Command c) {
        Channel channel = null;
        try {
            ensureConnection();

            channel = connection.createChannel();

            Command command = c;
            String queue = "command-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(command, Command.class);

            Log.info("Going to send back:" + json);
            try {
                channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            } catch(Exception ex) { }

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "command-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + json + "'");
            channel.close();

        } catch (Exception ex) {
            Log.info("ERROR: publish failed:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public void getNewAddress(Command c) {
        Channel channel = null;
        try {
            ensureConnection();

            channel = connection.createChannel();

            Command command = c;
            String queue = "getnewaddress-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(command);

            channel.queueDeclare(queue, durable, exclusive, autoDelete, null);

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "getnewaddress-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + json + "'");

            channel.close();

        } catch (Exception ex) {
            Log.info("ERROR: publish failed:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (Exception ex) {
            }
        }
    }

    public void getBalanceByAddress(Command c) {
        Channel channel = null;
        try {
            ensureConnection();

            channel = connection.createChannel();

            Command command = c;
            String queue = "getbalancebyaddress-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(command);

            channel.queueDeclare(queue, durable, exclusive, autoDelete, null);

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "getbalancebyaddress-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + json + "'");

            channel.close();

        } catch (Exception ex) {
            Log.info("ERROR: publish failed:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (Exception ex) {
            }
        }
    }

    public void process() {

        Channel channel = null;

        try {

            ensureConnection();

            channel = connection.createChannel();

            String queue = "atomiic-" + Server.getProperty("hostname");     //queue name
            Log.info("Atomiic from Queue:" + queue);
            String json = "";
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, true, consumer);
            Log.info("Waiting for inbound transaction:" + queue);

            boolean flag = true;
            Gson gson = new Gson();
            while (flag) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] encrypted = delivery.getBody();

                    json = new String(encrypted);
                    final Action t = gson.fromJson(json, Action.class);
                    Log.info("========================== WE GOT ONE =============================");
                    Log.info(" [x] Received '" + json + "'");

                    Thread swap = new Thread() {
                        boolean once = false;

                        public void run() {
                            try {

                                Log.info("Transaction starting for:" + t.getUuid());

                                DecimalFormat df = new DecimalFormat("0.00000000");
                                DecimalFormat pdf = new DecimalFormat("0.00");

                                Log.info("Looking up currency in:" + t.getCurrencyIn());
                                Coin c = Engine.getCoinByName(t.getCurrencyIn());
                                Log.info("Looking up currency out:" + t.getCurrencyOut());
                                Coin r = Engine.getCoinByName(t.getCurrencyOut());

                                // check that watch address does not already have a balance.
                                if( c.getBalanceByAddress(t.getWatchAddress(), 0) > 0){
                                    String msg = "Duplicate Transaction detected starting for:" + t.getUuid() + " Aborting!";
                                    Log.info(msg);
                                    tell("Transaction Aborted: Duplicate Watch Address", msg);
                                    return;
                                }

                                GenericFeed inFeed = Feed.getFeedByInstrument(new Instrument(c.getSymbol()));
                                GenericFeed outFeed = Feed.getFeedByInstrument(new Instrument(r.getSymbol()));
                                boolean weWait = true;
                                long snap = System.currentTimeMillis();
                                double step1 = ((t.getAmountIn() * inFeed.getPrice(1, "USD")) * c.getDiscountRate());
                                double step2 = outFeed.getPrice(1, "USD") * r.getDiscountRate(); // what is the value of a single value, including the discount rate of what is to go out?
                                double step3 = step1 / step2;

                                double amountToSend = step3;

                                long timeToWait = (snap + (30 * 60 * (60 * 1000))); // 30 minutes max for first confirmation.

                                while (weWait) {
                                    step1 = ((t.getAmountIn() * inFeed.getPrice(1, "USD")) * c.getDiscountRate());
                                    step2 = outFeed.getPrice(1, "USD") * ((1 - r.getDiscountRate()) + 1); // what is the value of a single value, including the discount rate of what is to go out?
                                    step3 = step1 / step2;

                                    if (step3 < amountToSend) {
                                        amountToSend = step3;
                                        Log.info("Adjusting amount to send to:" + df.format(amountToSend) + " " + r.getSymbol());
                                    }

                                    if (c.getBalanceByAddress(t.getWatchAddress(), c.getRequiredConfirmations()) >= t.getAmountIn()) {
                                        String txid = r.send(t.getSendToAddress(), amountToSend);
                                        String msg = " sent " + amountToSend + " (discounted at " + pdf.format(((1 - r.getDiscountRate())*100)) + "%) " + t.getCurrencyOut() + " to " + t.getSendToAddress() + " -> " + txid + " for " + df.format(t.getAmountIn()) + " of " + t.getCurrencyIn() + " UUID " + t.getUuid();
                                        Log.info(msg);
                                        weWait = false;
                                        try {
                                            if(txid == null || txid.equals("NA") || txid.equals("null") ) {
                                                tell("Transaction Failure", msg);
                                            } else {
                                                tell("Transaction Success", msg);
                                            }
                                        } catch(Exception ex){

                                        }
                                        return;
                                    } else {
                                        System.out.print(".");
                                    }
                                    if(!once && c.getBalanceByAddress(t.getWatchAddress(), 1) >= t.getAmountIn() ){
                                        // We have 1 confirmation = so lets extend the period.
                                        Log.info("Adjusting time to 2 hours as we have at least 1 confirmation once:" + once);
                                        timeToWait = (snap + (120 * 60 * (60 * 1000))); // 2 hours max time.
                                        once = true; // don't just keep extending more time.
                                    }
                                    // we will wait 1 hour max for confirmations before we abandon.
                                    if ( System.currentTimeMillis() > timeToWait ) {
                                        weWait = false; //quit process;
                                        String msg = "Transaction timeout for:" + t.getUuid();
                                        Log.info(msg);
                                        tell("Transaction Timeout", msg);
                                    }
                                    try {
                                        Thread.sleep(60000);
                                    } catch (Exception ex) {

                                    }
                                }
                            } catch (Exception ex) {
                                Log.info("ERROR:" + ex.getMessage());
                                Utility.lag();
                                ex.printStackTrace();
                            }
                        }

                    };
                    swap.start(); // fire and forget.

                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {

                }
            }
        } catch (Exception ex) {
            Log.info("ERROR: atomiic queue failed:" + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if( channel.isOpen() ) {
                    channel.close();
                }
            } catch (Exception ex) {
            }
            Utility.lag();
        }

    }

    public void tell(String subject, String msg)
    {
        try {
            String wh = WebhookDirectory.lookup("discord-atomiic-transactions");
            Messenger.notify(wh, subject, msg);
            wh = WebhookDirectory.lookup("discord-placeholder-atomiic");
            Messenger.notify(wh, subject, msg);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void publish(Action t) {
        Channel channel = null;
        try {
            ensureConnection();

            channel = connection.createChannel();

            String queue = "atomiic-" + Server.getProperty("hostname");     //queue name
            Log.info("Publish to Queue:" + queue);
            boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

            Gson gson = new Gson();

            // 2. Java object to JSON string
            String json = gson.toJson(t);


            channel.queueDeclare(queue, durable, exclusive, autoDelete, null);

            //AesGcmJce agjEncryption = new AesGcmJce(key.getBytes("ISO-8859-1"));
            //byte[] encrypted = agjEncryption.encrypt(json.getBytes("ISO-8859-1"), aad.getBytes("ISO-8859-1"));

            String exchangeName = "";
            String routingKey = "atomiic-" + Server.getProperty("hostname");
            Log.info("Routing Key:" + routingKey);
            channel.basicPublish(exchangeName, routingKey, null, json.getBytes());
            Log.info(" [x] Sent '" + json + "'");

            channel.close();
        } catch (Exception ex) {
            Log.info("ERROR: publish failed:" + ex.getMessage());
            Log.info(ex);
        } finally {
            try {
                if( channel.isOpen()) {
                    channel.close();
                }
                Utility.lag();
            } catch (Exception ex) {
            }
        }
    }
}
