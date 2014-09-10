mybatis-metrics
===============

Instrumentation for MyBatis using Dropwizard Metrics library. The database is always slow in the programming
folklore, but do you actually measure it?


Can I use this in Production? Are there any skeletons in the closet?
--------------------------------------------------------------------

No, please don't. Some of the functionality still remains to be tested. Please don't use this library in production 
yet. 

As for the limitations of the current incarnation of this library, please see the [LIMITATIONS.md](LIMITATIONS.md) file.


How do I get it?
----------------

At this moment the library is available in source form and in Sonatype OSS **Snapshots** repository:

    <dependency>
      <groupId>com.tguzik.mybatis-metrics</groupId>
      <artifactId>mybatis-metrics</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>


Contributing
------------

You are more than welcome to contribute! If you don't know what to start with, please see the contents of
the file <TODO.md>, as some of the tasks do not require any project-specific knowledge.


Dependencies & nice-to-haves
----------------------------

Here are the high-level dependencies of this library:

* `JDK 1.7+`
* `Metrics 3.0+`
* `MyBatis 3.2.7+` - should work with any 3.x release of MyBatis, but that hasn't been tested yet. Development is
  based off the most recent version of MyBatis.
* `javax.inject` - enable use of Dependency Injection using the standard Java annotations, should anyone need it.


Most of the development for this library is done using IntelliJ IDEA. This particular IDE is not required for 
development in any shape or form, but it is beneficial for all contributors to use the same source code formatting 
settings. Community edition of the IDE is more than enough.


License
-------

This library is available under [MIT License](LICENSE).
