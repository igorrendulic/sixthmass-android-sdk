package com.sixthmass.sdk;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.sixthmass.sdk.util.M6Config;
import com.sixthmass.sdk.util.M6ProfileEmitter;
import com.sixthmass.sdk.util.M6Task;
import com.sixthmass.sdk.util.M6TaskEmitter;
import com.sixthmass.sdk.util.M6Util;
import com.sixthmass.sdk.util.SixthMassException;
import com.sixthmass.sdk.util.SixthMassItem;
import com.sixthmass.sdk.util.SixthMassUserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

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
 * SixthMass works only on one consumer thread - M6TaskEmitter
 * SixthMass is thread safe. It works in offline mode as with server problems
 * It sends events one by one as they come in unless they can't be saved
 * It also handles a burst of events, queuing them up.
 * Each event gets it's own worker in a non blocking application manner
 *
 * TODO: - custom logging (into separate file) if log "large" send the log somewhere on server?
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
*/
public final class SixthMass {

    //private static M6Config config;

    /**
     * FIFO runnable queue for adding new workers
     */
    private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    /**
     * simple task list (which is serialized to persistent state
     * (covering situations as server not available at the moment, app offline, ...)
     */
    private static List<M6Task> taskList = Collections.synchronizedList(new ArrayList<M6Task>());

    /**
     * no need to have more than 1 thread sending events
     */
    private static ExecutorService poolExecutor = Executors.newSingleThreadExecutor();

    /**
     * 5 threads should be enough for profile updates (parallel execution possible, overriding the profile changes also possible)
     * SDK user has to take into consideration in hers/his own code not to overwrite hers/his own object values
     */
    private static ExecutorService poolProfileExecutor = Executors.newFixedThreadPool(5);

    /**
     * queue holding tasks to be sent to SixthMass event server
     */
    private static ConcurrentLinkedQueue<M6Task> taskQueue = new ConcurrentLinkedQueue<>();

    /**
     * Profile queue holding profile updates to be sent to SixthMass servers
     */
    private static ConcurrentLinkedQueue<SixthMassUserProfile> profileQueue = new ConcurrentLinkedQueue<>();

    /**
     * Initialization of SDK
     *
     * Required as first action before any other SDK calls in ActivityMain
     *
     * @param context Android context (most commonly referred as "this" in ActivityMain)
     * @param token clientId defined by SixthMass analytics
     */
    public static void init(Context context, String token) {
        M6Config.instance(context,token);

        // the start event is handled only in cases when ActivityCallbacks not supported
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            track(M6Util.EVENT_NAME_LAUNCH);
        }
    }

    /**
     * Non-blocking update of user profile
     *
     * SixthMassUserProfile object should not be created by itself. It should always accessed as SixtMass.getProfile()
     *
     * In case SixthMassUserProfile created as a new object SixthMass does it's best not to assign it as new user profile
     * but tries to identify the user as existing one. No guarantees.
     *
     * Only non-null values are updated in SixthMass analytics thus preserving non set old properties of the user profile object
     *
     * <p>
     *     SixthMassSDK must be initialized first
     * </p>
     *
     * @param profile Assigned user profile
     * @param customProperties Optional
     */
    public static void profileUpdate(SixthMassUserProfile profile, Map<String,String> customProperties) {

        try {

            profile = M6Util.augmentProfile(profile, M6Config.instance());

            profileQueue.add(profile);

            M6ProfileEmitter emit = new M6ProfileEmitter(profileQueue, M6Config.instance());

            Future<?> future = poolProfileExecutor.submit(emit);
            while (!future.isDone() && !future.isCancelled()) { // TODO: remove later?
            }

        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage(), e);
        }
    }

    /**
     * Convinient method for profile update without custom properties
     *
     * @param profile Users profile
     */
    public static void profileUpdate(SixthMassUserProfile profile) {
        profileUpdate(profile,null);
    }

    /**
     * Registration on the SixthMass at the point of user registration.
     *
     * This is useful when a user can be anonymous for some time before she/he becomes a registered user
     *
     * <p>
     *     SixthMassSDK must be initialized first
     * </p>
     *
     * @param profile Users profile
     * @param customProperties Optional
     */
    public static void register(SixthMassUserProfile profile, Map<String,String> customProperties) {
        profileUpdate(profile,customProperties);
        track(M6Util.EVENT_NAME_REGISTER, customProperties);
    }

    /**
     * Tracking of purchased items
     *
     * <p>
     *     SixthMassSDK must be initialized first
     * </p>
     *
     * @param items List of purchased items
     * @param customProperties Optional additional purchase properties
     */
    public static void purchase(List<SixthMassItem> items, Map<String,String> customProperties) {

        try {
            M6Task task = new M6Task(M6Util.EVENT_NAME_PURCHASE, customProperties);

            // adding purchase data to task
            task = M6Util.augmentWithPurchase(task,items);

            track(task);

        } catch (SixthMassException e) {
            Log.e("SixtMass", e.getMessage(),e);
        }
    }

    /**
     * Convenient method for purchase without custom properties
     *
     * @param items list of purchased items
     */
    public static void purchase(List<SixthMassItem> items) {
        purchase(items,null);
    }

    /*
     * Event tracking method with custom properties
     * <p>
     *     SixthMassSDK must be initialized first
     * </p>
     *
     * @param eventName Name of the event to track
     * @param customProperties Optional
     */
    public static void track(String eventName, Map<String,String> customProperties) {

        // creating and augmenting task to be sent to sixthmass server
        // the constructor augments the task with user data, session id, device, operating system and other android specifics
        try {
            M6Task task = new M6Task(eventName, customProperties);
            track(task);
        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage(), e);
        }
    }

    /**
     * Convenient method for event tracking without custom properties
     *
     * @param eventName Name of the event to track
     */
    public static void track(String eventName) {
        track(eventName, null);
    }

    /**
     * Returns auto generated user profile or null if SDK not initialized in MainActivity in method protected void onCreate(Bundle savedInstanceState)
     *
     * It can me modified as desired. After updating SixtMassUserProfile object profileUpdate function must be called
     * for changed to synchronize with the server.
     *
     * @return SixthMassUserProfile
     */
    public static SixthMassUserProfile getProfile() {
        try {
            return M6Config.instance().getUserProfile();
        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Main method for sending events
     *s
     * @param task Task object
     */
    private static void track(final M6Task task) {

        try {
            // tasks are added to concurrent queue
            // we don't wan't to block main thread but it's also thread safe
            taskQueue.add(task);
            // running new worker in the blocking queue (thread safe) for each task added
            // but only on one thread
            // that we get sequential execution of threads (until previous thread finished, new one is waiting in Blocking Queue
            // some of the threads may run on "empty" TaskList but that's ok since in normal scenario no burst of events
            // is expected even though SixthMassAPI can handle those too
            M6TaskEmitter emit = new M6TaskEmitter(taskQueue, M6Config.instance());

            // not waiting for the future to finish (maybe loss of couple of last events when app closed?)
            // maybe continue in background to send events?
            Future<?> future = poolExecutor.submit(emit);
            while (!future.isDone() && !future.isCancelled()) { // TODO: remove later?
            }
        } catch (SixthMassException e) {
            Log.e("SixthMass", e.getMessage(), e);
        }
    }
}
