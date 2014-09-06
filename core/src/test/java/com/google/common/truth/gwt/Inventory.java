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

import com.google.common.truth.AbstractArraySubject;
import com.google.common.truth.BooleanSubject;
import com.google.common.truth.ClassSubject;
import com.google.common.truth.DefaultSubject;
import com.google.common.truth.Expect;
import com.google.common.truth.FailureStrategy;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.ListSubject;
import com.google.common.truth.MapSubject;
import com.google.common.truth.ObjectArraySubject;
import com.google.common.truth.OptionalSubject;
import com.google.common.truth.Ordered;
import com.google.common.truth.Platform;
import com.google.common.truth.PrimitiveBooleanArraySubject;
import com.google.common.truth.PrimitiveCharArraySubject;
import com.google.common.truth.PrimitiveDoubleArraySubject;
import com.google.common.truth.PrimitiveFloatArraySubject;
import com.google.common.truth.PrimitiveIntArraySubject;
import com.google.common.truth.PrimitiveLongArraySubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.SubjectFactory;
import com.google.common.truth.TestVerb;
import com.google.common.truth.Truth;

/**
 * Static references to a variety of classes to force their loading during
 * the {@link TruthGwtTest}.
 */
public class Inventory {
  AbstractArraySubject<?, ?> a;
  BooleanSubject b;
  ClassSubject c;
  DefaultSubject e;
  Expect ab;
  FailureStrategy ac;
  IntegerSubject f;
  IterableSubject<?, ?, ?> g;
  ListSubject<?, ?, ?> h;
  MapSubject<?, ?, ?, ?> i;
  ObjectArraySubject<?> j;
  OptionalSubject<?> k;
  Ordered l;
  Platform v;
  PrimitiveBooleanArraySubject m;
  PrimitiveCharArraySubject n;
  PrimitiveDoubleArraySubject o;
  PrimitiveFloatArraySubject p;
  PrimitiveIntArraySubject q;
  PrimitiveLongArraySubject r;
  StringSubject s;
  Subject<?, ?> t;
  SubjectFactory<?, ?> sf;
  TestVerb tv;
  Truth tr;
}

