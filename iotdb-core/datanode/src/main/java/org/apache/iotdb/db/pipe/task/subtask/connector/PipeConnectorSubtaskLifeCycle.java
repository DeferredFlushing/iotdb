/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.iotdb.db.pipe.task.subtask.connector;

import org.apache.iotdb.commons.pipe.task.connection.BoundedBlockingPendingQueue;
import org.apache.iotdb.db.pipe.execution.executor.PipeConnectorSubtaskExecutor;
import org.apache.iotdb.pipe.api.event.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipeConnectorSubtaskLifeCycle implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipeConnectorSubtaskLifeCycle.class);

  private final PipeConnectorSubtaskExecutor executor;
  private final PipeConnectorSubtask subtask;
  private final BoundedBlockingPendingQueue<Event> pendingQueue;

  private int runningTaskCount;
  private int registeredTaskCount;

  public PipeConnectorSubtaskLifeCycle(
      PipeConnectorSubtaskExecutor executor,
      PipeConnectorSubtask subtask,
      BoundedBlockingPendingQueue<Event> pendingQueue) {
    this.executor = executor;
    this.subtask = subtask;
    this.pendingQueue = pendingQueue;

    runningTaskCount = 0;
    registeredTaskCount = 0;
  }

  public PipeConnectorSubtask getSubtask() {
    return subtask;
  }

  public BoundedBlockingPendingQueue<Event> getPendingQueue() {
    return pendingQueue;
  }

  public synchronized void register() {
    if (registeredTaskCount < 0) {
      throw new IllegalStateException("registeredTaskCount < 0");
    }

    if (registeredTaskCount == 0) {
      executor.register(subtask);
      runningTaskCount = 0;
    }

    registeredTaskCount++;
    LOGGER.info(
        "Register subtask {}. runningTaskCount: {}, registeredTaskCount: {}",
        subtask,
        runningTaskCount,
        registeredTaskCount);
  }

  /**
   * Deregister the subtask. If the subtask is the last one, close the subtask.
   *
   * <p>Note that this method should be called after the subtask is stopped. Otherwise, the
   * runningTaskCount might be inconsistent with the registeredTaskCount because of parallel
   * connector scheduling.
   *
   * @param pipeNameToDeregister pipe name
   * @return true if the subtask is out of life cycle, indicating that the subtask should never be
   *     used again
   * @throws IllegalStateException if registeredTaskCount <= 0
   */
  public synchronized boolean deregister(String pipeNameToDeregister) {
    if (registeredTaskCount <= 0) {
      throw new IllegalStateException("registeredTaskCount <= 0");
    }

    subtask.discardEventsOfPipe(pipeNameToDeregister);

    try {
      if (registeredTaskCount > 1) {
        return false;
      }

      close();
      // This subtask is out of life cycle, should never be used again
      return true;
    } finally {
      registeredTaskCount--;
      LOGGER.info(
          "Deregister subtask {}. runningTaskCount: {}, registeredTaskCount: {}",
          subtask,
          runningTaskCount,
          registeredTaskCount);
    }
  }

  public synchronized void start() {
    if (runningTaskCount < 0) {
      throw new IllegalStateException("runningTaskCount < 0");
    }

    if (runningTaskCount == 0) {
      executor.start(subtask.getTaskID());
    }

    runningTaskCount++;
    LOGGER.info(
        "Start subtask {}. runningTaskCount: {}, registeredTaskCount: {}",
        subtask,
        runningTaskCount,
        registeredTaskCount);
  }

  public synchronized void stop() {
    if (runningTaskCount <= 0) {
      throw new IllegalStateException("runningTaskCount <= 0");
    }

    if (runningTaskCount == 1) {
      executor.stop(subtask.getTaskID());
    }

    runningTaskCount--;
    LOGGER.info(
        "Stop subtask {}. runningTaskCount: {}, registeredTaskCount: {}",
        subtask,
        runningTaskCount,
        registeredTaskCount);
  }

  @Override
  public synchronized void close() {
    executor.deregister(subtask.getTaskID());
  }
}
