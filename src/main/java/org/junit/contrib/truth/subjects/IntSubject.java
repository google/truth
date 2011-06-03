/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.junit.contrib.truth.subjects;

import org.junit.contrib.truth.FailureStrategy;

public class IntSubject extends Subject<Integer> {

 private static final String RANGE_BOUNDS_OUT_OF_ORDER_MSG =
   "Range inclusion parameter lower (%d) should not be greater than upper (%d)";
  
 public IntSubject(FailureStrategy failureStrategy, int i) {
  super(failureStrategy, i);
 }

 /**
  * Attests that a Subject<Integer> is inclusively within the 
  * {@code lower} and {@code upper} bounds provided or fails.
  * 
  * @throws IllegalArgumentException if the lower bound is greater than the upper.
  */
 public Subject<Integer> isInclusivelyInRange(int lower, int upper) {
  if (lower > upper) {
   throw new IllegalArgumentException(
     String.format(RANGE_BOUNDS_OUT_OF_ORDER_MSG, lower, upper));
  }
  if (!(lower <= getSubject() && getSubject() <= upper)) {
   fail("is inclusively in range", lower, upper);
  }
  return this;    
 }
 
 /**
  * Attests that a Subject<Integer> is exclusively within the 
  * {@code lower} and {@code upper} bounds provided or fails.
  * 
  * @throws IllegalArgumentException if the lower bound is greater than the upper.
  */
 public Subject<Integer> isBetween(int lower, int upper) {
  if (lower > upper) {
   throw new IllegalArgumentException(
     String.format(RANGE_BOUNDS_OUT_OF_ORDER_MSG, lower, upper));
  }
  if (!(lower < getSubject() && getSubject() < upper)) {
   fail("is in between", lower, upper);
  }
  return this;    
 }
 
}
