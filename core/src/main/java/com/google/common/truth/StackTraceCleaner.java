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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Utility that cleans stack traces to remove noise from common frameworks. */
@GwtIncompatible
@J2ktIncompatible
final class StackTraceCleaner {

  static final String CLEANER_LINK = "https://goo.gl/aH3UyP";

  /**
   * <b>Call {@link Platform#cleanStackTrace} rather than calling this directly.</b>
   *
   * <p>Cleans the stack trace on the given {@link Throwable}, replacing the original stack trace
   * stored on the instance (see {@link Throwable#setStackTrace(StackTraceElement[])}).
   *
   * <p>Removes Truth stack frames from the top and JUnit framework and reflective call frames from
   * the bottom. Collapses the frames for various frameworks in the middle of the trace as well.
   */
  static void cleanStackTrace(Throwable throwable) {
    new StackTraceCleaner(throwable).clean(Sets.<Throwable>newIdentityHashSet());
  }

  private final Throwable throwable;
  private final List<StackTraceElementWrapper> cleanedStackTrace = new ArrayList<>();
  private @Nullable StackTraceElementWrapper lastStackFrameElementWrapper = null;
  private @Nullable StackFrameType currentStreakType = null;
  private int currentStreakLength = 0;

  /**
   * A new instance is instantiated for each throwable to be cleaned. This is so that helper methods
   * can make use of instance variables describing the state of the cleaning process.
   */
  private StackTraceCleaner(Throwable throwable) {
    this.throwable = throwable;
  }

  // TODO(b/135924708): Add this to the test runners so that we clean all stack traces, not just
  // those of exceptions originating in Truth.
  /** Cleans the stack trace on {@code throwable}, replacing the trace that was originally on it. */
  @SuppressWarnings("SetAll") // not available under old versions of Android
  private void clean(Set<Throwable> seenThrowables) {
    // Stack trace cleaning can be disabled using a system property.
    if (isStackTraceCleaningDisabled()) {
      return;
    }

    /*
     * TODO(cpovirk): Consider wrapping this whole method in a try-catch in case there are any bugs.
     * It would be a shame for us to fail to report the "real" assertion failure because we're
     * instead reporting a bug in Truth's cosmetic stack cleaning.
     */

    // Prevent infinite recursion if there is a reference cycle between Throwables.
    if (seenThrowables.contains(throwable)) {
      return;
    }
    seenThrowables.add(throwable);

    StackTraceElement[] stackFrames = throwable.getStackTrace();

    int stackIndex = stackFrames.length - 1;
    for (; stackIndex >= 0 && !isTruthEntrance(stackFrames[stackIndex]); stackIndex--) {
      // Find first frame that enters Truth's world, and remove all above.
    }
    stackIndex += 1;

    int endIndex = 0;
    for (;
        endIndex < stackFrames.length && !isJUnitIntrastructure(stackFrames[endIndex]);
        endIndex++) {
      // Find last frame of setup frames, and remove from there down.
    }
    /*
     * If the stack trace would be empty, the error was probably thrown from "JUnit infrastructure"
     * frames. Keep those frames around (though much of JUnit itself and related startup frames will
     * still be removed by the remainder of this method) so that the user sees a useful stack.
     */
    if (stackIndex >= endIndex) {
      endIndex = stackFrames.length;
    }

    for (; stackIndex < endIndex; stackIndex++) {
      StackTraceElementWrapper stackTraceElementWrapper =
          new StackTraceElementWrapper(stackFrames[stackIndex]);
      // Always keep frames that might be useful.
      if (stackTraceElementWrapper.getStackFrameType() == StackFrameType.NEVER_REMOVE) {
        endStreak();
        cleanedStackTrace.add(stackTraceElementWrapper);
        continue;
      }

      // Otherwise, process the current frame for collapsing
      addToStreak(stackTraceElementWrapper);

      lastStackFrameElementWrapper = stackTraceElementWrapper;
    }

    // Close out the streak on the bottom of the stack.
    endStreak();

    // Filter out testing framework and reflective calls from the bottom of the stack
    ListIterator<StackTraceElementWrapper> iterator =
        cleanedStackTrace.listIterator(cleanedStackTrace.size());
    while (iterator.hasPrevious()) {
      StackTraceElementWrapper stackTraceElementWrapper = iterator.previous();
      if (stackTraceElementWrapper.getStackFrameType() == StackFrameType.TEST_FRAMEWORK
          || stackTraceElementWrapper.getStackFrameType() == StackFrameType.REFLECTION) {
        iterator.remove();
      } else {
        break;
      }
    }

    // Replace the stack trace on the Throwable with the cleaned one.
    StackTraceElement[] result = new StackTraceElement[cleanedStackTrace.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = cleanedStackTrace.get(i).getStackTraceElement();
    }
    throwable.setStackTrace(result);

    // Recurse on any related Throwables that are attached to this one
    if (throwable.getCause() != null) {
      new StackTraceCleaner(throwable.getCause()).clean(seenThrowables);
    }
    for (Throwable suppressed : Platform.getSuppressed(throwable)) {
      new StackTraceCleaner(suppressed).clean(seenThrowables);
    }
  }

