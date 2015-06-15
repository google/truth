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
package com.google.common.truth;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.truth.codegen.CompilingClassLoader;
import com.google.common.truth.codegen.CompilingClassLoader.CompilerException;
import com.google.common.truth.codegen.IteratingWrapperClassBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

import javax.annotation.CheckReturnValue;
/**
 * A verb that iterates over data and applies the predicate iteratively
 */
@GwtIncompatible("Code generation and loading.")
@J2ObjCIncompatible("Code generation and loading.")
public class IteratingVerb<T> {
  private static final String CANNOT_WRAP_MSG = "Cannot build an iterating wrapper around ";

  private static LoadingCache<SubjectFactory<?, ?>, Class<?>> WRAPPER_CACHE =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<SubjectFactory<?, ?>, Class<?>>() {
                @Override
                public Class<?> load(SubjectFactory<?, ?> subjectFactory) throws Exception {
                  return compileWrapperClass(subjectFactory);
                }
              });

  private final Iterable<T> data;
  private final FailureStrategy failureStrategy;

  public IteratingVerb(Iterable<T> data, FailureStrategy fs) {
    this.failureStrategy = fs;
    this.data = data;
  }

  @CheckReturnValue
  public <S extends Subject<S, T>, SF extends SubjectFactory<S, T>> S thatEach(SF factory) {
    return wrap(failureStrategy, factory, data);
  }

  private <S extends Subject<S, T>, SF extends SubjectFactory<S, T>>
      S wrap(FailureStrategy fs, SF factory, Iterable<T> data) {
    Type t = factory.getSubjectClass();
    Class<?> wrapperClass;
    try {
      wrapperClass = WRAPPER_CACHE.get(factory);
      return instantiate(wrapperClass, t, fs, factory, data);
    } catch (ExecutionException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    }
  }

  @SuppressWarnings("unchecked")
  private <SF, S> S instantiate(
      Class<?> wrapperType, Type t, FailureStrategy fs, SF factory, Iterable<T> data) {
    try {
      Constructor<S> c = (Constructor<S>) wrapperType.getConstructors()[0];
      return c.newInstance(fs, factory, data);
    } catch (SecurityException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (InstantiationException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(CANNOT_WRAP_MSG + t, e);
    }
  }

  private static Class<?> compileWrapperClass(SubjectFactory<?, ?> subjectFactory) {
    IteratingWrapperClassBuilder builder = new IteratingWrapperClassBuilder(subjectFactory);
    String out = builder.build().toString();
    ClassLoader classLoader;
    try {
      classLoader = new CompilingClassLoader(
          subjectFactory.getSubjectClass().getClassLoader(), builder.className, out, null);
    } catch (CompilerException e) {
      throw new Error("Could not compile class " + builder.className + " with source:\n" + out, e);
    }
    try {
      Class<?> wrapper = classLoader.loadClass(builder.className);
      return wrapper;
    } catch (ClassNotFoundException e) {
      throw new Error(
          "Could not load class " + subjectFactory.getSubjectClass().getSimpleName(), e);
    }
  }
}
