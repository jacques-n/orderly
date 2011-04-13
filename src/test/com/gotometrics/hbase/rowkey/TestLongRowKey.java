/*  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.gotometrics.hbase.rowkey;

import org.apache.hadoop.io.LongWritable;

public class TestLongRowKey extends TestLongWritableRowKey
{
  @Override
  public AbstractVarIntRowKey createVarIntRowKey() { 
    return new LongRowKey(); 
  }

  @Override
  public Object createObject() {
    Object o = super.createObject();
    if (o == null)
      return o;
    return Long.valueOf(((LongWritable)o).get());
  }

  @Override
  public int compareTo(Object o1, Object o2) {
    if (o1 == null || o2 == null)
      return super.compareTo(o1, o2);
    return super.compareTo(new LongWritable((Long)o1),
        new LongWritable((Long)o2));
  }
}
