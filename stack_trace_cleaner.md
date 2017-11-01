---
subtitle: Stack Trace Cleaner
layout: default
url: /stack_trace_cleaner
---

1. auto-gen TOC:
{:toc}

Stack traces for failures reported by Truth are cleaned so that they are more
readable and useful to developers.

## The Effects of Cleaning

Categories of stack frames that commonly clutter stack traces are identified by
package name prefixes. These categories are defined by
StackTraceCleaner.StackFrameType.

If two or more adjacent stack frames belong to the same category, the entire
series of those frames will be collapsed into a single frame summarizing what
the frames were doing (ex: "Reflective call").

Some frames are not collapsed and are instead stripped from the stack entirely.
Frames for Truth are removed from the top of the stack so that the first
displayed frame is likely the failing assertion. Testing framework (JUnit, etc.)
and reflective call frames are removed from the bottom of the stack, since these
will not be helpful in determining why a test failed.

## Disable Stack Trace Cleaning

Stack trace cleaning is enabled by default. To disable the cleaning and have
Truth report the original stack traces, set the system property
com.google.common.truth.disable_stack_trace_cleaning to true.

<!-- References -->

[`StackTraceCleaner`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/StackTraceCleaner.java
