Truth
=====
***We've made failure a strategy***

**Main Website:** *[truth0.github.io](http://truth0.github.io/)* &nbsp;
**Continuous Integration:** [![Build Status](https://secure.travis-ci.org/truth0/truth.png?branch=master)](https://travis-ci.org/truth0/truth) <br />
**Latest Release:** *0.20* &nbsp; 
**Latest Artifact:** *org.junit.contrib:truth:jar:0.20* <br />

**Note:** Truth is subject to change and prior to 1.0, may introduce 
breaking changes.  We're getting closer to "prime time" but please 
use with caution before release 1.0.  Consider Truth in alpha.

What is Truth?
--------------

Truth is an assertion/proposition framework appropriate for testing, inspired by FEST, and 
driven by some extensibility needs, written nearly entirely by Google employees in their spare
time or contributing in their capacity as Java core librarians.

Truth can be used in place of JUnit's assertions, FEST, or Hamcrest's matchers, or it can be
used alongside where other approaches seem more suitable.  The basic form is similar to that
of FEST

```java
ASSERT.that(foo).isEqualTo(bar);
```

but it's power really shines where you have custom types that need complex propositions

```java
ASSUME.about(processors()).that(myAnnotationProcessor).handlesAnnotation(Foo.class);
ASSERT.about(javaSources()).that(asList(source1, source2, source3))
    .processedWith(myAnnotationProcessor)
    .compilesWithoutError().and()
    .generatesSources(expected1, expected2);
    
ASSERT.about(javaSources()).that(asList(source4, source5))
    .processedWith(myAnnotationProcessor)
    .failsToCompile()
        .withErrorContaining("expected error!").in(source4).onLine(18).atColumn(1).and()
        .withErrorContaining("expected error!").in(source5).onLine(12); // less specific
```

Instructions, documentation, and other information
----------------

The full documentation and website for Truth is available at http://truth0.github.io.

License
----------------

Truth is licensed under the open-source Apache 2.0 license.  Any contributions must
be provided with such a license.  

Acknowledgements
----------------

Thanks to Github and Travis-CI for having a strong commitment to open-source, and 
providing us with tools so we can provide others with code.  And thanks to Google 
for [Guava](http://code.google.com/p/guava-libraries "Guava"), and for encouraging
us to try to solve problems in better ways and  share that with the world.

Also thanks to the authors of JUnit, TestNG, Hamcrest, FEST, and others for creating
testing tools that let us write high-quality code, for inspiring this work and for 
moving the ball forward in the field of automated software testing.  This project
works with, works alongside, and sometimes works in competition with the above
tools, but owes a debt that everyone owes to those gone before.  They paved the 
way, and we hope this contribution is helpful to the field.
