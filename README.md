# Transparent CDI event distribution in Jakarta EE clusters

The `jakartaee-clustered-cdi-events` project is inspired by the approach
invented by Eder Ignatowicz for transparently distributing CDI events across a
Jakarta EE cluster. Eder Ignatowicz's somewhat different implementation is used
in the Drools and jBPM web framework
[Uberfire](https://github.com/kiegroup/appformer/commit/875f0efd9ea80ef9ad5fb104bb05ca81dcdf661e).

## Overview

The library builds upon the familiar Jakarta EE CDI event mechanism that works
within single application instances and extends it to work across clustered
environments without changing how you use CDI events.

When you fire an event, the system automatically serializes it and sends it
over JMS to all cluster nodes. Each node then deserializes and fires it as a
regular CDI event. From your perspective, it's just normal CDI events that now
work cluster-wide transparently.

Both asynchronous and synchronous CDI events are supported. Async events work
naturally with JMS distribution. Synchronous events use
`TransactionPhase.AFTER_SUCCESS` to ensure they only fire after successful
transaction completion.

Events marked with the `@Clustered` annotation are distributed across the
cluster, unannotated events remain local.

All events use a single JMS topic called `CLUSTER_CDI_EVENTS`. This simplifies
configuration, reduces resource usage and ensures consistent event processing
across nodes.

Integration is straightforward. Add the module as a dependency to your WAR or
EAR, configure the JMS topic and cluster-wide CDI events work automatically.

## Prerequisites

To use this project, you need:

- Java 17 or higher.
- A Jakarta EE 10 compatible application server (e.g., WildFly, Payara, Open Liberty).
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

### Cross-bridging JMS on two WildFly servers

A JMS cross-bridge can be used for JMS clustering between two WildFly server
instances. By cross-bridging the messages are replicated from a topic on one
server to the same topic on the other.

The process involves:

1. Setting up a user on both server instances that the JMS connection will use.
2. Configuring a JMS bridge in instance A with the bridge’s source destination
   connecting to instance B.
3. Similarly, configuring the bridge in instance B with the bridge’s source
   destination connecting to instance A.

The source and target destination of the bridge is the `CLUSTER_CDI_EVENTS`
topic used in `jakartaee-clustered-cdi-events`.

The example `jboss-cli` commands for adding the topic, bridge and other
required configuration are available in the test project in
[`config/wildfly-configuration-commands.cli`](https://github.com/mrts/test-jakartaee-clustered-cdi-events/blob/main/config/wildfly-configuration-commands.cli).

## Usage

To use this module in your Jakarta EE application:

- Use the `@Clustered` annotation to mark CDI events that should be distributed
  across the cluster. Make sure that the event class is serializable to/from
JSON using Yasson (it must be public, have getters, setters and a no-args
constructor).
- Fire clustered events from your application code using either the
  asynchronous or synchronous CDI events mechanism:
  - for asynchronous event propagation, use `Event<T>.fireAsync(T event)`,
  - for synchronous event propagation, use `Event<T>.fire(T event)`.
- Events are automatically serialized and distributed to other nodes in the
  cluster, then deserialized and fired as local CDI events. The method of
firing asynchronously or synchronously on the originating node will be mirrored
on the receiving nodes.
- Use observer methods annotated with `@ObservesAsync` for handling
  asynchronous events. For synchronous events, use `@Observes(during =
TransactionPhase.AFTER_SUCCESS)` to align with the transaction phase used in
the module, ensuring that synchronous events are handled only after the
successful completion of transactions. 

## Testing

A simple application for testing this module is available
[here](https://github.com/mrts/test-jakartaee-clustered-cdi-events).

## Debugging

In case you are using WildFly, enable debug logs for the `org.clustercdievents`
module in WildFly using `jboss-cli` as follows:

```sh
jboss-cli.sh --connect --commands="/subsystem=logging/root-logger=ROOT:write-attribute(name=level, value=DEBUG),/subsystem=logging/logger=org.clustercdievents:add(level=DEBUG)"
```

## Implementation

The implementation is relatively straightforward:

1. **`Event<T>.fire(T event)`** or `Event<T>.fireAsync(T event)`: An
   application component fires a CDI event either synchronously or
asynchronously. This is a regular CDI event firing that occurs within the
application code.
2. **`CDIEventObserver.observeAllEvents()`** and
   `CDIEventObserver.observeAllEventsAsync()`: The `CDIEventObserver` class
method `observeAllEvents()` uses the `@Observes(during =
TransactionPhase.AFTER_SUCCESS) Object event` annotated parameter to observe
all events emitted after the successful completion of transactions.
`observeAllEventsAsync()` uses `@ObservesAsync` for observing all asynchronous
events. These methods are invoked by the Jakarta EE container internally. The
methods check if the event is marked with the `@Clustered` annotation for
cluster-wide distribution.
3. **`JMSMessageSender.send()`**: If the event is marked for cluster
   distribution, `CDIEventObserver` serializes the event data and whether it is
synchronous or asynchronous, and uses `JMSMessageSender` to send this data over
a JMS topic. The Jakarta EE JMS provider ensures the delivery of this message
to other nodes in the cluster.
4. **`JMSMessageReceiver.onMessage()`**: On each node in the cluster,
   `JMSMessageReceiver` listens for messages on the JMS topic. When it receives
a message, the `onMessage()` method is invoked.
5. **`CDIEventEmitter.fireLocalAsyncCDIEventFromJMSMessage()`**: Inside the
   `onMessage()` method, the `JMSMessageReceiver` passes the received message
to `CDIEventEmitter`, which then deserializes the message back into a CDI event
and fires it locally on the node either synchronously or asynchronously,
mirroring the method used on the originating node. CDI components on this node
observe and react to the event as if it were fired locally.

Eder Ignatowicz's original diagram, shown below, illustrates the system design.
However, his implementation differs, he is using a single class
`ClusterEventObserver` instead of all the components mentioned above.

![Architecture diagram](https://ederign.me/static/images/2018/cdievent.png)

## Acknowledgments

Special thanks to Eder Ignatowicz for the original concept and implementation
as described in [his blog post](https://ederign.me/blog/2018-09-07-transparent-cdi-events).

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details.
