mybatis-metrics
===============

Instrumentation for MyBatis using Dropwizard Metrics library. The database is always slow in the programming
folklore, but do you actually measure it?

Can I use this in Production? Are there any skeletons in the closet?
--------------------------------------------------------------------

No, please don't. Some of the functionality still remains to be tested. Please don't use this library in Production yet. 

As for the limitations of the current incarnation of this library, please see the [LIMITATIONS.md](LIMITATIONS.md) file.


How do I get it?
----------------

At this moment the library is available in source form and in Sonatype OSS **Snapshots** repository:

    <dependency>
      <groupId>com.tguzik.mybatis-metrics</groupId>
      <artifactId>mybatis-metrics</artifactId>
      <version>0.1-SNAPSHOT</version>
    </dependency>



Dependencies & nice-to-haves
----------------------------

Here are the high-level items  of this library:

* `JDK 1.7+`
* `Metrics 3.0+`
* `MyBatis 3.2.7+` - should work with any 3.x release of MyBatis, but that hasn't been tested yet. Development is
  based off the most recent version of MyBatis.
* `javax.inject` - enable use of Dependency Injection using the Java-standard annotations, should anyone need it.
* `IntelliJ IDEA` - not required per se, but it would be nice if all contributors used the same code formatting
  settings. Community edition is more than enough.


License
-------

This library is available under [MIT License](LICENSE).
