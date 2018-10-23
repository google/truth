# Truth

[![Main Site][gh-pages-shield]][gh-pages-link]
[![Build Status][travis-shield]][travis-link]
[![Maven Release][maven-shield]][maven-link]
[![Stackoverflow][stackoverflow-shield]][stackoverflow-link]

## What is Truth?

Truth makes your [test assertions] and [failure messages] more readable.
[Similar][comparison] to [AssertJ], it [natively supports][known_types] many JDK
and [Guava] types, and it is [extensible][extension] to others.

Compare these example JUnit assertions...

```java
assertEquals(b, a);
assertTrue(c);
assertTrue(d.contains(a));
assertTrue(d.contains(a) && d.contains(b));
assertTrue(d.contains(a) || d.contains(b) || d.contains(c));
```

...to their Truth equivalents...

```java
assertThat(a).isEqualTo(b);
assertThat(c).isTrue();
assertThat(d).contains(a);
assertThat(d).containsAllOf(a, b);
assertThat(d).containsAnyOf(a, b, c);
```

### Advantages of Truth

* aligns all the "actual" values on the left
* produces more detailed failure messages
* provides richer operations (like [IterableSubject#containsExactly](https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/IterableSubject.java))

## Reference

Truth is owned and maintained by the [Guava] team. It is used from the majority
of the tests in Google’s own codebase.

Read more at [the main website](https://google.github.io/truth).

<!-- references -->

[test assertions]: https://google.github.io/truth/benefits#readable-assertions
[failure messages]: https://google.github.io/truth/benefits#readable-messages
[comparison]: https://google.github.io/truth/comparison
[AssertJ]: http://joel-costigliola.github.io/assertj/
[known_types]: https://google.github.io/truth/known_types
[extension]: https://google.github.io/truth/extension
[Guava]: https://github.com/google/guava
[gh-pages-shield]: https://img.shields.io/badge/main%20site-google.github.io/truth-ff55ff.png?style=flat
[gh-pages-link]: https://google.github.io/truth/
[travis-shield]: https://img.shields.io/travis/google/truth.png
[travis-link]: https://travis-ci.org/google/truth
[maven-shield]: https://img.shields.io/maven-central/v/com.google.truth/truth.png
[maven-link]: https://search.maven.org/artifact/com.google.truth/truth
[stackoverflow-shield]: https://img.shields.io/badge/stackoverflow-truth-5555ff.png?style=flat
[stackoverflow-link]: https://stackoverflow.com/questions/tagged/google-truth
