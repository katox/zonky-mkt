# zonky-mkt
Show new loans via public Zonky API (demo).

## Prerequisites

You need Java 11 to build and run this demo.

## Build

```$sh
mvn clean dependency:copy-dependencies package
```

## Run

In the project main directory run the created jar in the `target/` directory
```$sh
java -jar target/zonky_mkt.jar
```