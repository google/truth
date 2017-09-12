/*
 * Copyright (c) 2011 Google, Inc.
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
package com.google.common.truth.gwt;

import com.google.common.truth.BigDecimalSubject;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.ClassSubject;
import com.google.common.truth.ComparableSubject;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.DoubleSubject;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.GuavaOptionalSubject;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.ListMultimapSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.MapSubject;
import com.google.common.truth.MathUtil;
import com.google.common.truth.MultimapSubject;
import com.google.common.truth.MultisetSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.Ordered;
import com.google.common.truth.PrimitiveBooleanArraySubject;
import com.google.common.truth.PrimitiveByteArraySubject;
import com.google.common.truth.PrimitiveCharArraySubject;
import com.google.common.truth.PrimitiveDoubleArraySubject;
import com.google.common.truth.PrimitiveFloatArraySubject;
import com.google.common.truth.PrimitiveIntArraySubject;
import com.google.common.truth.PrimitiveLongArraySubject;
import com.google.common.truth.PrimitiveShortArraySubject;
import com.google.common.truth.SetMultimapSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.TableSubject;
import com.google.common.truth.ThrowableSubject;
import com.google.common.truth.Truth;
import com.google.common.truth.TruthJUnit;

/**
 * Static references to a variety of classes to force their loading during the {@link TruthGwtTest}.
 */
public class Inventory {
  BigDecimalSubject bigDecimalSubject;
  BooleanSubject booleanSubject;
  ClassSubject classSubject;
  ComparableSubject comparableSubject;
  DefaultSubject defaultSubject;
  DoubleSubject doubleSubject;
  FailureStrategy failureStrategy;
  FloatSubject floatSubject;
  GuavaOptionalSubject guavaOptionalSubject;
  IntegerSubject integerSubject;
  IterableSubject iterableSubject;
  ListMultimapSubject listMultimapSubject;
  LongSubject longSubject;
  MapSubject mapSubject;
  MathUtil mathUtil;
  MultimapSubject multimapSubject;
  MultisetSubject multisetSubject;
  ObjectArraySubject objectArraySubject;
  Ordered ordered;
  PrimitiveBooleanArraySubject primitiveBooleanArraySubject;
  PrimitiveByteArraySubject primitiveByteArraySubject;
  PrimitiveCharArraySubject primitiveCharArraySubject;
  PrimitiveDoubleArraySubject primitiveDoubleArraySubject;
  PrimitiveFloatArraySubject primitiveFloatArraySubject;
  PrimitiveIntArraySubject primitiveIntArraySubject;
  PrimitiveLongArraySubject primitiveLongArraySubject;
  PrimitiveShortArraySubject primitiveShortArraySubject;
  SetMultimapSubject setMultimapSubject;
  StringSubject stringSubject;
  SubjectFactory subjectFactory;
  Subject subject;
  TableSubject tableSubject;
  ThrowableSubject throwableSubject;
  Truth truth;
  TruthJUnit truthJUnit;
}
