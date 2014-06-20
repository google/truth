---
subtitle: Questions and Answers
layout: default
url: /faq/
---

1. auto-gen TOC:
{:toc}

### Does Truth support GWT?

A subset of Truth functionality is GWT compatible.  Most propositions and subjects 
that do not inherently use reflection or complex classloading are available.  This
mostly excludes categorical testing of collection contents since that uses code
generation not supportable (as is) on GWT, as well as ClassSubjects, which largely
concerns itself with the internals of Java classes and testing them.  Also, raw
field access is not supported, though this might be in the future if there is enough
of a use-case for it.

### Who's using Truth

  - [Dagger](http://github.com/square/dagger) (Square and Google)
  - [Google Auto](http://github.com/google/auto)
  - [Google Guava](http://code.google.com/p/guava-libraries)

### Are there extensions to Truth? 

  - [Compile-Testing](http://github.com/google/compile-testing)

