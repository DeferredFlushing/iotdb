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
package org.apache.iotdb.db.utils.datastructure;

public class QuickIntTVList extends IntTVList implements QuickSort {
  @Override
  public void sort() {
    if (!sorted) {
      qSort(0, rowCount - 1);
    }
    sorted = true;
  }

  @Override
  public void sort(int lo, int hi) {
    qSort(lo, hi);
  }

  @Override
  public void swap(int p, int q) {
    int valueP = getInt(p);
    long timeP = getTime(p);
    int valueQ = getInt(q);
    long timeQ = getTime(q);
    set(p, timeQ, valueQ);
    set(q, timeP, valueP);
  }

  @Override
  protected void set(int src, int dest) {
    long srcT = getTime(src);
    int srcV = getInt(src);
    set(dest, srcT, srcV);
  }

  @Override
  public int compare(int idx1, int idx2) {
    long t1 = getTime(idx1);
    long t2 = getTime(idx2);
    return Long.compare(t1, t2);
  }
}
