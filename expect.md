---
layout: default
title: "Truth: Expect"
---

1. auto-gen TOC:
{:toc}

[`Expect`](https://truth.dev/api/latest/com/google/common/truth/Expect.html) is
a JUnit `Rule` that collects [Truth] assertions so that failures are aggregated
over the course of a test method, rather than failing fast.

## Why you should use Expect

Suppose the assert phase of your test method has the lines

```java
    assertThat(result.getFoo()).isEqualTo(3);
    assertThat(result.isBar()).isTrue();
```

If either condition is false, the test method will fail. However, if *both*
conditions are false, only the first violation will be reported, because Truth
"fails fast".

With `Expect`, the code looks like this:

```java
  @Rule public final Expect expect = Expect.create();

  ...

    expect.that(result.getFoo()).isEqualTo(3);
    expect.that(result.isBar()).isTrue();
```

If a check fails, the `Expect` rule retains the failure message and continues.
The test method will then fail with a message that includes all the individual
failures. So the developer will be able to see both violations, and fix them in
fewer iterations.

## When you should avoid Expect

Sometimes, if an expectation is not met, test execution should stop.

*   Further checking might be irrelevant -- if this check fails, you know the
    rest will fail as well, and their error messages won't tell you anything
    more.

*   When testing sequential behavior, a failure might mean that the system under
    test didn't advance to the next step. Further error messages would be
    misleading.

In either case, the "terminal" check should be coded with Truth in the usual
way. If it fails, execution of the test method will halt, with a message that
includes the terminal condition and also any previous failures accrued by the
Expect rule.

## What about non-standard test subjects (Truth extensions)?

If you use [Truth extensions] such as [`ProtoTruth`], `expect.that()` needs to
know about the subject to provide the same methods:

```java
import static com.google.common.truth.extensions.proto.ProtoTruth.protos;

expect.about(protos()).that(myProto).isEqualToDefaultInstance();
```

[Truth]: https://truth.dev
[Truth extensions]: https://truth.dev/extension
[`ProtoTruth`]: https://truth.dev/protobufs
