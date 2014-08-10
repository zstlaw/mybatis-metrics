Known limitations
=================

Below are some of the known limitations of this library.

As of `0.1-SNAPSHOT`:
* Mapper methods differing by arguments are rolled into one metric
  * This is because the types of arguments are not passed in MyBatis' MappedStatement,
    which means that we do not have access to them (we have access to actual values, but
    guessing the signature would be extremely error prone)

* Currently there is no way to change the mertic name template
  * More specifically, changing metric name template requires subclassing InstrumentingInterceptor
    and in some deployment scenarios (see below) PropertyBootstrappedInstrumentingInterceptor (class
    names are subject to change before version `0.1`)
  * Metric names are always the concatenation of the class name (belonging to the mapper) and the
    specific method in the mapper.

* Non-manual bootstrap without a dependency injection integration requires using a secondary
  interceptor *and* a provider.
  * For the data collection to make sense, we have to take the MetricRegistry instance from
    somewhere. Unfortunately the way that MyBatis instanitates the plugins in XML file-based
    bootstrap, we have to use a decorating interceptor that looks up an instance of
    MetricRegistry using a system or a plugin property that should point to a
    Provider<MetricRegistry>
  * This is kludgy and will be refactored... at least some parts of it.
