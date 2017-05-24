---
layout: default
subtitle: We've made failure a strategy
url: /truth
---

Truth is an [open source][github], [fluent] testing framework for Java that is
designed to make your [test assertions](benefits#readable-assertions) and
[failure messages](benefits#readable-messages) more readable, as well as other
[benefits](benefits). It natively supports many JDK types (e.g., `Iterable`,
`String`, `Map`) and [Guava] types (e.g., `Optional`, `Multimap`, `Multiset`,
`Table`), and is also [extensible](extension) to new types (`YourCustomType`).
See all of the known types [here](known_types).

You can also [read Truth's source directly][source], view its [API docs], and
[compare it to common alternatives](comparison).

# How to use Truth

## 1. Add the appropriate dependency to your build file:

### Maven:

```xml
<dependency>
  <groupId>com.google.truth</groupId>
  <artifactId>truth</artifactId>
  <version>0.33</version>
</dependency>
```

### Gradle:

```groovy
buildscript {
  repositories.mavenLocal()
}
dependencies {
  testCompile "com.google.truth:truth:0.33"
}
```


## 2. Add static imports for Truth’s entry point(s):

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

If you’re using an IDE with autocompletion, you should be able to hit `<TAB>`
and get a list of assertions you can make about the given type. If your IDE does
not support autocompletion, please consult the documentation listed above (e.g.,
if you’re looking for assertions about a `Map`, look at the documentation for
[`MapSubject`]).

# More information {#more-information}

Truth is owned and maintained by the [Java Core Libraries Team]. More
information can be found here:

*   [Stack Overflow]

*   [Github issues]

<!-- References -->

[github]: https://github.com/google/truth
[source]: https://github.com/google/truth
[fluent]: http://en.wikipedia.org/wiki/Fluent_interface
[Github issues]: https://github.com/google/truth/issues
[Stack Overflow]: http://stackoverflow.com/questions/tagged/google-truth
[Guava]: http://github.com/google/guava
[API docs]: http://google.github.io/truth/api/latest/
[`MapSubject`]: https://google.github.io/truth/api/latest/com/google/common/truth/MapSubject
[Java Core Libraries Team]: https://www.reddit.com/r/java/comments/1y9e6t/ama_were_the_google_team_behind_guava_dagger/

