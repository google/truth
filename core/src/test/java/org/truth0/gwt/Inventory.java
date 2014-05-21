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

