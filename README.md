# rdf-triple-share
An example of how Git, Maven, and Apache Jena can be used to manage knowledge graph data

Environmental prerequisites - Java 11 and Maven

To use, just run `mvn clean install`.

The `rdf` directory will be traversed as part of the build, and the set of all facts examined for syntax. Facts need to be expressed is Turtle (.ttl) or N-triple (.nt) format.
