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
package org.truth0.gwt;

import org.truth0.AbstractVerb;
import org.truth0.Expect;
import org.truth0.FailureStrategy;
import org.truth0.TestVerb;
import org.truth0.Truth;
import org.truth0.subjects.AbstractArraySubject;
import org.truth0.subjects.BooleanSubject;
import org.truth0.subjects.ClassSubject;
import org.truth0.subjects.DefaultSubject;
import org.truth0.subjects.IntegerSubject;
import org.truth0.subjects.IterableSubject;
import org.truth0.subjects.ListSubject;
import org.truth0.subjects.MapSubject;
import org.truth0.subjects.ObjectArraySubject;
import org.truth0.subjects.OptionalSubject;
import org.truth0.subjects.Ordered;
import org.truth0.subjects.PrimitiveBooleanArraySubject;
import org.truth0.subjects.PrimitiveCharArraySubject;
import org.truth0.subjects.PrimitiveDoubleArraySubject;
import org.truth0.subjects.PrimitiveFloatArraySubject;
import org.truth0.subjects.PrimitiveIntArraySubject;
import org.truth0.subjects.PrimitiveLongArraySubject;
import org.truth0.subjects.StringSubject;
import org.truth0.subjects.Subject;
import org.truth0.subjects.SubjectFactory;
import org.truth0.util.Platform;
import org.truth0.util.StringUtil;

public class Inventory {
  // Main
  AbstractVerb<?> aa;
  Expect ab;
  FailureStrategy ac;
  TestVerb ad;
  Truth ae;

  // Subject
  AbstractArraySubject<?, ?> a;
  BooleanSubject b;
  ClassSubject c;
  DefaultSubject e;
  IntegerSubject f;
  IterableSubject<?, ?, ?> g;
  ListSubject<?, ?, ?> h;
  MapSubject<?, ?, ?, ?> i;
  ObjectArraySubject<?> j;
  OptionalSubject<?> k;
  Ordered l;
  PrimitiveBooleanArraySubject m;
  PrimitiveCharArraySubject n;
  PrimitiveDoubleArraySubject o;
  PrimitiveFloatArraySubject p;
  PrimitiveIntArraySubject q;
  PrimitiveLongArraySubject r;
  StringSubject s;
  Subject<?, ?> t;
  SubjectFactory<?, ?> u;

  // Util
  Platform v;
  StringUtil w;

}

