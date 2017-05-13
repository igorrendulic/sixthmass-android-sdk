package com.sixthmass.sdk.util;
/*
  Created by Igor Rendulic on 5/13/17.
  
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

import android.util.Log;

import com.sixthmass.sdk.SixthMass;

import java.util.Date;
import java.util.UUID;

/**
 * Callback handler for when the app comes back in foreground (session reset)
 */
public final class M6ForegroundCallbackHandler implements M6ForegroundCallback {

    @Override
    public void appInForeground() {
        try {
            M6Config.instance().setSessionId(UUID.randomUUID().toString());
            M6Config.instance().setSessionStart(new Date().getTime());
            SixthMass.track(M6Util.EVENT_NAME_LAUNCH);
            Log.i("SixthMass", "App back in foreground. Initializing new session with id: " + M6Config.instance().getSessionId());
        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage());
        }

    }
}
