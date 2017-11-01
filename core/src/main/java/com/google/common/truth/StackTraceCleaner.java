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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/** Utility that cleans stack traces to remove noise from common frameworks. */
final class StackTraceCleaner {

  static final String CLEANER_LINK = "https://goo.gl/aH3UyP";

  /**
   * Cleans the stack trace on the given {@link Throwable}, replacing the original stack trace
   * stored on the instance (see {@link Throwable#setStackTrace(StackTraceElement[])}).
   *
   * <p>Strips Truth stack frames from the top and JUnit framework and reflective call frames from
   * the bottom. Collapses the frames for various frameworks in the middle of the trace as well.
   */
  static void cleanStackTrace(Throwable throwable) {
    new StackTraceCleaner(throwable).cleanStackTrace(Sets.<Throwable>newIdentityHashSet());
  }

  private final Throwable throwable;
  private final List<StackTraceElementWrapper> cleanedStackTrace =
      new ArrayList<StackTraceElementWrapper>();
  private StackTraceElementWrapper lastStackFrameElementWrapper = null;
  private StackFrameType currentStreakType = null;
  private int currentStreakLength = 0;

  /**
   * A new instance is instantiated for each throwable to be cleaned. This is so that helper methods
   * can make use of instance variables describing the state of the cleaning process.
   */
  private StackTraceCleaner(Throwable throwable) {
    this.throwable = throwable;
  }

  // TODO(user): Add this to the test runners so that we clean all stack traces, not just
  // those of exceptions originating in Truth.
  /** Cleans the stack trace on {@code throwable}, replacing the trace that was originally on it. */
  private void cleanStackTrace(Set<Throwable> seenThrowables) {
    // Stack trace cleaning can be disabled using a system property.
    if (Platform.isStackTraceCleaningDisabled()) {
      return;
    }

    // Prevent infinite recursion if there is a reference cycle between Throwables.
    if (seenThrowables.contains(throwable)) {
      return;
    }
    seenThrowables.add(throwable);

    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      StackTraceElementWrapper stackTraceElementWrapper =
          new StackTraceElementWrapper(
              stackTraceElement, StackFrameType.forClassName(stackTraceElement.getClassName()));

      // Always keep frames that might be useful.
      if (stackTraceElementWrapper.getStackFrameType() == StackFrameType.NEVER_REMOVE) {
        endStreak();
        cleanedStackTrace.add(stackTraceElementWrapper);
        continue;
      }

      // Filter out Truth framework frames from the top of the stack.
      if (stackTraceElementWrapper.getStackFrameType() == StackFrameType.TRUTH_FRAMEWORK
          && cleanedStackTrace.isEmpty()) {
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
      new StackTraceCleaner(throwable.getCause()).cleanStackTrace(seenThrowables);
    }
    for (Throwable suppressed : Platform.getSuppressed(throwable)) {
      new StackTraceCleaner(suppressed).cleanStackTrace(seenThrowables);
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
      cleanedStackTrace.add(lastStackFrameElementWrapper);
    } else {
      // Add a single frame to the result summarizing the streak of framework frames
      cleanedStackTrace.add(createStreakReplacementFrame(currentStreakType, currentStreakLength));
    }

    clearStreak();
  }

  /** Resets the streak counter. */
  private void clearStreak() {
    currentStreakType = null;
    currentStreakLength = 0;
  }

  /**
   * Wrapper around a {@link StackTraceElement} for calculating and holding the metadata used to
   * clean the stack trace.
   */
  private static class StackTraceElementWrapper {

    private final StackTraceElement stackTraceElement;
    private final StackFrameType stackFrameType;

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
    TRUTH_FRAMEWORK("Truth framework", "com.google.common.truth."),
    TEST_FRAMEWORK(
        "Testing framework",
        "junit.",
        "org.junit.",
        "com.google.testing.junit.",
        "com.google.testing.testsize.",
        "com.google.testing.util."),
    REFLECTION("Reflective call", "java.lang.reflect.", "sun.reflect."),
    CONCURRENT_FRAMEWORK(
        "Concurrent framework",
        "com.google.tracing.CurrentContext",
        "com.google.common.util.concurrent.",
        "java.util.concurrent.ForkJoin");

    /** Helper method to determine the frame type from the fully qualified class name. */
    private static StackFrameType forClassName(String fullyQualifiedClassName) {
      // Never remove the frames from a test class. These will probably be the frame of a failing
      // assertion.
      if (fullyQualifiedClassName.endsWith("Test")) {
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
        if (fullyQualifiedClassName.startsWith(prefix)) {
          return true;
        }
      }
      return false;
    }
  }
}
