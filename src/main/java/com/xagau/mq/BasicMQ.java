/*

* To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;
import com.xagau.atomiic.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean
 */
public class BasicMQ {
    static Connection connection = null;
    
    public void establishConnection()
    {
        try {

            Log.info("Reestablishing cloud AMQP connection [E|F|G]");
            boolean connected = false;
            if( isConnectionDead() ){
                try {
                    connection.clearBlockedListeners();
                    connection.close();
                } catch(Exception ex) {

                } finally {
                    connection = null;
                }
            }
            while( !connected ) {
                String uri = System.getenv("CLOUDAMQP_URL");
                if (uri == null)
                    uri = Server.getProperty("cloudamqp_url");

                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri(uri);
                factory.setRequestedHeartbeat(30);
                factory.setConnectionTimeout(60000);
                factory.setAutomaticRecoveryEnabled(true);
                factory.setNetworkRecoveryInterval(10000);
                factory.setTopologyRecoveryEnabled( true );
                connection = factory.newConnection();
                if(!isConnectionDead()){
                    connected = true;
                }
            }

        } catch (URISyntaxException ex) {
            Logger.getLogger(Publish.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Publish.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (KeyManagementException ex) {
            Logger.getLogger(Publish.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(Publish.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch(Exception ex) { 
            ex.printStackTrace();
        }
    
    }
    
    public boolean isConnectionDead()
    {
        if ( connection == null ){
            return true;
        }
        if( !connection.isOpen() ){
            return true;
        }
        try {
            ShutdownSignalException sig = connection.getCloseReason();
            if( sig != null ){
                return false;
            }
        } catch(Exception ex) {
            return false;
        }

        return false;        
    }
    
    public void ensureConnection()
    {
        if( isConnectionDead() ){
            if( connection != null ) { try { connection.close(); } catch(Exception ex) { } }
            establishConnection();
        }
    }
}


