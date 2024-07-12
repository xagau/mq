package com.xagau.mq.reports;

import java.util.UUID;

public class Action {
    final private UUID uuid = UUID.randomUUID();
    private String watchAddress = null;
    private double amountIn;
    private long ts;
    private String sendToAddress;
    private String currencyIn;
    private String currencyOut;
    private boolean paid = false;

    private String request = null;


    public String getWatchAddress() {
        return watchAddress;
    }

    public void setWatchAddress(String watchAddress) {
        this.watchAddress = watchAddress;
    }

    public double getAmountIn() {
        return amountIn;
    }

    public void setAmountIn(double amountIn) {
        this.amountIn = amountIn;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getCurrencyIn() {
        return currencyIn;
    }

    public void setCurrencyIn(String currencyIn) {
        this.currencyIn = currencyIn;
    }

    public String getCurrencyOut() {
        return currencyOut;
    }

    public void setCurrencyOut(String currencyOut) {
        this.currencyOut = currencyOut;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getSendToAddress() {
        return sendToAddress;
    }

    public void setSendToAddress(String sendToAddress) {
        this.sendToAddress = sendToAddress;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