  /**
   * Either adds the given frame to the running streak or closes out the running streak and starts a
   * new one.
   */
  private void addToStreak(StackTraceElementWrapper stackTraceElementWrapper) {
    if (stackTraceElementWrapper.getStackFrameType() != currentStreakType) {
      endStreak();
      currentStreakType = stackTraceElementWrapper.getStackFrameType();
      currentStreakLength = 1;
    } else {
      currentStreakLength++;
    }
  }

  /** Ends the current streak, adding a summary frame to the result. Resets the streak counter. */
  private void endStreak() {
    if (currentStreakLength == 0) {
      return;
    }

    if (currentStreakLength == 1) {
      // A single frame isn't a streak. Just include the frame as-is in the result.
      cleanedStackTrace.add(checkNotNull(lastStackFrameElementWrapper));
    } else {
      // Add a single frame to the result summarizing the streak of framework frames
      cleanedStackTrace.add(
          createStreakReplacementFrame(checkNotNull(currentStreakType), currentStreakLength));
    }

    clearStreak();
  }

  /** Resets the streak counter. */
  private void clearStreak() {
    currentStreakType = null;
    currentStreakLength = 0;
  }

  private static final ImmutableSet<String> SUBJECT_CLASS =
      ImmutableSet.of(
          Subject.class.getCanonicalName());

  private static final ImmutableSet<String> STANDARD_SUBJECT_BUILDER_CLASS =
      ImmutableSet.of(StandardSubjectBuilder.class.getCanonicalName());

  private static boolean isTruthEntrance(StackTraceElement stackTraceElement) {
    return isFromClassOrClassNestedInside(stackTraceElement, SUBJECT_CLASS)
        /*
         * Don't match classes _nested inside_ StandardSubjectBuilder because that would match
         * Expect's Statement implementation. While we want to strip everything from there _down_, we
         * don't want to strip everything from there _up_ (which would strip the test class itself!).
         *
         * (StandardSubjectBuilder is listed here only for its fail() methods, anyway, so we don't
         * have to worry about nested classes like we do with Subject.)
         */
        || isFromClassDirectly(stackTraceElement, STANDARD_SUBJECT_BUILDER_CLASS);
  }

  private static final ImmutableSet<String> JUNIT_INFRASTRUCTURE_CLASSES =
      ImmutableSet.of("org.junit.runner.Runner", "org.junit.runners.model.Statement");

  private static boolean isJUnitIntrastructure(StackTraceElement stackTraceElement) {
    // It's not clear whether looking at nested classes here is useful, harmful, or neutral.
    return isFromClassOrClassNestedInside(stackTraceElement, JUNIT_INFRASTRUCTURE_CLASSES);
  }

  private static boolean isFromClassOrClassNestedInside(
      StackTraceElement stackTraceElement, ImmutableSet<String> recognizedClasses) {
    Class<?> stackClass;
    try {
      stackClass = loadClass(stackTraceElement.getClassName());
    } catch (ClassNotFoundException e) {
      return false;
    }
    try {
      for (; stackClass != null; stackClass = stackClass.getEnclosingClass()) {
        for (String recognizedClass : recognizedClasses) {
          if (isSubtypeOf(stackClass, recognizedClass)) {
            return true;
          }
        }
      }
    } catch (Error e) {
      if (e.getClass().getName().equals("com.google.j2objc.ReflectionStrippedError")) {
        /*
         * We're running under j2objc without reflection. Skip testing the enclosing classes. At
         * least we tested the class itself against all the recognized classes.
         *
         * TODO(cpovirk): The smarter thing might be to guess the name of the enclosing classes by
         * removing "$Foo" from the end of the name. But this should be good enough for now.
         */
        return false;
      }
      if (e instanceof IncompatibleClassChangeError) {
        // OEM class-loading bug? https://issuetracker.google.com/issues/37045084
        return false;
      }
      throw e;
    }
    return false;
  }

