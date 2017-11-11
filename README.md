An Akka HTTP API Server persisting objects with Akka Persistence for Cassandra
---

# UNDER CONSTRUCTION

# UNDER CONSTRUCTION

# UNDER CONSTRUCTION

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
  * once Cassandra is fully started (may take a minute), connect the `cqlsh` client via `docker run -it --link my-cassandra:cassandra --rm cassandra sh -c 'exec cqlsh "$CASSANDRA_PORT_9042_TCP_ADDR"'`
  * to use a different server, override settings with ENV vars: see `application.conf`
* `sbt assembly` works
* `sbt assembly && docker build -t myimage .` builds a usable Docker image

