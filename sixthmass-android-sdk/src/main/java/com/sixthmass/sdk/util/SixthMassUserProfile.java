package com.sixthmass.sdk.util;
/*
  Created by Igor Rendulic on 4/26/17.
  
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * User profile
 *
 * Currently it can not be retrieved from server.
 *
 * <p>
 *     Only the populated fields will be updated in the analytics.
 *     E.g. if in one session name is set and in another the name is not set it will not be lost after seconds update
 *     This enables users to set the values one by one and none of them will be lost.
 *     In case when the property is modified the latest pushed modified version of that property will be stored in the analytics
 * </p>
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class SixthMassUserProfile {
    /**
     * Custom userId
     */
    private String remoteUserId;

    /**
     * Email
     */
    private String email;

    /**
     * First name
     */
    private String firstName;

    /**
     * Last name
     */
    private String lastName;

    /**
     * Gender (custom value)
     */
    private String gender;

    /**
     * Name fo the business, e.g. Business inc.
     */
    private String businessName;

    /**
     * Date of the birthday
     */
    private Date birthday;

    /**
     * Custom values to be stored with user profile
     */
    private Map<String,String> customValues;

    /**
     * ClientId or Token defined by SixthMass analytics
     *
     * This property is defined on SDK init
     */
    private String clientId;

    /**
     * Internal UserId of the user
     *
     * This property is defined on SDK init
     */
    private String userId;

    /**
     * Internet property
     *
     * Latest timezone offset in hours
     */
    private Integer tzOffset;

    /**
     * Internal property: Android push token
     */
    private String androidPushToken;

    /**
     * Internal property: deviceUUID (for iOs)
     */
    private String deviceUUID;

    /**
     * Internal property set when business name not null
     */
    private Boolean isBusiness;

    public String getRemoteUserId() {
        return remoteUserId;
    }

    public void setRemoteUserId(String remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        if (businessName != null) {
            this.isBusiness = true;
        } else {
            this.isBusiness = false;
        }
        this.businessName = businessName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Map<String, String> getCustomValues() {
        return customValues;
    }

    public void setCustomValues(Map<String, String> customValues) {
        this.customValues = customValues;
    }

    /**
     * Protected section
     */
    protected SixthMassUserProfile() {
    }

    // protected setters
    protected void setUserId(String userId) {
        this.userId = userId;
    }

    protected String getUserId() {
        return this.userId;
    }

    protected  void setClientId(String clientId) {
        this.clientId = clientId;
    }

    protected String getClientId() {
        return this.clientId;
    }

    protected void setTzOffset(Integer offset) {
        this.tzOffset = offset;
    }

    protected void setAndroidPushToken(String pushToken) {
        this.androidPushToken = pushToken;
    }

    protected void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    protected void setIsBusiness(boolean isBusiness) {
        this.isBusiness = isBusiness;
    }

    /**
     * Convert object to required JSON format
     *
     * @return json object
     * @throws JSONException Exception when conversion failed
     */
    protected JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("clientId", this.clientId);
        json.put("userId", this.userId);
        json.put("remoteUserId", this.remoteUserId);
        json.put("email", this.email);
        json.put("firstName", this.firstName);
        json.put("lastName", this.lastName);
        json.put("tzOffset", this.tzOffset);
        json.put("androidPushToken", this.androidPushToken);
        json.put("deviceUUID", this.deviceUUID);
        json.put("gender", this.gender);
        json.put("isBusiness", this.isBusiness);
        json.put("businessName", this.businessName);

        if (this.birthday != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            json.put("birthday", df.format(this.birthday));
        }

        if (this.customValues != null) {
            JSONObject customProperties = new JSONObject(this.customValues);
            json.put("customValues", customProperties);
        }

        return json;
    }
}
