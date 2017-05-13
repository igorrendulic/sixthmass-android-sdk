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

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Configuration properties
 *
 * This object holds all configuration options for the SDK. It's singleton so only 1 instance exists
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6Config {

    /**
     * Library version
     */
    private static final String LIB_VERSION = "android_0.0.3";

    /**
     * This object instance
     */
    private static M6Config me;

    /**
     * Android Context
     */
    private Context context;

    /**
     * ClientId or Token defined by SixthMass platform
     */
    private String token;

    /**
     * Library version
     */
    private String libVersion;

    /**
     * User Profile as defined by this SDK and augmented by user
     */
    private SixthMassUserProfile userProfile;

    /**
     * Time of the session start
     */
    private long sessionStart;

    /**
     * UUID of the current session
     */
    private String sessionId;

    /**
     * Activity callback tracking if app is in foreground or background
     */
    private M6ActivityLifecycleCallbacks sixthMassActivityCallbacks;

    /**
     * Instance method that creates a singleton with initial parameters
     *
     * @param context Android Context
     * @param token ClientId or Token defined by SixthMass platform
     * @return Singleton object
     */
    public synchronized static M6Config instance(Context context, String token) {
        if (me == null) {
            me = new M6Config(context,token);
        }
        me.userProfile = M6Util.getUserProfile(context);
        return me;
    }

    /**
     * Instance method used to retrieve the configuration
     *
     * @return This object
     * @throws SixthMassException Exception when SDK not initailized first
     */
    public static synchronized M6Config instance() throws SixthMassException {
        if (me == null) {
            throw new SixthMassException("Config not initialized");
        }
        return me;
    }

    /**
     * Contructor and initializer with Android Context and ClientId (token)
     *
     * @param context Android Context
     * @param token Client Id
     */
    private M6Config(Context context, String token) {
        this.context = context;
        this.token = token;
        this.libVersion = LIB_VERSION;

        this.sixthMassActivityCallbacks = registerSixthMassActivityLifecycleCallbacks(context);

    }

    /**
     * Register activity callbacks for SixthMass SDK (only for versions equal or higher to ice cream sandwich
     *
     * @param context application context
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    M6ActivityLifecycleCallbacks registerSixthMassActivityLifecycleCallbacks(Context context) {

        M6ActivityLifecycleCallbacks sixthMassCallbacks = null;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            if (context.getApplicationContext() instanceof Application) {
                final Application app = (Application) context.getApplicationContext();
                sixthMassCallbacks = new M6ActivityLifecycleCallbacks(new M6ForegroundCallbackHandler());
                app.registerActivityLifecycleCallbacks(sixthMassCallbacks);
            } else {
                Log.i("SixthMass","Context not in Application. Activity Lifecycle callbacks not supported in this OS");
            }
        }
        return sixthMassCallbacks;
    }

    /**
     * User profile is by default generated on init
     *
     * This user profile object can be manipulated by SDK user
     *
     * @return SixthMassUserProfile
     */
    public SixthMassUserProfile getUserProfile() {
        return this.userProfile;
    }

    protected Context getContext() {
        return this.context;
    }

    String getToken() {
        return this.token;
    }

    String getLibVersion() {
        return this.libVersion;
    }

    void setSessionStart(long timestamp) {
        this.sessionStart = timestamp;
    }

    long getSessionStart() {
        return this.sessionStart;
    }

    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    String getSessionId() {
        return this.sessionId;
    }
}