  private static boolean isSubtypeOf(@Nullable Class<?> subclass, String superclass) {
    for (; subclass != null; subclass = checkNotNull(subclass).getSuperclass()) {
      if (subclass.getCanonicalName() != null && subclass.getCanonicalName().equals(superclass)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isFromClassDirectly(
      StackTraceElement stackTraceElement, ImmutableSet<String> recognizedClasses) {
    Class<?> stackClass;
    try {
      stackClass = loadClass(stackTraceElement.getClassName());
    } catch (ClassNotFoundException e) {
      return false;
    }
    for (String recognizedClass : recognizedClasses) {
      if (isSubtypeOf(stackClass, recognizedClass)) {
        return true;
      }
    }
    return false;
  }

  // Using plain Class.forName can cause problems.
  /*
   * TODO(cpovirk): Consider avoiding classloading entirely by reading classes with ASM. But that
   * won't work on Android, so we might ultimately need classloading as a fallback. Another
   * possibility is to load classes in a fresh, isolated classloader. However, that requires
   * creating a list of jars to load from, which is fragile and would also require special handling
   * under Android. If we're lucky, this new solution will just work: The classes should already be
   * loaded, anyway, since they appear on the stack, so we just have to hope that we have the right
   * classloader.
   */
  private static Class<?> loadClass(String name) throws ClassNotFoundException {
    ClassLoader loader =
        firstNonNull(
            currentThread().getContextClassLoader(), StackTraceCleaner.class.getClassLoader());
    return loader.loadClass(name);
  }

  /**
   * Wrapper around a {@link StackTraceElement} for calculating and holding the metadata used to
   * clean the stack trace.
   */
  private static class StackTraceElementWrapper {

    private final StackTraceElement stackTraceElement;
    private final StackFrameType stackFrameType;

    /** Creates a wrapper with the given frame with frame type inferred from frame's class name. */
    StackTraceElementWrapper(StackTraceElement stackTraceElement) {
      this(stackTraceElement, StackFrameType.forClassName(stackTraceElement.getClassName()));
    }

    /** Creates a wrapper with the given frame and the given frame type. */
    StackTraceElementWrapper(StackTraceElement stackTraceElement, StackFrameType stackFrameType) {
      this.stackTraceElement = stackTraceElement;
      this.stackFrameType = stackFrameType;
    }

    /** Returns the type of this frame. */
    StackFrameType getStackFrameType() {
      return stackFrameType;
    }

    /** Returns the wrapped {@link StackTraceElement}. */
    StackTraceElement getStackTraceElement() {
      return stackTraceElement;
    }
  }

  private static StackTraceElementWrapper createStreakReplacementFrame(
      StackFrameType stackFrameType, int length) {
    return new StackTraceElementWrapper(
        new StackTraceElement(
            "[["
                + stackFrameType.getName()
                + ": "
                + length
                + " frames collapsed ("
                + CLEANER_LINK
                + ")]]",
            "",
            "",
            0),
        stackFrameType);
  }

  /**
   * Enum of the package or class-name based categories of stack frames that might be removed or
   * collapsed by the cleaner.
   */
  private enum StackFrameType {
    NEVER_REMOVE("N/A"),
    TEST_FRAMEWORK(
        "Testing framework",
        "junit",
        "org.junit",
        "androidx.test.internal.runner",
        "com.github.bazel_contrib.contrib_rules_jvm.junit5",
        "com.google.testing.junit",
        "com.google.testing.testsize",
        "com.google.testing.util"),
    REFLECTION("Reflective call", "java.lang.reflect", "jdk.internal.reflect", "sun.reflect"),
    CONCURRENT_FRAMEWORK(
        "Concurrent framework",
        "com.google.tracing.CurrentContext",
        "com.google.common.util.concurrent",
        "java.util.concurrent.ForkJoin");

    /** Helper method to determine the frame type from the fully qualified class name. */
    private static StackFrameType forClassName(String fullyQualifiedClassName) {
      // Never remove the frames from a test class. These will probably be the frame of a failing
      // assertion.
      // TODO(cpovirk): This is really only for tests in Truth itself, so this doesn't matter yet,
      // but.... If the Truth tests someday start calling into nested classes, we may want to add:
      // || fullyQualifiedClassName.contains("Test$")
      if (fullyQualifiedClassName.endsWith("Test")
          && !fullyQualifiedClassName.equals(
              "androidx.test.internal.runner.junit3.NonLeakyTestSuite$NonLeakyTest")) {
        return StackFrameType.NEVER_REMOVE;
      }

      for (StackFrameType stackFrameType : StackFrameType.values()) {
        if (stackFrameType.belongsToType(fullyQualifiedClassName)) {
          return stackFrameType;
        }
      }

      return StackFrameType.NEVER_REMOVE;
    }

    private final String name;
    private final ImmutableList<String> prefixes;

    /**
     * Each type of stack frame has a name of the summary displayed in the cleaned trace.
     *
     * <p>Most also have a set of fully qualified class name prefixes that identify when a frame
     * belongs to this type.
     */
    StackFrameType(String name, String... prefixes) {
      this.name = name;
      this.prefixes = ImmutableList.copyOf(prefixes);
    }

    /** Returns the name of this frame type to display in the cleaned trace */
    String getName() {
      return name;
    }

    /**
     * Returns true if the given frame belongs to this frame type based on the package and/or class
     * name of the frame.
     */
    boolean belongsToType(String fullyQualifiedClassName) {
      for (String prefix : prefixes) {
        // TODO(cpovirk): Should we also check prefix + "$"?
        if (fullyQualifiedClassName.equals(prefix)
            || fullyQualifiedClassName.startsWith(prefix + ".")) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Returns true if stack trace cleaning is explicitly disabled in a system property. This switch
   * is intended to be used when attempting to debug the frameworks which are collapsed or filtered
   * out of stack traces by the cleaner.
   */
  private static boolean isStackTraceCleaningDisabled() {
    // Reading system properties might be forbidden.
    try {
      return Boolean.parseBoolean(
          System.getProperty("com.google.common.truth.disable_stack_trace_cleaning"));
    } catch (SecurityException e) {
      // Hope for the best.
      return false;
      // TODO(cpovirk): Log a warning? Or is that likely to trigger other violations?
    }
  }
}
