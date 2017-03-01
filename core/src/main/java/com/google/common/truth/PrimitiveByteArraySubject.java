/*
 * Copyright (c) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.truth;

import com.google.common.collect.EvictingQueue;
import com.google.common.primitives.Bytes;
import com.google.gwt.i18n.shared.FirstStrongDirectionEstimator;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A Subject to handle testing propositions for {@code byte[]}.
 *
 * @author Kurt Alfred Kluever
 */
public final class PrimitiveByteArraySubject
    extends AbstractArraySubject<PrimitiveByteArraySubject, byte[]> {
  PrimitiveByteArraySubject(FailureStrategy failureStrategy, @Nullable byte[] o) {
    super(failureStrategy, o);
  }

  @Override
  protected String underlyingType() {
    return "byte";
  }

  @Override
  protected List<Byte> listRepresentation() {
    return Bytes.asList(actual());
  }

  /**
   * A proposition that the actual array and {@code expected} are arrays of the same length and
   * type, containing elements such that each element in {@code expected} is equal to each element
   * in the actual array, and in the same position.
   */
  @Override
  public void isEqualTo(Object expected) {
    byte[] actual = actual();
    if (actual == expected) {
      return; // short-cut.
    }
    try {
      byte[] expectedArray = (byte[]) expected;
      if (!Arrays.equals(actual, expectedArray)) {
        failureStrategy.failComparing(
            getArrayDiffMessage(actual, expectedArray),
            base16(expectedArray),
            base16(getSubject()));
      }
    } catch (ClassCastException e) {
      failWithBadType(expected);
    }
  }

  private String getArrayDiffMessage(byte[] actual, byte[] expected) {
    
    int MAX_ELEMENTS = 10;
  
    boolean bracketOpen = false;
    int firstWrongLocation = -1;
    boolean canDropHead = true;
    boolean headCropped = false;
    boolean tailCropped = false;
    
    StringBuilder actualOut = new StringBuilder();
    StringBuilder expectedOut = new StringBuilder();
    
    for (int i = 0; i < actual.length; i++) {
      if (actual[i] != expected[i] && !bracketOpen) {
        actualOut.append("[");
        bracketOpen = true;
        if (firstWrongLocation < 0) {
          firstWrongLocation = i;
        }
      }
      if (bracketOpen && actual[i] == expected[i]) {
        actualOut.append("]");
        bracketOpen = false;
      } 
      actualOut.append(actual[i] + ",");      
      if (canDropHead && i > MAX_ELEMENTS) {
        canDropHead = dropHeadElemnt(actualOut);
        if (canDropHead) {
          headCropped = true;
        }
      }
      if (i > MAX_ELEMENTS && !canDropHead) {
        tailCropped = true;
        break;
      }
    }
    
    if (actualOut.charAt(actualOut.length()-1) == ',') {
      actualOut.setLength(actualOut.length()-1);
    }
    
    if (headCropped) {
      actualOut.insert(0, "...");
    }
    if (tailCropped) {
      actualOut.append("...");
    }
    
    int diffCount = 0;
    for (int i=0; i<actual.length; i++) {
      if (actual[i] != expected[i]) {
        diffCount++;
      }
    }
    
    return "Not true that <"
        + actualOut
        + "> is equal to <"
        + expectedOut
        + ">;"
        + "Failed with " 
        + diffCount + " element mismatches, "
        + "with 1st element mismatch is at index "
        + firstWrongLocation + ".";
  }

  private boolean dropHeadElemnt(StringBuilder data) {
    int firstIndex = data.indexOf(",");
    if (data.substring(0,  firstIndex+1).contains("[")){
      return false;
    }
    data.delete(0, firstIndex+1);
    return true;
  }

  // We could add a dep on com.google.common.io, but that seems overkill for base16 encoding
  private static String base16(byte[] bytes) {
    StringBuilder sb = new StringBuilder(2 * bytes.length);
    for (byte b : bytes) {
      sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
    }
    return sb.toString();
  }

  private static final char[] hexDigits = "0123456789ABCDEF".toCharArray();

  /**
   * A proposition that the actual array and {@code expected} are not arrays of the same length and
   * type, containing elements such that each element in {@code expected} is equal to each element
   * in the actual array, and in the same position.
   */
  @Override
  public void isNotEqualTo(Object expected) {
    byte[] actual = actual();
    try {
      byte[] expectedArray = (byte[]) expected;
      if (actual == expected || Arrays.equals(actual, expectedArray)) {
        failWithRawMessage(
            "%s unexpectedly equal to %s.", actualAsString(), Arrays.toString(expectedArray));
      }
    } catch (ClassCastException ignored) {
      // If it's not byte[] then it's not equal and the test passes.
    }
  }

  public IterableSubject asList() {
    return internalCustomName() != null
        ? check().that(listRepresentation()).named(internalCustomName())
        : check().that(listRepresentation());
  }
}
