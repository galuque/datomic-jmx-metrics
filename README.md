# Datomic JMX Metrics

Provides a handler function to be used by Datomic's transactor and peer server to expose metrics via JMX. 

It uses the [custom monitoring](https://docs.datomic.com/pro/operation/monitoring.html#custom) feature of Datomic

With the metrics exposed via JMX, you can use tools like [JConsole](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html) or [VisualVM](https://visualvm.github.io/) to monitor your Datomic system in development.

And in production you can use tools like [Prometheus](https://prometheus.io/) and [Grafana](https://grafana.com/) to monitor your Datomic system using [JMX exporter](https://github.com/prometheus/jmx_exporter)

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

In both cases you need the `datomic-jmx-metrics` library in the classpath of the transactor or peer server.

You can download the latest release JAR and add it to the `lib` directory of your Datomic installation.

```bash
curl -LJO https://github.com/galuque/datomic-jmx-metrics/releases/download/v0.1.5/datomic-jmx-metrics-0
.1.11.jar && mv datomic-jmx-metrics-0.1.11.jar /path/to/datomic/lib
```

(This is a "thin" jar, it doesn't include any dependencies, the only dependency is "org.clojure/java.jmx" which is already included in Datomic)

Finally you need to expose the JMX port of the transactor or peer server to be able to connect to it with a JMX client.

You can do that with the following Java system properties:

```bash
bin/transactor ... -Dcom.sun.management.jmxremote.port=9004 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false ...
```

(This is not recommended for production environments, see [JMX Monitoring and Management](https://docs.oracle.com/javase/8/docs/technotes/guides/management/agent.html) for more information)

### Example project

In the "monitoring" branch of the [datomic-compose](https://github.com/galuque/datomic-compose/tree/monitoring) project you can find an example of how to use this library to monitor a Datomic system using prometheus and grafana.

## Development

Run the project's tests:

    $ clojure -T:build test

Run the project's CI pipeline and build a JAR:

    $ clojure -T:build ci

This will produce an updated `pom.xml` file with synchronized dependencies inside the `META-INF`
directory inside `target/classes` and the JAR in `target`. You can update the version (and SCM tag)
information in generated `pom.xml` by updating `build.clj`.

## License

Copyright © 2023 Gabriel Luque Di Donna

Distributed under the MIT License.
