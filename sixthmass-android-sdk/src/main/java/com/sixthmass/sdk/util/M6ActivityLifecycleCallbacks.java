package com.sixthmass.sdk.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/*
  Created by Igor Rendulic on 5/11/17.
  
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

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
final class M6ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private Handler m6Handler = new Handler(Looper.getMainLooper());

    private boolean m6IsForeground = true;
    private boolean m6Paused = true;
    static final long BACKGROUND_DELAY = 500;
    Runnable m6BackgroundTransition = null;
    private M6ForegroundCallback foreground;

    M6ActivityLifecycleCallbacks(M6ForegroundCallback foreground) {
        this.foreground = foreground;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        m6Paused = true;

        if (m6BackgroundTransition != null) {
            m6Handler.removeCallbacks(m6BackgroundTransition);
        }

        m6Handler.postDelayed(m6BackgroundTransition = new Runnable() {
            @Override
            public void run() {
                if (m6IsForeground && m6Paused) {
                    m6IsForeground = false;
                }
            }
        }, BACKGROUND_DELAY);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        m6Paused = true;
        boolean wasInBackground = !m6IsForeground;
        m6IsForeground = true;

        if (m6IsForeground) {
            foreground.appInForeground();
            Log.i("SixthMass","SixtMass SDK APP is in foreground: " + m6IsForeground);
        }

        if (m6BackgroundTransition!= null) {
            m6Handler.removeCallbacks(m6BackgroundTransition);
        }

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    public boolean getIsInForeground() {
        return m6IsForeground;
    }

    public boolean isPaused() {
        return m6Paused;
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
}