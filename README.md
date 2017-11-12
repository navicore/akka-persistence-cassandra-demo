[![Build Status](https://travis-ci.org/navicore/akka-persistence-cassandra-demo.svg?branch=master)](https://travis-ci.org/navicore/akka-persistence-cassandra-demo)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/18ad81a51b274a9d9787f6e85be661d5)](https://www.codacy.com/app/navicore/akka-persistence-cassandra-demo?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=navicore/akka-persistence-cassandra-demo&amp;utm_campaign=Badge_Grade)

An Akka HTTP API Server persisting objects with Akka Persistence for Cassandra
---

## RUN

```console
sbt run
```

## BUILD

```console
sbt assembly && docker build -t myimage .
```

#### Notes:

* See `application.conf` for `ENVIRONMENT` variable overrides for Cassandra and Akka settings
* The defaults assume an unsecured Cassandra at localhost:9042
  * create a test Cassandra server that will work with the defaults via `docker run -p 9042:9042 --name my-cassandra -d cassandra:3.11`
  * once Cassandra is fully started (may take a minute), connect the `cqlsh` client via `docker run -it --link my-cassandra:cassandra --rm cassandra:3.11 sh -c 'exec cqlsh "$CASSANDRA_PORT_9042_TCP_ADDR"'`
  * to use a different server, override settings with ENV vars: see `application.conf`
* `sbt assembly` works
* `sbt assembly && docker build -t myimage .` builds a usable Docker image
* The only Cassandra-specific things in this example are found in `application.conf` and `build.sbt`.  You should be able to use any API conforming persistence plugin with this example code.

