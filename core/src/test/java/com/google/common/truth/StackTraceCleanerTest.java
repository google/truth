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

import com.google.common.annotations.GwtIncompatible;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackTraceCleaner}. */
@RunWith(JUnit4.class)
public class StackTraceCleanerTest extends BaseSubjectTestCase {

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
  @GwtIncompatible
  /*
   * Stripping doesn't actually work under j2cl (and presumably GWT):
   * StackTraceElement.getClassName() doesn't have real data. Some data is available in toString(),
   * albeit along the lines of
   * "AssertionErrorWithCause.m_createError__java_lang_String_$pp_java_lang." StackTraceCleaner
   * could maybe look through the toString() representations to count how many frames to strip, but
   * that's a bigger project. (While we're at it, we could strip the j2cl-specific boilerplate from
   * the _bottom_ of the stack, too.) And sadly, it's not necessarily as simple as looking at just
   * _class_ names: The stripping is applied to causes, too, and it's possible for a cause to
   * legitimately contain an exception created inside a class like Throwable -- e.g., x.initCause(x)
   * will throw an exception, and it would be weird (though maybe tolerable) for us to strip that.
   *
   * Also note that j2cl includes some extra frames at the _top_, even beyond the ones that we try
   * to strip: b/71355096
   */
  public void assertionsActuallyUseCleaner() {
    expectFailure.whenTesting().that(1).isEqualTo(2);
    assertThat(expectFailure.getFailure().getStackTrace()[0].getClassName())
        .isEqualTo(getClass().getName());
  }

  @Test
  @GwtIncompatible
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
  @GwtIncompatible("Class.forName")
  public void mixedStreaks() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.google.common.truth.IterableSubject",
            "com.google.common.truth.MapSubject",
            "com.example.MyTest",
            "junit.Foo",
            "org.junit.Bar",
            "java.lang.reflect.Car",
            "sun.reflect.Dar",
            "com.google.testing.testsize.Dar",
            "com.google.testing.util.Far",
            "com.example.Jar");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.example.MyTest"),
              createCollapsedStackTraceElement("Testing framework", 2),
              createCollapsedStackTraceElement("Reflective call", 2),
              createCollapsedStackTraceElement("Testing framework", 2),
              createStackTraceElement("com.example.Jar"),
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
            "com.google.testing.util.ShouldStrip1",
            "com.google.testing.util.ShouldStrip2",
            "com.google.testing.util.ShouldNotStripTest");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createCollapsedStackTraceElement("Testing framework", 2),
              createStackTraceElement("com.google.testing.util.ShouldNotStripTest"),
            });
  }

  @Test
  @GwtIncompatible("Class.forName")
  public void allFramesAboveStandardSubjectBuilderCleaned() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.google.random.Package",
            "com.google.common.base.collection.ImmutableMap",
            "com.google.common.truth.StandardSubjectBuilder",
            "com.google.example.SomeClass");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.google.example.SomeClass"),
            });
  }

  @Test
  @GwtIncompatible("Class.forName")
  public void allFramesAboveSubjectCleaned() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.google.random.Package",
            "com.google.common.base.collection.ImmutableMap",
            "com.google.common.truth.StringSubject",
            "com.google.example.SomeClass");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.google.example.SomeClass"),
            });
  }

  /**
   * This scenario where truth class is called directly without any subject's subclass or {@link
   * StandardSubjectBuilder} in the call stack should not happen in practical, testing anyway to
   * make sure even if it does, the behavior should match expectation.
   */
  @Test
  public void truthFrameWithOutSubject_shouldNotCleaned() {
    Throwable throwable =
        createThrowableWithStackTrace(
            "com.google.random.Package",
            // two or more truth frame will trigger string matching mechenism to got it collapsed
            "com.google.common.truth.FailureMetadata",
            "com.google.example.SomeClass");

    StackTraceCleaner.cleanStackTrace(throwable);

    assertThat(throwable.getStackTrace())
        .isEqualTo(
            new StackTraceElement[] {
              createStackTraceElement("com.google.random.Package"),
              createStackTraceElement("com.google.common.truth.FailureMetadata"),
              createStackTraceElement("com.google.example.SomeClass"),
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
    if (Platform.isAndroid()) {
      return; // suppressed exceptions aren't supported under Ice Cream Sandwich, where we test
    }
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
    if (Platform.isAndroid()) {
      return; // suppressed exceptions aren't supported under Ice Cream Sandwich, where we test
    }
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
