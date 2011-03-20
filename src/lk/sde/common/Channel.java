/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lk.sde.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author macadmin
 */
public class Channel {
    private String owner;
    private String channelName;
    private List<String> subscriberList;

    public Channel(){
        subscriberList = new ArrayList<String>();
    }

    public Channel(String owner, String channelName){
        this.owner = owner;
        this.channelName = channelName;
        subscriberList = new ArrayList<String>();
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSubscriberList() {
        return subscriberList;
    }

    public void setSubscriberList(List<String> subscriberList) {
        this.subscriberList = subscriberList;
    }

    
}
