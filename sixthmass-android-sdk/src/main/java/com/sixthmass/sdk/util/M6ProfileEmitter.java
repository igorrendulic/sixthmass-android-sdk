package com.sixthmass.sdk.util;
/*
  Created by Igor Rendulic on 4/27/17.
  
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

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * Workers sending updates of user profile to SixthMass servers
 *
 * Simple Worker task retrieving profile update tasks from queue and sending them to server
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6ProfileEmitter implements Runnable {

    /**
     * Thread-safe queue
     */
    private ConcurrentLinkedQueue<SixthMassUserProfile> queue;

    /**
     * Configuration object
     */
    private M6Config config;

    /**
     * Constructor for this runnable
     *
     * Checks it SDK was initialized and sets the queue and config for this runnable
     *
     * @param queue ConcurrentLinkedQueue
     * @param config SDK configuration
     * @throws SixthMassException Exception when SDK not initailized first
     */
    public M6ProfileEmitter(ConcurrentLinkedQueue<SixthMassUserProfile>  queue, M6Config config) throws SixthMassException {
        if (queue == null || config == null) {
            throw new SixthMassException("SixthMass SDK Not initialized");
        }
        if (config.getToken() == null || config.getToken() == null) {
            throw new SixthMassException("SixthMass SDK Not Properly initialized. Context and Token can not be null");
        }

        this.queue = queue;
        this.config = config;
    }

    @Override
    public void run() {

        try {
            while (!this.queue.isEmpty()) {
                SixthMassUserProfile profile = this.queue.peek();
                M6Util.httpPost(M6Util.ENDPOINT_PROFILE, profile.toJson());
                this.queue.poll(); // remove from queue if sending succesfull

                Log.i("SixthMass", profile.toJson().toString(1));
            }
        } catch (Exception e) {
            Log.e("SixthMass",e.getMessage(), e);
        }
    }
}
