/* Copyright 2011 GOTO Metrics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.gotometrics.format;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;

public class TestFloatFormat
{
  private static final int NUM_TESTS = 1024 * 1024;
  private static DataFormat ascendingFormat = FloatFormat.get(),
                            descendingFormat = DescendingFloatFormat.get();

  private final Random r;
  private final int numTests;

  public TestFloatFormat(Random r, int numTests) {
    this.r = r;
    this.numTests = numTests;
  }

  private float randFloat() {
    switch (r.nextInt(128)) {
      case 0:
        return +0.0f;
      case 1:
        return -0.0f;
      case 2:
        return Float.POSITIVE_INFINITY;
      case 3:
        return Float.NEGATIVE_INFINITY;
      case 4:
        return Float.NaN;
    }

    return r.nextFloat();
  }

  /* Returns 1 if +0.0, 0 otherwise */
  private int isPositiveZero(float f) {
    return 1/f == Float.POSITIVE_INFINITY ? 1 : 0;
  }

  private void verifyEncoding(DataFormat format, float f,
      ImmutableBytesWritable fBytes) 
  {
    float decoded = format.decodeFloat(fBytes);
    if (!Float.isNaN(f) && f != 0.0f) {
      if (f != decoded)
        throw new RuntimeException("Float " + f + " decoded as " + decoded);
      return;
    } else if (Float.isNaN(f)) {
      if (!Float.isNaN(decoded))
        throw new RuntimeException("Float " + f + " decoded as " + decoded);
      return;
    } else if (f != decoded || isPositiveZero(f) != isPositiveZero(decoded)) 
        throw new RuntimeException("Float " + f + " decoded as " + decoded);
  }

  int floatCompare(float f, float g) {
    if (!Float.isNaN(f) && !Float.isNaN(g) && !(f == 0 && g == 0)) 
      return ((f > g) ? 1 : 0) - ((g > f) ? 1 : 0);

    if (Float.isNaN(f)) {
      if (Float.isNaN(g))
        return 0;
      return 1;
    } else if (Float.isNaN(g)) {
      return -1;
    } else /* f == +/-0.0 && g == +/-0.0 */ {
      return isPositiveZero(f) - isPositiveZero(g);
    }
  }

  private void verifySort(DataFormat format, float f, 
      ImmutableBytesWritable fBytes, float g, ImmutableBytesWritable gBytes)
  {
    int expectedOrder = floatCompare(f, g);
    int byteOrder = Integer.signum(Bytes.compareTo(fBytes.get(), 
          fBytes.getOffset(), fBytes.getLength(), gBytes.get(), 
          gBytes.getOffset(), gBytes.getLength()));

    if (format.getOrder() == Order.DESCENDING)
      expectedOrder = -expectedOrder;

    if (expectedOrder != byteOrder)
      throw new RuntimeException("Comparing " + f + " to "
          + g + " expected signum " + expectedOrder +
          " got signum " + byteOrder);
  }
                          
  public void test() {
    float f, g;
    ImmutableBytesWritable fBytes = new ImmutableBytesWritable(),
                           gBytes = new ImmutableBytesWritable();

    for (int i  = 0; i < numTests; i++) {
      DataFormat format = r.nextBoolean() ? ascendingFormat : descendingFormat;
      f = randFloat();
      g = randFloat();

      /* Canonicalize NaNs */
      f = Float.intBitsToFloat(Float.floatToIntBits(f));
      g = Float.intBitsToFloat(Float.floatToIntBits(g));
    
      format.encodeFloat(f, fBytes);
      format.encodeFloat(g, gBytes);

      verifyEncoding(format, f, fBytes);
      verifyEncoding(format, g, gBytes);

      verifySort(format, f, fBytes, g, gBytes);
    }
  }

  private static void usage() {
    System.err.println("Usage: TestFloatFormat <-s seed> <-n numTests>");
    System.exit(-1);
  }

  public static void main(String[] args) throws IOException {
    long seed = System.currentTimeMillis();
    int numTests = NUM_TESTS;

    for (int i = 0; i < args.length; i++) {
      if ("-s".equals(args[i]))
        seed = Long.valueOf(args[++i]);
      else if ("-n".equals(args[i]))
        numTests = Integer.valueOf(args[++i]);
      else usage();
    }

    FileWriter f = new FileWriter("TestFloatFormat.out");
    try {
      f.write("-s " + seed + " -n " + numTests);
    } finally {
      f.close();
    }

    TestFloatFormat tff = new TestFloatFormat(new Random(seed), numTests);
    tff.test();

    System.exit(0);
  }
}
