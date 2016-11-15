Random Text Gen
=====

A Spring Boot web application that test the following:

* A Markov Chain Random text generator (uses Stream API for lazy generation of text) and testing with JUnit 5.
* JUnit 5 integration with Spring Boot.
* MockMvc for integration test.
* Async REST using Spring's `DeferredResult` and Java's `CompletableFuture`.
* Compression for `text/plain` type responses.
* Serving static web content from Spring Boot.

Installation
===

Use the usual maven build process (you can skip testing):

`mvn install -DskipTests`

The resulting jar will be in `random-text-server/target/` directory.

To run the project do:

```bash
java -jar target/random-text-server-1.0-SNAPSHOT.jar
```

Then open your browser at [http://localhost:9091/](http://localhost:9091) 
