package com.sixthmass.sdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

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
 * Utility class for SixthMass
 *
 * Contains various utility methods
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6Util {

    /**
     * Shared Preferences Storage name (namespace)
     */
    public static final String SHARED_PREFERENCES_NAME = "android.sixthmass.com";

    /**
     * Shared Preferences key for storing sent and unsent tasks
     */
    public static final String SHARED_PREFERENCES_TASK_KEY = "sixthmass_tasks_key";

    /**
     * Shared Preferences key for storing user id
     */
    public static final String SHARED_PREFERENCES_USER_ID = "sixthmass_userid_key";
    public static final String ENDPOINT_SINGLE_EVENT = "http://10.0.2.2:8079/v1/event"; //
    public static final String ENDPOINT_PROFILE = "http://10.0.2.2:8079/v1/profile"; //

//    /**
//     * Url for where to send events to
//     */
//    public static final String ENDPOINT_SINGLE_EVENT = "http://events.sixthmass.com/v1/event";
//
//    /**
//     * Url for where to send user profile to
//     */
//    public static final String ENDPOINT_PROFILE = "http://events.sixthmass.com/v1/profile";

    /**
     * Default event name for profile update
     */
    public static final String EVENT_NAME_PROFILE = "zr_profile_update";

    /**
     * Default event name for new user registration event
     */
    public static final String EVENT_NAME_REGISTER = "zr_register";

    /**
     * Default event name for purchasing action
     */
    public static final String EVENT_NAME_PURCHASE = "zr_purchase";

    /**
     * Default event name for application launch
     */
    public static final String EVENT_NAME_LAUNCH = "zr_launch";

    /**
     * Method for serializing list of tasks and encoding them to base64
     *
     * Prepares list of tasks to be stored in Shared Preferences
     *
     * @param list list of tasks/events
     * @return serialized input list string
     * @throws IOException Exception with serialization
     */
    public static String serialize(final List<M6Task> list) throws IOException {

        ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(serialObj);

        try {

            objStream.writeObject(list);
            objStream.flush();
            return M6Util.encodeBytes(serialObj.toByteArray());

        } finally {
            objStream.close();
            serialObj.close();
        }
    }

    /**
     * Deserializing from serialized list ob tasks
     *
     * Used from retrieved object from Shared Preferences
     *
     * @param taskListString serialized list of tasks
     * @return List of M6Task objects
     */
    public static List<M6Task> deserialize(String taskListString) {
        if (taskListString == null || taskListString.length() == 0) return null;
        try {
            ByteArrayInputStream serialObj = new ByteArrayInputStream(M6Util.decodeBytes(taskListString));
            ObjectInputStream objStream = new ObjectInputStream(serialObj);
            return (List<M6Task>)objStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Base64 Encode of byte representation
     *
     * @param bytes input bytes
     * @return string
     */
    public static String encodeBytes(byte[] bytes) {
        StringBuffer strBuf = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

    /**
     * Decoding base64 from string to byte
     * @param str Encoded string
     * @return byte object
     */
    public static byte[] decodeBytes(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length(); i+=2) {
            char c = str.charAt(i);
            bytes[i/2] = (byte) ((c - 'a') << 4);
            c = str.charAt(i+1);
            bytes[i/2] += (c - 'a');
        }
        return bytes;
    }

    /**
     * Validation in case user created new SixthMassUserProfile and has no userId or clientId setup
     *
     * @param profile user profile
     * @param config sdk configuration
     * @return user profile
     */
    public static synchronized SixthMassUserProfile augmentProfile(SixthMassUserProfile profile, M6Config config) {

        if (profile.getUserId() == null) {
            SharedPreferences preferences = config.getContext().getSharedPreferences(M6Util.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            String userId = preferences.getString(M6Util.SHARED_PREFERENCES_USER_ID, null);
            if (userId == null) {
                profile.setUserId(UUID.randomUUID().toString());
            }
            if (profile.getClientId() == null) {
                profile.setClientId(config.getToken());
            }
        }
        return profile;
    }


    /**
     * Either getting user from already initialized config
     * or generating new user profile and storing userId in shared preferences and sessionId in M6Config
     *
     * @return SixthMassUserProfile
     */
    static synchronized SixthMassUserProfile getUserProfile(Context context) {

        // first check if user profile already initialized
        SixthMassUserProfile profile = null;
        M6Config config = null;
        try {
            config = M6Config.instance();
            profile = config.getUserProfile();
        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage(), e);
            return null;
        }

        // if profile not in config then create new object with existing userId and new sessionId
        // or use previously stored userId
        SharedPreferences preferences = context.getSharedPreferences(M6Util.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (profile == null) {
            profile = new SixthMassUserProfile();

            String userId = preferences.getString(M6Util.SHARED_PREFERENCES_USER_ID, null);
            // if user == null then user was never seen before
            if (userId == null) {
                userId = UUID.randomUUID().toString();
            }
            profile.setUserId(userId);
            profile.setClientId(config.getToken());
            profile.setDeviceUUID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            profile.setTzOffset(TimeZone.getDefault().getOffset(new Date().getTime()) / 1000 / 60);
            preferences.edit().putString(M6Util.SHARED_PREFERENCES_USER_ID, userId).commit();

            // setting random uuid for session only when version less than ice cream sanwich (otherwise M6ActivityLivecycleCallbacks are supported
            // and they will fire session restart

            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                config.setSessionId(UUID.randomUUID().toString());
                config.setSessionStart(new Date().getTime());
            }
        }

        return profile;
    }

    /**
     * Reading the task list from Shared Preferences
     *
     * TaskList is serialized and encoded with Base64 then stored
     *
     * @param context Android Context used to access Shared Preferences
     * @return list of tasks
     */
    static List<M6Task> readTaskList(Context context) {
        List<M6Task> list = null;
        SharedPreferences prefReader = context.getSharedPreferences(M6Util.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String taskListString = prefReader.getString(M6Util.SHARED_PREFERENCES_TASK_KEY, null);
        try {
            if (taskListString != null) {
                list = M6Util.deserialize(taskListString);
            }
        } catch (Exception e){
            Log.e("SixtMass retrieve: ", e.getMessage(), e);
            list = Collections.synchronizedList(new ArrayList<M6Task>());
        }
        return list;
    }

    /**
     * Saving task list to Shared Preferences
     * TaskList is deserialized from base64 to it's original object format
     *
     * @param list list of tasks
     * @param context Android context used to access Shared Preferences
     */
    static void saveTaskList(List<M6Task> list, Context context) {
        if (list != null) {
            SharedPreferences prefWriter = context.getSharedPreferences(M6Util.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            try {
                String taskList = M6Util.serialize(list);
                prefWriter.edit().putString(M6Util.SHARED_PREFERENCES_TASK_KEY, taskList).commit();
            } catch (IOException e) {
                Log.e("SixthMass error:", e.getMessage(), e);
            }
        }
    }

    /**
     * * Method converts HttpResponse to string by reading input stream
     *
     * @param is input stream
     * @return string
     * @throws IOException Exception reading server response
     */
    private static String readInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder(is.available());
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Augmenting the event task with purchase items
     *
     * @param task task
     * @param items list of purchased items
     * @return M6Task
     */
    public static M6Task augmentWithPurchase(M6Task task, List<SixthMassItem> items) {

        if (items != null) {
            for (SixthMassItem item : items) {
                M6Item m6item = new M6Item(item);
                task.addPurchasedItem(m6item);
            }
        }

        return task;
    }

    /**
     * Post single event to SixthMass server
     *
     * TODO: extends to list of events (or maybe JSONObject is good of an object to do that?)
     *
     * @param endpoint endpint of the service
     * @param payload json payload to send
     * @return string response from the server
     * @throws IOException Exception when send fails
     */
    static String httpPost(String endpoint, JSONObject payload) throws IOException {

        // non-progressive backoff (3 times)
        HttpURLConnection connection = null;
//        OutputStream out = null;
        InputStream in = null;
//        BufferedOutputStream bout = null;
        String response = null;

        int retry = 0;
        while (retry < 3) {
            try {
                final URL url = new URL(endpoint);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                byte[] bytes = payload.toString().getBytes("UTF-8");
                connection.setFixedLengthStreamingMode(bytes.length);
                BufferedOutputStream bout = new BufferedOutputStream(connection.getOutputStream());
                bout.write(bytes);
                bout.flush();
                bout.close();
                bout = null;

                InputStream errorStr = connection.getErrorStream();
                if (errorStr != null) {
                    String error = readInputStream(errorStr);
                    Log.e("SixthMass", error);
                    errorStr.close();
                    throw new IOException("Error Sending event: " + error);
                }

                in = connection.getInputStream();

                int responseCode  = connection.getResponseCode();
                if (responseCode < 200 || responseCode > 300) {
                    throw new IOException("Error code: " + connection.getResponseCode());
                }
                response = readInputStream(in);
                in.close();
                in = null;

                break; // exit loop on succesfull call
            } catch (SocketException e) {
                Log.e("SixthMass", "Network error: " + e.getMessage());
            } catch (final IOException e) {
                Log.e("SixthMass", e.getMessage());
                retry++;
            }  finally {
                if (null != connection)
                    connection.disconnect();


                retry+=1;
            }
        }
        return response;
    }
}
