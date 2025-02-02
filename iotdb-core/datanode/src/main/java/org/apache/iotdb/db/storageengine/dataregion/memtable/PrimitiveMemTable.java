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

package org.apache.iotdb.db.storageengine.dataregion.memtable;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveMemTable extends AbstractMemTable {
  // this constructor only used when deserialize
  public PrimitiveMemTable() {
    super();
  }

  public PrimitiveMemTable(String database, String dataRegionId) {
    super(database, dataRegionId);
  }

  public PrimitiveMemTable(
      String database, String dataRegionId, Map<IDeviceID, IWritableMemChunkGroup> memTableMap) {
    super(database, dataRegionId, memTableMap);
  }

  public PrimitiveMemTable(
      String database,
      String dataRegionId,
      Map<IDeviceID, IWritableMemChunkGroup> memTableMap,
      AbstractMemTable lastMemTable) {
    super(database, dataRegionId, memTableMap, lastMemTable);
  }

  @Override
  public IMemTable copy() {
    Map<IDeviceID, IWritableMemChunkGroup> newMap = new HashMap<>(getMemTableMap());

    return new PrimitiveMemTable(getDatabase(), getDataRegionId(), newMap);
  }

  @Override
  public boolean isSignalMemTable() {
    return false;
  }

  @Override
  public String toString() {
    return "PrimitiveMemTable{planIndex=[" + getMinPlanIndex() + "," + getMaxPlanIndex() + "]}";
  }
}
