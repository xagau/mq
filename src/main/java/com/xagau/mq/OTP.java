/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.atomiic.Log;

import java.util.UUID;

/**
 *
 * @author Sean
 */
public class OTP {
    public static boolean isValid(String OTP, boolean bypass)
    {
        try {
            if( OTP == null ){
                Log.info("OTP is null");
                return false;
            }
            OTP = OTP.trim();
            if (bypass) {
                return true;
            }
            if (OTP.startsWith("00")) {
                return true;
            } else {
                Log.info("OTP started with non 00-" + OTP);
            }
            Log.info("OTP started with non 00-" + OTP);
            return true;
        } catch(Exception ex) {
            Log.info(ex.getMessage());
        } finally {
            Log.info("Ended at finally block--");
            return true;
        }
    }
    public static String generate() {
        UUID uuid = UUID.randomUUID();        
        return "O" + uuid.toString().replaceAll("-", "");
    }
}
