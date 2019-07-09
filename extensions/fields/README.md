# Reflective Field Comparisons for Truth

###### Brought to you by Square, Inc.

## Overview

Sometimes there are types without proper equality semantics, or odd cases where introspecting a
variable not available via an API is necessary. It's often an anti-patern, but can be a useful
hedge in edge cases, during migrations, etc.

The **truth-fields-extension** allows you to use a custom `Subject` and `Correspondence` 
instances to do field-by-field comparisons (in regular and list situations respectively).

> Note: Examples here assume static importing of the subject factory, or correspondence.

For example, comparing a single (complex) instance with another: 

```java
public class MyTest {
    // assume Foo, Bar, and Blah types
    @Test public void compareBlahs() {
        assertAbout(fields())
            .that(Blah(Bar(Foo(B("A", "B")), "q")))
            .comparingAllFields()
            .recursively(/* respectDeclaredEquals = */ false /* default = true */)
            .isNotEqualTo(Blah(Bar(Foo(B("NotA", "B")), "a")));
    }
}
```


```java
public class MyTest {
    // assume Foo
    @Test public void correspondence_with_all_fields_success() {
        List<Foo> actual = asList(
            new Foo("foo", "bar"),
            new Foo("baz", "blah"),
            new Foo("qu", "arr"));
        List<Foo> expected = asList(
            new Foo("baz", "blah"),
            new Foo("foo", "bar"), // intentionally out of order
            new Foo("qu", "arr"));

        assertThat(actual)
            .comparingElementsUsing(flatFieldByField()) // can also do deep comparisons
            .containsExactlyElementsIn(expected);
    }
}
```

## How to use it

### Build setup

Requires at least Truth at the same version number.

#### Maven:

```xml
<dependency>
  <groupId>com.google.truth.extensions</groupId>
  <artifactId>truth-fields-extension</artifactId>
  <version>1.0</version>
  <scope>test</scope>
</dependency>
```

#### Gradle:

```groovy
repositories {
  mavenCentral()
}
dependencies {
  testImplementation "com.google.truth.extensions:truth-fields-extension:1.0"
}
```

Then use it just like you would use Truth, but in place of `assertThat(...)` use `assertAbout(...)`
using one of the `Correspondence` instances (see below).  

### Kotlin vs. Java : You Decide

The subject and correspondence is written in Kotlin, and so can be invoked using
named parameters, and using normal Kotlin conveniences. Examples given here are
written in Java since the tests are mostly written in Kotlin. Where applicable,
the Kotlin has been annotated for ease of Java-based consumption, but this should
not affect Kotlin usage.

### Configuring the Subject

When simply comparing two objects, use the field comparing subject like so:

```java
assertAbout(fields())
    .that(actual)
    .comparingAllFields()
    .isEqualTo(expected);
```

#### Selecting fields

The field subject can be configured to compare different sets of fields, and this must be specified.
The simplest is to compare all fields (except fields on Object in Java or Any
in Kotlin). It can also be configured to compare only a whitelisted set of fields, or to exclude
certain fields:


```java
// compares all fields, except fields named "foo" and "bar"
assertAbout(fields())
    .that(actual)
    .comparingAllFields()
    .except("foo", "bar")
    .isEqualTo(expected);

// compares fields named "bar" and "baz" but excludes "bar" (and "foo" which wasn't used anyway)
assertAbout(fields())
    .that(actual)
    .comparingFields("bar", "baz")
    .except("foo", "bar")
    .isEqualTo(expected);
```

Exclusions override inclusions, and typical set semantics are used to work out the field list.
Comparing on fields that don't exist in the type compared will result in an
`IllegalArgumentException`.  Excluding non-existent fields will have no effect.

### Comparing collections of objects by field type.

The `truth-fields-extension` also provides a `Correspondence` for field-by-field evaluation,
which can be used for collections comparisons like so:

```java
        List<Foo> actual =
            asList(new Foo("foo", "bar"), new Foo("baz", "blah"), new Foo("qu", "arr"));
        List<Foo> expected =
            asList(new Foo("foo", "bar"), new Foo("baz", "blah"), new Foo("qu", "arr"));

        assertThat(actual)
            .comparingElementsUsing(flatFieldByField())
            .containsExactlyElementsIn(expected);
        // or
        assertThat(actual)
            .comparingElementsUsing(deepFieldByField(/* respect declared equals*/ true)))
            .containsExactlyElementsIn(expected);
```

### Cyclic object references

Objects with fields that participate in a reference cycle may be compared, though there
are a few subtleties. 

Firstly, positive assertions will trigger only if the same types are being passed to
the assertion. That is comparing `Foo` with `Foo` will work as expected, but comparing
`Foo` with `Bar`, even if Foo and Bar are in a cycle containing the same field values.

Secondly, while the comparison framework can handle cycles, it will throw a stack-overflow
error messages if the `toString()` representations are not built to account for the cycle.
This would, however, be an issue with any assertion system that uses the objects' `toString()`
in its error messages.

## License

```text
License Copyright 2019 Square, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
or implied. See the License for the specific language governing permissions and limitations under
the License.
```
