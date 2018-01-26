---
layout: default
subtitle: Fluent assertions for Java
url: /truth
---

Truth makes your [test assertions](benefits#readable-assertions) and [failure
messages](benefits#readable-messages) more readable. [Similar](comparison) to
[AssertJ], it [natively supports](known_types) many JDK and [Guava] types, and
it is [extensible](extension) to others.

Truth is owned and maintained by the [Guava] team. It is used from the majority
of the tests in Google’s own codebase.


# How to use Truth

## 1. Add the appropriate dependency to your build file:

### Maven:

```xml
<dependency>
  <groupId>com.google.truth</groupId>
  <artifactId>truth</artifactId>
  <version>{{ site.version }}</version>
  <scope>test</scope>
</dependency>
```

### Gradle:

```groovy
buildscript {
  repositories.mavenLocal()
}
dependencies {
  testCompile "com.google.truth:truth:{{ site.version }}"
}
```

To use the Java 8 extensions, also include
`com.google.truth.extensions:truth-java8-extension:{{ site.version }}`.


## 2. Add static imports for Truth’s entry points:

```java
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat; // for assertions on Java 8 types
```

## 3. Write a test assertion:

```java
String string = "awesome";
assertThat(string).startsWith("awe");
assertWithMessage("Without me, it's just aweso").that(string).contains("me");

Iterable<Color> googleColors = googleLogo.getColors();
assertThat(googleColors)
    .containsExactly(BLUE, RED, YELLOW, BLUE, GREEN, RED)
    .inOrder();
```

If you’re using an IDE with autocompletion, it will suggest list of assertions
you can make about the given type. If not, consult the [API docs]. For example,
if you’re looking for assertions about a `Map`, look at the documentation for
[`MapSubject`].

# More information {#more-information}

*   Questions: Ask on [Stack Overflow] with the `google-truth` tag.
*   Bugs: [Github issues]
*   Source: [Github][source]

<!-- References -->

[source]: https://github.com/google/truth/tree/master/core/src/main/java/com/google/common/truth
[Github issues]: https://github.com/google/truth/issues
[Stack Overflow]: http://stackoverflow.com/questions/tagged/google-truth
[Guava]: http://github.com/google/guava
[API docs]: http://google.github.io/truth/api/latest/
[`MapSubject`]: https://google.github.io/truth/api/latest/com/google/common/truth/MapSubject
[Java Core Libraries Team]: https://www.reddit.com/r/java/comments/1y9e6t/ama_were_the_google_team_behind_guava_dagger/
[AssertJ]: http://joel-costigliola.github.io/assertj/

