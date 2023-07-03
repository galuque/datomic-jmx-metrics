# Datomic JMX Metrics

Provides a handler function to be used by Datomic's transactor and peer server to expose metrics via JMX. 

It uses the [custom monitoring](https://docs.datomic.com/pro/operation/monitoring.html#custom) feature of Datomic

With the metrics exposed via JMX, you can use tools like [JConsole](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html) or [VisualVM](https://visualvm.github.io/) to monitor your Datomic system in development.

And in production you can use tools like [Prometheus](https://prometheus.io/) and [Grafana](https://grafana.com/) to monitor your Datomic system using [JMX expoerter](https://github.com/prometheus/jmx_exporter)

## Usage

To monitor a transactor, add the following to your transactor properties file:

```ini
metrics-callback=io.github.galuque.datomic.jmx.metrics/callback
```

or add the `datomic.metricsCallback` Java system property to your transactor startup command:

```bash
bin/transactor ... -Ddatomic.metricsCallback=io.github.galuque.datomic.jmx.metrics/callback ...
```

To monitor a peer server you have to set the `datomic.metricsCallback` Java system property in the peer server startup command.

```bash
bin/run -m datomic.peer-server -Ddatomic.metricsCallback=io.github.galuque.datomic.jmx.metrics/callback ...
``` 

## Development

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

Install it locally (requires the `ci` task be run first):

    $ clojure -T:build install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `ci` task be run first):

    $ clojure -T:build deploy

## License

Copyright © 2023 Gabriel Luque Di Donna

Distributed under the MIT License.
