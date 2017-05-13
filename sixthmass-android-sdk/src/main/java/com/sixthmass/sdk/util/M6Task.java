package com.sixthmass.sdk.util;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/*
  Created by Igor Rendulic on 4/24/17.

  Copyright 2017

  Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/**
 * Task object holding single event values
 *
 * This object is for internals of SixthMass SDK and should not be modified.
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6Task implements Serializable {

    /**
     * Name of the event
     */
    public String eventName;

    /**
     * Previous event name
     */
    public String previousEvent;

    /**
     * Miliseconds since epoch at the time of the event
     */
    public Long timestamp;

    /**
     * Miliseconds since epoch at the time of the previous event
     */
    public Long previousTimestamp;

    /**
     * UUID of the current user
     */
    private String userId;

    /**
     * Current session id
     */
    private String sessionId;

    /**
     * Client ID or Token
     */
    private String clientId;

    /**
     * Library version (SDK internal version)
     */
    private String libVer; // SixthMass library version

    /**
     * Timezone in which SDK was initialized
     */
    private String timezone;

    /**
     * Always Android for this SDK
     */
    private String os;

    /**
     * Timezone offset in minutes
     */
    private Integer tzOffset;

    /**
     * Browser - Build.MANUFACTURER
     */
    private String browser;

    /**
     * Androids  Build.VERSION.RELEASE
     */
    private String browserVersion;

    /**
     * Android Build.MODEL
     */
    private String device;

    /**
     * Locale.getDisplayLanguage()
     */
    private String language;

    /**
     * Not used in this version but meant to be info on where the user came from
     */
    private String refDomain; //TODO: where did user come from

    /**
     * Duration of the current session
     */
    private Double sessionDuration;

    /**
     * List of purchased items
     */
    private List<M6Item> purchasedItems; //TODO: add support

    /**
     * Custom event properties
     */
    private Map<String,String> properties;

    /**
     * Internal variable for marking events which have been sent to the server
     */
    protected boolean sent;

    /**
     * Init method for this object
     *
     * Initialized this object and augments it with android device info and language
     *
     * @param name name of the event
     * @param properties custom properties for this event
     * @throws SixthMassException Exception when SDK not initailized first
     */
    public M6Task(String name, Map<String,String> properties) throws SixthMassException {
        this.eventName = name;
        this.properties = properties;
        this.timestamp = new Date().getTime();
        augmentWithDeviceInfo();
    }

    /**
     * Augmenting current event with previous event (previous event name and timestamp)
     *
     * @param previousTask previous task
     */
    public void augmentTask(M6Task previousTask) {
        if (previousTask != null) {
            this.previousEvent = previousTask.eventName;
            this.previousTimestamp = previousTask.timestamp;
        }
    }

    /**
     * Adding information to the object such as current device manufacturer, language, ...
     *
     * @throws SixthMassException Exception when SDK not initailized first
     */
    private void augmentWithDeviceInfo() throws SixthMassException {
        M6Config config = M6Config.instance();
        this.clientId = config.getToken();
        this.libVer = config.getLibVersion();
        TimeZone tz = TimeZone.getDefault();
        this.timezone = tz.getID();
        this.tzOffset = Math.round(tz.getOffset(new Date().getTime()) / 1000 / 60);
        this.os = "Android";
        this.browserVersion = Build.VERSION.RELEASE == null ? null : Build.VERSION.RELEASE;
        this.browser = Build.MANUFACTURER == null ? null : Build.MANUFACTURER;
        this.device = Build.MODEL;
        this.language = Locale.getDefault().getDisplayLanguage();
        if (config.getUserProfile() == null) {
            throw new SixthMassException("User profile not instantiated!");
        }
        this.userId = config.getUserProfile().getUserId();
        this.sessionId = config.getSessionId();
        this.sessionDuration = Double.parseDouble( ( (new Date().getTime() - config.getSessionStart()) / 1000) + "");
    }

    /**
     * Returning purchased items from task
     *
     * @return M6Item
     */
    protected List<M6Item> getPurchasedItems() {
        return this.purchasedItems;
    }


    /**
     * Creates an array if needed and adds all items to the task
     *
     * @param items list of purchased items
     */
    protected void setPurchasedItems(List<M6Item> items) {
        if (this.purchasedItems == null) {
            this.purchasedItems = new ArrayList<>();
        }
        purchasedItems.addAll(items);
    }

    /**
     * Adding purchase items one by one to task
     *
     * @param item M6item
     */
    protected void addPurchasedItem(M6Item item) {
        if (this.purchasedItems == null) {
            this.purchasedItems = new ArrayList<>();
        }
        this.purchasedItems.add(item);
    }

    /**
     * Convert to json compatible with sixthmass servers
     *
     * @return json representation of this object
     * @throws JSONException Exception when conversion failed
     */
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("e", this.eventName);
        json.put("p", this.previousEvent);
        json.put("ts", this.timestamp);
        json.put("pTs", this.previousTimestamp);
        json.put("uId", this.userId);
        json.put("cId", this.clientId);
        json.put("sId", this.sessionId);
        json.put("sessDuration", this.sessionDuration);
        json.put("timezone", this.timezone);
        json.put("tzOffset", this.tzOffset);
        json.put("libVer", this.libVer);
        json.put("refDomain", this.refDomain);
        json.put("browser", this.browser);
        json.put("browserVersion", this.browserVersion);
        json.put("searchQuery", null);
        json.put("os", this.os);
        json.put("device", this.device);
        json.put("lang", this.language);
        if (this.properties != null) {
            JSONObject customProperties = new JSONObject(this.properties);
            json.put("customValues", customProperties);
        }
        JSONArray purchaseItemsArray = null;
        if (this.purchasedItems != null) {
            purchaseItemsArray = new JSONArray();
            for (M6Item item : this.purchasedItems) {
                purchaseItemsArray.put(item.toJson());
            }
        }
        if (purchaseItemsArray != null) {
            json.put("pItems", purchaseItemsArray);
        }

        return json;
    }
}
