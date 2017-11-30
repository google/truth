/*
 * Copyright (c) 2017 Google, Inc.
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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackTraceCleaner}. */
@RunWith(JUnit4.class)
public class StackTraceCleanerTest {
  @Rule public final ExpectFailure expectFailure = new ExpectFailure();

  @Test
  public void emptyTrace() {
    Throwable throwable = createThrowableWithStackTrace();

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace()).isEqualTo(new StackTraceElement[0]);
  }

  @Test
  public void collapseStreaks() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.example.MyTest",
            "junit.Foo",
            "org.junit.Bar",
            "com.google.testing.junit.Car",
            "com.google.testing.testsize.Dar",
            "com.google.testing.util.Far",
            "com.example.Gar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.MyTest"),
              createCollapsedStackTraceElement("Testing framework", 5),
              createStackTraceElement("com.example.Gar"),
            });
  }

  @Test
  public void assertionsActuallyUseCleaner() {
    expectFailure.whenTesting().that(1).isEqualTo(2);
    assertThat(expectFailure.getFailure().getStackTrace()[0].getClassName())
        .isEqualTo(getClass().getName());
  }

  @Test
  public void assertionsActuallyUseCleaner_ComparisonFailure() {
    expectFailure.whenTesting().that("1").isEqualTo("2");
    assertThat(expectFailure.getFailure().getStackTrace()[0].getClassName())
        .isEqualTo(getClass().getName());
  }

  @Test
  public void dontCollapseStreaksOfOneFrame() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.example.MyTest",
            "junit.Foo",
            "com.example.Helper",
            "org.junit.Bar",
            "com.example.Helper",
            "com.google.testing.junit.Car",
            "com.google.testing.testsize.Dar",
            "com.google.testing.util.Far",
            "com.example.Gar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.MyTest"),
              createStackTraceElement("junit.Foo"),
              createStackTraceElement("com.example.Helper"),
              createStackTraceElement("org.junit.Bar"),
              createStackTraceElement("com.example.Helper"),
              createCollapsedStackTraceElement("Testing framework", 3),
              createStackTraceElement("com.example.Gar"),
            });
  }

  @Test
  public void mixedStreaks() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.example.MyTest",
            "junit.Foo",
            "org.junit.Bar",
            "java.lang.reflect.Car",
            "sun.reflect.Dar",
            "com.google.testing.testsize.Dar",
            "com.google.testing.util.Far",
            "com.google.common.truth.Gar",
            "com.google.common.truth.Har",
            "com.example.Jar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.MyTest"),
              createCollapsedStackTraceElement("Testing framework", 2),
              createCollapsedStackTraceElement("Reflective call", 2),
              createCollapsedStackTraceElement("Testing framework", 2),
              createCollapsedStackTraceElement("Truth framework", 2),
              createStackTraceElement("com.example.Jar"),
            });
  }

  @Test
  public void removesTruthFramesOnTop() {
    Throwable throwable =
        createThrowableWithStackTrace("com.google.common.truth.Foo", "com.example.Bar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.Bar"),
            });
  }

  @Test
  public void removesTestingAndReflectiveFramesOnBottom() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.example.Foo",
            "com.example.Bar",
            "sun.reflect.Car",
            "org.junit.Dar",
            "java.lang.reflect.Far",
            "junit.Gar",
            "com.google.testing.junit.Har",
            "java.lang.reflect.Jar",
            "java.lang.reflect.JarJar",
            "com.google.testing.junit.Kar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.Foo"),
              createStackTraceElement("com.example.Bar"),
            });
  }

  @Test
  public void packagesAreIgnoredForTestClasses() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.google.common.truth.Foo", "com.google.common.truth.MyTest");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.google.common.truth.MyTest"),
            });
  }

  @Test
  public void causingThrowablesAreAlsoCleaned() {
    Throwable cause2 = createThrowableWithStackTrace("com.example.Foo", "org.junit.FilterMe");
    Throwable cause1 =
        createThrowableWithStackTrace(cause2, "com.example.Bar", "org.junit.FilterMe");
    Throwable rootThrowable =
        createThrowableWithStackTrace(cause1, "com.example.Car", "org.junit.FilterMe");

    StackTraceCleaner.cleanStackTrace(rootThrowable);

    assertThat(rootThrowable.getStackTrace()).isEqualTo(createStackTrace("com.example.Car"));
    assertThat(cause1.getStackTrace()).isEqualTo(createStackTrace("com.example.Bar"));
    assertThat(cause2.getStackTrace()).isEqualTo(createStackTrace("com.example.Foo"));
  }

  @Test
  public void suppressedThrowablesAreAlsoCleaned() {
    Throwable throwable = createThrowableWithStackTrace("com.example.Foo", "org.junit.FilterMe");
    Throwable suppressed1 = createThrowableWithStackTrace("com.example.Bar", "org.junit.FilterMe");
    Throwable suppressed2 = createThrowableWithStackTrace("com.example.Car", "org.junit.FilterMe");
    throwable.addSuppressed(suppressed1);
    throwable.addSuppressed(suppressed2);

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace()).isEqualTo(createStackTrace("com.example.Foo"));
    assertThat(suppressed1.getStackTrace()).isEqualTo(createStackTrace("com.example.Bar"));
    assertThat(suppressed2.getStackTrace()).isEqualTo(createStackTrace("com.example.Car"));
  }

  @Test
  public void mixedCausingAndSuppressThrowablesAreCleaned() {
    Throwable suppressed1 = createThrowableWithStackTrace("com.example.Foo", "org.junit.FilterMe");
    Throwable cause2 = createThrowableWithStackTrace("com.example.Bar", "org.junit.FilterMe");
    Throwable cause1 =
        createThrowableWithStackTrace(cause2, "com.example.Car", "org.junit.FilterMe");
    Throwable suppressed2 =
        createThrowableWithStackTrace(cause1, "com.example.Dar", "org.junit.FilterMe");
    Throwable throwable = createThrowableWithStackTrace("com.example.Far", "org.junit.FilterMe");
    throwable.addSuppressed(suppressed1);
    throwable.addSuppressed(suppressed2);

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace()).isEqualTo(createStackTrace("com.example.Far"));
    assertThat(suppressed1.getStackTrace()).isEqualTo(createStackTrace("com.example.Foo"));
    assertThat(suppressed2.getStackTrace()).isEqualTo(createStackTrace("com.example.Dar"));
    assertThat(cause1.getStackTrace()).isEqualTo(createStackTrace("com.example.Car"));
    assertThat(cause2.getStackTrace()).isEqualTo(createStackTrace("com.example.Bar"));
  }

  @Test
  public void cleaningTraceIsIdempotent() {
    Throwable throwable = createThrowableWithStackTrace("com.example.Foo", "org.junit.FilterMe");

    StackTraceCleaner.cleanStackTrace(throwable);
    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace()).isEqualTo(createStackTrace("com.example.Foo"));
  }

  @Test
  public void cyclesAreHandled() {
    SelfReferencingThrowable selfReferencingThrowable =
        new SelfReferencingThrowable("com.example.Foo", "org.junit.FilterMe");

    StackTraceCleaner.cleanStackTrace(selfReferencingThrowable);

    assertThat(selfReferencingThrowable.getStackTrace())
        .isEqualTo(createStackTrace("com.example.Foo"));
  }

  private static Throwable createThrowableWithStackTrace(String... classNames) {
    return createThrowableWithStackTrace(null, classNames);
  }

  private static Throwable createThrowableWithStackTrace(Throwable cause, String... classNames) {
    Throwable throwable = new RuntimeException(cause);
    StackTraceElement[] stackTrace = createStackTrace(classNames);
    throwable.setStackTrace(stackTrace);
    return throwable;
  }

  private static StackTraceElement[] createStackTrace(String... classNames) {
    StackTraceElement[] stackTrace = new StackTraceElement[classNames.length];
    for (int i = 0; i < classNames.length; i++) {
      stackTrace[i] = createStackTraceElement(classNames[i]);
    }
    return stackTrace;
  }

  private static StackTraceElement createStackTraceElement(String className) {
    return new StackTraceElement(className, "", "", -1);
  }

  private static StackTraceElement createCollapsedStackTraceElement(
      String frameworkName, int collapsed) {
    return new StackTraceElement(
        "[["
            + frameworkName
            + ": "
            + collapsed
            + " frames collapsed ("
            + StackTraceCleaner.CLEANER_LINK
            + ")]]",
        "",
        "",
        0);
  }

  private static class SelfReferencingThrowable extends Throwable {

    SelfReferencingThrowable(String... classNames) {
      setStackTrace(createStackTrace(classNames));
    }

    @Override
    public synchronized Throwable getCause() {
      return this;
    }
  }
}
