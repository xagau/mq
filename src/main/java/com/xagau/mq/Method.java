/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author Sean
 */
public class Method {

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    private UUID uuid = UUID.randomUUID();
    private String target = null;
    private String signature = null;
    private ArrayList<String> arguments = new ArrayList<String>();

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return the arguments
     */
    public ArrayList<String> getArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(ArrayList<String> arguments) {
        this.arguments = arguments;
    }
}
