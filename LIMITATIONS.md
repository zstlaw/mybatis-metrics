Known limitations
=================

Below are some of the known limitations of this library. Each item appears under library version when
the limitation was written down in this file and a pseudo-ASCII checkbox to note that the limitation
has been resolved.

As of `0.1-SNAPSHOT`:
* Mapper methods differing by arguments are rolled into one metric
  * This is because the arguments are not taken into account when generating metric name.
* Currently there is no way to change the mertic name template
  * Metric names are always the concatenation of the class name (belonging to the mapper) and the
    specific method in the mapper.
