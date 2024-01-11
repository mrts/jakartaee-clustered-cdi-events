# Transparent CDI event distribution in Jakarta EE clusters

The `jakartaee-clustered-cdi-events` project is inspired by the approach
invented by Eder Ignatowicz for transparently distributing CDI events across a
Jakarta EE cluster. Eder Ignatowicz's somewhat different implementation is used
in the Drools and jBPM web framework
[Uberfire](https://github.com/kiegroup/appformer/commit/875f0efd9ea80ef9ad5fb104bb05ca81dcdf661e).

## Key concepts

- Relies on the strengths and simplicity of the Jakarta EE CDI event mechanism
  that was originally intended only for single-instance use.
- Transparently adapts the CDI event mechanism for clustered environments.
  Events are automatically serialized and sent across cluster nodes over JMS,
then deserialized and fired as regular CDI events on each node, ensuring
uniform event distribution across the cluster.
- Uses asynchronous CDI Events, `@ObservesAsync` and `fireAsync()` to match the
  inherent asynchronicity of JMS-based distribution.
- Utilizes a custom `@Clustered` annotation to mark events for propagation
  across the cluster.
- A single JMS topic `CLUSTER_CDI_EVENTS` is used for all events. This
  simplifies configuration and monitoring, reduces resource usage and ensures
consistent event processing.
- Plug and play, integration is easy. Include the module as a dependency in
  your Jakarta EE WAR or EAR, configure the JMS topic and cross-cluster CDI
event distribution starts to work.

## Prerequisites

To use this project, you need:

- Java 11 or higher.
- A Jakarta EE 8 compatible application server (e.g., WildFly, Payara, Open Liberty).
- A JMS provider configured in your Jakarta EE environment.

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/mrts/jakartaee-clustered-cdi-events
   ```

2. Build the project using Maven:
   ```
   cd jakartaee-clustered-cdi-events
   mvn clean install
   ```

3. Add the resulting EJB artifact as a dependency to your project, e.g. with Maven:
   ```
   <dependencies>
       <dependency>
           <groupId>org.clusteredcdievents</groupId>
           <artifactId>jakartaee-clustered-cdi-events</artifactId>
           <version>...</version>
           <type>ejb</type>
       </dependency>
   </dependencies>
   ```
   In case of an EAR, you have to additionally include it as an `ejbModule`:
   ```
   <build>
       <plugins>
           <plugin>
               <groupId>org.apache.maven.plugins</groupId>
               <artifactId>maven-ear-plugin</artifactId>
               <configuration>
                   <modules>
                       <ejbModule>
                           <groupId>org.clusteredcdievents</groupId>
                           <artifactId>jakartaee-clustered-cdi-events</artifactId>
                       </ejbModule>
                   </modules>
               </configuration>
           </plugin>
       </plugins>
   </build>
   ```

## Configuration

You need to configure the JMS topic that the module uses in the Jakarta EE application server, e.g. with `jboss-cli`:

```sh
jboss-cli.sh --connect --command="jms-topic add --topic-address=CLUSTER_CDI_EVENTS --entries=java:/jms/topic/CLUSTER_CDI_EVENTS"
```

In case you are using WildFly, note that you need to run it with full profile to enable JMS:

```sh
standalone.sh -c standalone-full.xml
```

## Usage

To use this module in your Jakarta EE application:

- Use the `@Clustered` annotation to mark CDI events that should be distributed
  across the cluster. Make sure that the event class is serializable to/from
JSON using Yasson (it must be public, have getters, setters and a no-args
constructor).
- Fire the clustered events from your application code using the ordinary CDI
  asynchronous events mechanism: `Event<T>.fireAsync(T event)`.
- Events are automatically serialized and distributed to other nodes in the
  cluster, then deserialized and fired as local asynchronous CDI events.
- Use a method annotated with `@ObservesAsync` to catch and process the events.

## Testing

A simple application for testing this module is available
[here](https://github.com/mrts/test-jakartaee-clustered-cdi-events).

## Debugging

In case you are using WildFly, enable debug logs for the `org.clustercdievents` module in WildFly using `jboss-cli` as follows:

```sh
jboss-cli.sh --connect --commands="/subsystem=logging/root-logger=ROOT:write-attribute(name=level, value=DEBUG),/subsystem=logging/logger=org.clustercdievents:add(level=DEBUG)"
```

## Implementation

The implementation is relatively straightforward:

1. **`Event<T>.fireAsync(T event)`**: An application component fires an
   asynchronous CDI event. This is a regular CDI event firing that occurs
within the application code.
2. **`CDIEventObserver.observeAllEvents()`**: The `CDIEventObserver` class
   method `observeAllEvents()` uses the `@ObservesAsync Object event` annotated
parameter to observe all asynchronous events. This method is invoked by the
Jakarta EE container whenever a CDI event is fired. The method checks if the
event is marked with the `@Clustered` annotation for cluster-wide distribution.
3. **`JMSMessageSender.send()`**: If the event is marked for cluster
   distribution, `CDIEventObserver` serializes the event data and uses
`JMSMessageSender` to send this data over a JMS topic. The Jakarta EE JMS
provider ensures the delivery of this message to other nodes in the cluster.
4. **`JMSMessageReceiver.onMessage()`**: On each node in the cluster,
   `JMSMessageReceiver` listens for messages on the JMS topic. When it receives
a message, the `onMessage()` method is invoked.
5. **`CDIEventEmitter.fireLocalAsyncCDIEventFromJMSMessage()`**: Inside the
   `onMessage()` method, the `JMSMessageReceiver` passes the received message
to `CDIEventEmitter`, which then deserializes the message back into a CDI event
and fires it locally on the node. This allows CDI components on this node to
observe and react to the event as if it were fired locally.

Eder Ignatowicz's original diagram, shown below, illustrates the system design.
However, his implementation differs, he is using a single class
`ClusterEventObserver` instead of all the components mentioned above.

![Architecture diagram](https://ederign.me/assets/2018/cdievent.png)

## Acknowledgments

Special thanks to Eder Ignatowicz for the original concept and implementation
as described in [his blog post](https://ederign.me/2018/09/07/transparent-cdi-events.html).

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details.
