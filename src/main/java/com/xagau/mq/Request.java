/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.mq.reports.Action;

import java.util.UUID;

/**
 *
 * @author Sean
 */
public class Request {

    public Request() {
        uuid = UUID.randomUUID();
    }
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
    private UUID uuid = null;
    private String page;
    private Action action;

    /**
     * @return the page
     */
    public String getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(String page) {
        this.page = page;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Action getAction()
    {
        return action;
    }
    
}
