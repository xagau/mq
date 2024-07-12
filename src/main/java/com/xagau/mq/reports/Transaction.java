/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq.reports;


public class Transaction {

    private double amount = 0;
    private int confirmations;
    private double fee = 0;
    private long time;
    private long blocktime;
    private String txid;
    private String address;
    private int blockheight;

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return the confirmations
     */
    public int getConfirmations() {
        return confirmations;
    }

    /**
     * @param confirmations the confirmations to set
     */
    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    /**
     * @return the fee
     */
    public double getFee() {
        return fee;
    }

    /**
     * @param fee the fee to set
     */
    public void setFee(double fee) {
        this.fee = fee;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @return the blocktime
     */
    public long getBlocktime() {
        return blocktime;
    }

    /**
     * @param blocktime the blocktime to set
     */
    public void setBlocktime(long blocktime) {
        this.blocktime = blocktime;
    }

    /**
     * @return the txid
     */
    public String getTxid() {
        return txid;
    }

    /**
     * @param txid the txid to set
     */
    public void setTxid(String txid) {
        this.txid = txid;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the blockheight
     */
    public int getBlockheight() {
        return blockheight;
    }

    /**
     * @param blockheight the blockheight to set
     */
    public void setBlockheight(int blockheight) {
        this.blockheight = blockheight;
    }

}