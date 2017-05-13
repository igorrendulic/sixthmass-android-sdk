package com.sixthmass.sdk.util;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 *
 * Worker class for persisting task list and sending tasks to SixthMass server
 * Worker is a thread which is meant to run in sequential order (with ExecutorService as single thread executor)
 * Even though it's thread safe it's not meant to have concurrent threads running
 * In case of concurrent threads task list sequence is unpredictable
 *
 * Each event gets it's own worker. This covers most examples when events are setup to be tracked on user click for example
 * It can also handle tracking bursts.
 *
 * Each event still gets it's own worker. It just means some workers might iterate over complete task list and
 * not send to server anything if all previously stored events have already been sent. They might clean up already sent events if any
 *
 * TODO: currently only sending one by one event. Change to send in bulk
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6TaskEmitter implements Runnable {

    /**
     * Concurrent linked queue
     */
    private ConcurrentLinkedQueue<M6Task> queue;

    /**
     * SDK Configuration
     */
    private M6Config config;

    /**
     * This object's constructor initalizing queue of tasks and a configuration
     *
     * It also checks if SDK was initializaed properly
     *
     * @param queue Input event queue
     * @param config Configuration
     * @throws SixthMassException Exception when SDK not initailized first
     */
    public M6TaskEmitter(ConcurrentLinkedQueue<M6Task> queue, M6Config config) throws SixthMassException {

        if (queue == null || config == null) {
            throw new SixthMassException("SixthMass SDK Not initialized");
        }
        if (config.getToken() == null || config.getToken() == null) {
            throw new SixthMassException("SixthMass SDK Not Properly initialized. Context and Token can not be null");
        }

        this.queue = queue;
//        this.context = config.getContext();
        this.config = config;
    }

    @Override
    public void run() {
        try {

            List<M6Task> taskList = M6Util.readTaskList(config.getContext());
            if (taskList == null) {
                taskList = Collections.synchronizedList(new ArrayList<M6Task>());
            }
            while (!this.queue.isEmpty()) {
                M6Task task = this.queue.poll();
                task.sent = false;
                taskList.add(task);
            }
            M6Util.saveTaskList(taskList, config.getContext());

            // if more than one object in task list we augment each next event with previous one
            if (taskList.size() > 1) {
                for (int i = 1; i < taskList.size(); i++) {
                    M6Task previousTask = taskList.get(i - 1);
                    M6Task currentTask = taskList.get(i);

                    // skip already sent events
                    if (previousTask.sent && currentTask.sent) {
                        continue;
                    }
                    // new session (if not launch task encountered)
                    if (!M6Util.EVENT_NAME_LAUNCH.equals(currentTask.eventName)) {
                        currentTask.previousTimestamp = previousTask.timestamp;
                        currentTask.previousEvent = previousTask.eventName;
                    }

                    if (!previousTask.sent) {
                        JSONObject test = previousTask.toJson();
                        Log.i("SixthMass", test.toString(1));
                        M6Util.httpPost(M6Util.ENDPOINT_SINGLE_EVENT, previousTask.toJson());
                        previousTask.sent = true;
                    }
                    if (!currentTask.sent) {
                        M6Util.httpPost(M6Util.ENDPOINT_SINGLE_EVENT, currentTask.toJson());
                        JSONObject test = currentTask.toJson();
                        Log.i("SixthMass", test.toString(1));
                        currentTask.sent = true;
                    }
                }
            } else if (taskList.size() == 1) {
                // sending single event (first event) if it hasn't been sent yet
                if (!taskList.get(0).sent) {
                    M6Util.httpPost(M6Util.ENDPOINT_SINGLE_EVENT, taskList.get(0).toJson());
                    JSONObject test = taskList.get(0).toJson();
                    Log.i("SixthMass", test.toString(1));
                    taskList.get(0).sent = true;
                }
            }

            // clean up already sent tasks (all but the last one)
            List<M6Task> cleanedTaskList = new ArrayList<>();
            if (taskList.size() > 1) {
                for (int i=0; i<taskList.size(); i++) {
                    M6Task task = taskList.get(i);
                    if (!task.sent || i == taskList.size() - 1) {
                        cleanedTaskList.add(task);
                    }
                }
            } else {
                cleanedTaskList = taskList;
            }

            // saving task list again since some events might of been modified (sent flag)
            M6Util.saveTaskList(cleanedTaskList, config.getContext());

        } catch (Exception e) {
            Log.e("SixtMass", e.getMessage(), e);
        }
    }
}
