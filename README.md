Truth
=====
<em><strong>We've made failure a strategy</strong></em>

Introduction
------------

Truth is a proposition framework suitable for testing assertions 
and assumptions.  Truth adopts a fluent style for your test 
propositions, is extensible in several ways, supports IDE 
completion/discovery of available propositions, and supports 
different responses to un-true propositions.  Truth can be used 
to declare assumptions (skip the test if they fail), assertions 
(fail the test), and expectations (continue but report errors 
and fail at the end).

While intended to work with JUnit, Truth can be used with other
testing framework with minimal effort.  Truth is released as
a maven artifact through a custom repository (it will be released
to repo1.maven.org soon), and is licensed with the Apache 2.0
open-source license.  As such, you are free to use it or modify
it subject only to the terms in that license.

Installation
------------

To prepare to use Truth, declare this dependency:

> &lt;dependency>
>   &lt;groupId>truth&lt;/groupId>
>   &lt;artifactId>truth&lt;/artifactId>
>   &lt;version>0.7&lt;/version>
> &lt;/dependency>

and add this repository section to your pom or to a parent 
pom.xml file. (for now - this requirement will later be removed.

> &lt;repositories>
>   &lt;repository>
>     &lt;id>truthi-repo&lt;/id>
>     &lt;url>https://raw.github.com/truth0/repo/master&lt;/url>
>   &lt;/repository>
> &lt;/repositories>

or download the jar directly from the link below and add it to
your tests classpath

> https://raw.github.com/truth0/repo/master/org/junit/contrib/truth/0.7.0/truth-0.7.0.jar




