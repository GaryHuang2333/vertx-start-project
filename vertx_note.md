refer to https://vertx.io/docs/vertx-core/java/

# 1. Vert.x Core Manual

create vert.x instance
```java
Vertx vertx = Vertx.vertx();
```

## 1.1. Future
#### ``future.onComplete()``
```java
FileSystem fs = vertx.fileSystem();
Future<FileProps> future = fs.props("/my_file.txt");
future.onComplete((AsyncResult<FileProps> ar) -> {
  if (ar.succeeded()) {
    FileProps props = ar.result();
    System.out.println("File size = " + props.size());
  } else {
    System.out.println("Failure: " + ar.cause().getMessage());
  }
});
```
### 1.1.1. Future composition
#### ``future.compose(f1).compose(f2).compose(f3)`` 
- futures are ``chained`` together, operation ``step by step`` : f1 -> f2 -> f3
- Succeed : all succeed 
```java
FileSystem fs = vertx.fileSystem();
Future<Void> future = fs
  .createFile("/foo")
  .compose(v -> {
    // When the file is created (fut1), execute this:
    return fs.writeFile("/foo", Buffer.buffer());
  })
  .compose(v -> {
    // When the file is written (fut2), execute this:
    return fs.move("/foo", "/bar");
  });
```
### 1.1.2. Future coordination
#### ``CompositeFuture.all(f1,f2...)`` 
- succeeded : all succedded
- failed : any failed
- if one failed, will return
```java
CompositeFuture.all(httpServerFuture, netServerFuture).onComplete(ar -> {
  if (ar.succeeded()) {
    // All servers started
  } else {
    // At least one server failed
  }
});
CompositeFuture.all(Arrays.asList(future1, future2, future3));
```
#### ``CompositeFuture.any(f1,f2...)`` 
- Succeeded : one succeed
- Failed : all failed
- if one succeeded, will return
```java
CompositeFuture.any(future1, future2).onComplete(ar -> {
  if (ar.succeeded()) {
    // At least one is succeeded
  } else {
    // All failed
  }
});
CompositeFuture.any(Arrays.asList(f1, f2, f3));
```
#### ``CompositeFuture.join(f1,f2...)``
- Succeeded : all succeeded
- Failed : all failed
- The join will waits until all futures are competed
```java
CompositeFuture.join(future1, future2, future3).onComplete(ar -> {
  if (ar.succeeded()) {
    // All succeeded
  } else {
    // All completed and at least one failed
  }
});
CompositeFuture.join(Arrays.asList(future1, future2, future3));
```
### 1.1.3. CompletionStage interoperability ???
#### ``Future -> CompletionStage``
```java
Future<String> future = vertx.createDnsClient().lookup("vertx.io");
future.toCompletionStage().whenComplete((ip, err) -> {
  if (err != null) {
    System.err.println("Could not resolve vertx.io");
    err.printStackTrace();
  } else {
    System.out.println("vertx.io => " + ip);
  }
});
```
#### ``CompletionStage -> Future``
```java
Future.fromCompletionStage(completionStage, vertx.getOrCreateContext())
  .flatMap(str -> {
    String key = UUID.randomUUID().toString();
    return storeInDb(key, str);
  })
  .onSuccess(str -> {
    System.out.println("We have a result: " + str);
  })
  .onFailure(err -> {
    System.err.println("We have a problem");
    err.printStackTrace();
  });
```

## 1.2. Verticles
### 1.2.1. Create verticles
#### ``Default``
```java
public class MyVerticle extends AbstractVerticle {
 // Called when verticle is deployed
 public void start() {
 }
 // Optional - called when verticle is undeployed
 public void stop() {
 }
}
```
#### ``Asynchronous``
```java
public class MyVerticle extends AbstractVerticle {
 private HttpServer server;
 public void start(Promise<Void> startPromise) {
   server = vertx.createHttpServer().requestHandler(req -> {
     req.response()
       .putHeader("content-type", "text/plain")
       .end("Hello from Vert.x!");
     });
   // Now bind the server:
   server.listen(8080, res -> {
     if (res.succeeded()) {
       startPromise.complete();
     } else {
       startPromise.fail(res.cause());
     }
   });
 }
 public void stop(Promise<Void> stopPromise) {
   obj.doSomethingThatTakesTime(res -> {
     if (res.succeeded()) {
       stopPromise.complete();
     } else {
       stopPromise.fail();
     }
   });
 }
}
```
### 1.2.2. Verticle type
#### ``Standard verticle``
```java
DeploymentOptions options = new DeploymentOptions();
vertx.deployVerticle("com.mycompany.MyStandardVerticle", options);
```
#### ``Worker verticle``
```java
DeploymentOptions options = new DeploymentOptions().setWorker(true);
vertx.deployVerticle("com.mycompany.MyWorkerVerticle", options);
```
### 1.2.3. Deploy verticle
```java
// 1. pass a verticle instance
Verticle myVerticle = new MyVerticle();
vertx.deployVerticle(myVerticle);
// 2. specifying the verticle name
vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle");
```
### 1.2.4. Waiting for deployment to complete
```java
vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", res -> {
  if (res.succeeded()) {
    // deploy id : res.result()
    System.out.println("Deployment id is: " + res.result());
  } else {
    System.out.println("Deployment failed!");
  }
});
```
### 1.2.5. Undeploy verticles
```java
vertx.undeploy(deploymentID, res -> {
  if (res.succeeded()) {
    System.out.println("Undeployed ok");
  } else {
    System.out.println("Undeploy failed!");
  }
});
```
### 1.2.6. Specifying number of verticle instances
```java
DeploymentOptions options = new DeploymentOptions().setInstances(16);
vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", options);
```
### 1.2.7. Passing configuration to a verticle
```java
JsonObject config = new JsonObject().put("name", "tim").put("directory", "/blah");
DeploymentOptions options = new DeploymentOptions().setConfig(config);
vertx.deployVerticle("com.mycompany.MyOrderProcessorVerticle", options);
```
### 1.2.8. High Availability
### 1.2.9. The Context object
#### ``create context``
```java
Context context = vertx.getOrCreateContext();
```
####``test context type``
```java
Context context = vertx.getOrCreateContext();
if (context.isEventLoopContext()) {
  System.out.println("Context attached to Event Loop");
} else if (context.isWorkerContext()) {
  System.out.println("Context attached to Worker Thread");
} else if (! Context.isOnVertxThread()) {
  System.out.println("Context not attached to a thread managed by vert.x");
}
```
####``run on context``
```java
vertx.getOrCreateContext().runOnContext( (v) -> {
  System.out.println("This will be executed asynchronously in the same context");
});
// pass data
final Context context = vertx.getOrCreateContext();
context.put("data", "hello");
context.runOnContext((v) -> {
  String hello = context.get("data");
});
```
### 1.2.10. Executing periodic and delayed actions
####``One-shot Timers``
```java
// 1000 milliseconds and unique timer id
long timerID = vertx.setTimer(1000, id -> {
  System.out.println("And one second later this is printed");
});
System.out.println("First this is printed");
```
####``Periodic Timers``
```java
// 1000 milliseconds and unique timer id
long timerID = vertx.setPeriodic(1000, id -> {
  System.out.println("And every second this is printed");
});
System.out.println("First this is printed");
```
####``Cancelling timers``
```java
vertx.cancelTimer(timerID);
```
### 1.2.11. Verticle worker pool
Verticles use the Vert.x worker pool for executing blocking actions, i.e executeBlocking or worker verticle.
```java
vertx.deployVerticle("the-verticle", new DeploymentOptions().setWorkerPoolName("the-specific-pool"));
```

## 1.3. Event bus
### 1.3.1. Create event bus
```java
EventBus eb = vertx.eventBus();
```
### 1.3.2. Consumer
####``Register``
```java
EventBus eb = vertx.eventBus();
// case 1
eb.consumer("news.uk.sport", message -> {
  System.out.println("I have received a message: " + message.body());
});
// case 2
MessageConsumer<String> consumer = eb.consumer("news.uk.sport");
consumer.handler(message -> {
  System.out.println("I have received a message: " + message.body());
});
// get notified when registration complete
consumer.completionHandler(res -> {
  if (res.succeeded()) {
    System.out.println("The handler registration has reached all nodes");
  } else {
    System.out.println("Registration failed!");
  }
});
```
####``Un-register``
```java
consumer.unregister(res -> {
  if (res.succeeded()) {
    System.out.println("The handler un-registration has reached all nodes");
  } else {
    System.out.println("Un-registration failed!");
  }
});
```
### 1.3.3. Publish message (one-to-all)
```java
// That message will then be delivered to all handlers registered against the address news.uk.sport
eventBus.publish("news.uk.sport", "Yay! Someone kicked a ball");
```
### 1.3.4. Send message (one-to-one)
#### 1.3.4.1. send
```java
// Sending a message will result in only one handler registered at the address receiving the message
eventBus.send("news.uk.sport", "Yay! Someone kicked a ball");
```
#### 1.3.4.2. header
```java
DeliveryOptions options = new DeliveryOptions();
options.addHeader("some-header", "some-value");
eventBus.send("news.uk.sport", "Yay! Someone kicked a ball", options);
```
#### 1.3.4.3. order
Vert.x will deliver messages to any particular handler in the same order they were sent from any particular sender.
#### 1.3.4.4. reply
```java
// reciever
MessageConsumer<String> consumer = eventBus.consumer("news.uk.sport");
consumer.handler(message -> {
  System.out.println("I have received a message: " + message.body());
  message.reply("how interesting!");
});
// sender
eventBus.request("news.uk.sport", "Yay! Someone kicked a ball across a patch of grass", ar -> {
  if (ar.succeeded()) {
    System.out.println("Received reply: " + ar.result().body());
  }
});
```
#### 1.3.4.5. Sending with timeouts
When sending a message with a reply handler, you can specify a timeout in the DeliveryOptions.
If a reply is not received within that time, the reply handler will be called with a failure.
The default timeout is 30 seconds.
#### 1.3.4.6. Send Failures
Message sends can fail for other reasons, including:
- There are no handlers available to send the message to
- The recipient has explicitly failed the message using fail

In all cases, the reply handler will be called with the specific failure.
### 1.3.5. Message Codecs ???
```java
// 1
eventBus.registerCodec(myCodec);
DeliveryOptions options = new DeliveryOptions().setCodecName(myCodec.name());
eventBus.send("orders", new MyPOJO(), options);
// 2
eventBus.registerDefaultCodec(MyPOJO.class, myCodec);
eventBus.send("orders", new MyPOJO());
```
### 1.3.6. Clustered Event Bus
```java
VertxOptions options = new VertxOptions();
Vertx.clusteredVertx(options, res -> {
  if (res.succeeded()) {
    Vertx vertx = res.result();
    EventBus eventBus = vertx.eventBus();
    System.out.println("We now have a clustered event bus: " + eventBus);
  } else {
    System.out.println("Failed: " + res.cause());
  }
});
```
### 1.3.7. Automatic clean-up in verticles
If you’re registering event bus handlers from inside verticles, those handlers will be automatically unregistered when the verticle is undeployed.
### 1.3.8. Configuring the event bus
```java
// 1. how you can use SSL connections for the event bus
VertxOptions options = new VertxOptions()
    .setEventBusOptions(new EventBusOptions()
        .setSsl(true)
        .setKeyStoreOptions(new JksOptions().setPath("keystore.jks").setPassword("wibble"))
        .setTrustStoreOptions(new JksOptions().setPath("keystore.jks").setPassword("wibble"))
        .setClientAuth(ClientAuth.REQUIRED)
    );
// clustered
Vertx.clusteredVertx(options, res -> {
  if (res.succeeded()) {
    Vertx vertx = res.result();
    EventBus eventBus = vertx.eventBus();
    System.out.println("We now have a clustered event bus: " + eventBus);
  } else {
    System.out.println("Failed: " + res.cause());
  }
});

// 2. When used in containers, you can also configure the public host and port:
VertxOptions options = new VertxOptions()
    .setEventBusOptions(new EventBusOptions()
        .setClusterPublicHost("whatever")
        .setClusterPublicPort(1234)
    );
// clustered
Vertx.clusteredVertx(options, res -> {
  if (res.succeeded()) {
    Vertx vertx = res.result();
    EventBus eventBus = vertx.eventBus();
    System.out.println("We now have a clustered event bus: " + eventBus);
  } else {
    System.out.println("Failed: " + res.cause());
  }
});
```

## JSON



## Buffers



## TCP Server and Client
### Server
#### Create a TCP server
```java
NetServer server = vertx.createNetServer();
```
#### configuration
```java
NetServerOptions options = new NetServerOptions().setPort(4321);
NetServer server = vertx.createNetServer(options);
```
#### start listening
```java
// 1. default
NetServer server = vertx.createNetServer();
server.listen();
// 2. specify hostname and port
NetServer server = vertx.createNetServer();
// The default host is 0.0.0.0 which means 'listen on all available addresses' and the default port is 0, which is a special value that instructs the server to find a random unused local port and use that.
server.listen(1234, "localhost");
// 3. provide handler
NetServer server = vertx.createNetServer();
server.listen(1234, "localhost", res -> {
  if (res.succeeded()) {
    System.out.println("Server is now listening!");
  } else {
    System.out.println("Failed to bind!");
  }
});
// 4. listen to random port
NetServer server = vertx.createNetServer();
// port=0 means find a random unused port
server.listen(0, "localhost", res -> {
  if (res.succeeded()) {
    System.out.println("Server is now listening on actual port: " + server.actualPort());
  } else {
    System.out.println("Failed to bind!");
  }
});
```
#### Getting notified of incoming connections
```java
NetServer server = vertx.createNetServer();
server.connectHandler(socket -> {
  // Handle the connection in here
});
```
When a connection is made the handler will be called with an instance of NetSocket.
#### Socket
##### Reading data from the socket
```java
NetServer server = vertx.createNetServer();
server.connectHandler(socket -> {
  socket.handler(buffer -> {
    System.out.println("I received some bytes: " + buffer.length());
  });
});
```
##### Writing data to a socket
```java
Buffer buffer = Buffer.buffer().appendFloat(12.34f).appendInt(123);
socket.write(buffer);
// Write a string in UTF-8 encoding
socket.write("some data");
// Write a string using the specified encoding
socket.write("some data", "UTF-16");
```
##### Closed handler
```java
socket.closeHandler(v -> {
  System.out.println("The socket has been closed");
});
```
##### Sending files or resources from the classpath
```java
socket.sendFile("myfile.dat");
```
#### Closing a TCP Server
```java
server.close(res -> {
  if (res.succeeded()) {
    System.out.println("Server is now closed");
  } else {
    System.out.println("close failed");
  }
});
```
#### Scaling - sharing TCP servers
```java
for (int i = 0; i < 10; i++) {
  NetServer server = vertx.createNetServer();
  server.connectHandler(socket -> {
    socket.handler(buffer -> {
      // Just echo back the data
      socket.write(buffer);
    });
  });
  server.listen(1234, "localhost");
}
```
### Client
#### Create a TCP client
```java
NetClient client = vertx.createNetClient();
```
#### Configuration
```java
NetClientOptions options = new NetClientOptions().setConnectTimeout(10000);
NetClient client = vertx.createNetClient(options);
```
#### Making connections
```java
NetClientOptions options = new NetClientOptions().setConnectTimeout(10000);
NetClient client = vertx.createNetClient(options);
client.connect(4321, "localhost", res -> {
  if (res.succeeded()) {
    System.out.println("Connected!");
    NetSocket socket = res.result();
  } else {
    System.out.println("Failed to connect: " + res.cause().getMessage());
  }
});
```
#### Config connection attempts
```java
NetClientOptions options = new NetClientOptions().
  setReconnectAttempts(10).
  setReconnectInterval(500);
NetClient client = vertx.createNetClient(options);
```
### Logging
#### server
```java
NetServerOptions options = new NetServerOptions().setLogActivity(true);
NetServer server = vertx.createNetServer(options);
```
#### client
```java
NetClientOptions options = new NetClientOptions().setLogActivity(true);
NetClient client = vertx.createNetClient(options);
```
Network activity is logged by Netty with the DEBUG level and with the io.netty.handler.logging.LoggingHandler name. When using network activity logging there are a few things to keep in mind:
- logging is not performed by Vert.x logging but by Netty
- this is not a production feature
### Config SSL/TLS
#### server
```java
// 1. Specifying key/certificate for the server
NetServerOptions options = new NetServerOptions().setSsl(true).setKeyStoreOptions(
  new JksOptions().
    setPath("/path/to/your/server-keystore.jks").
    setPassword("password-of-your-keystore")
);
NetServer server = vertx.createNetServer(options);
// 2. Specifying trust for the server
NetServerOptions options = new NetServerOptions().
  setSsl(true).
  setClientAuth(ClientAuth.REQUIRED).
  setTrustStoreOptions(
    new JksOptions().
      setPath("/path/to/your/truststore.jks").
      setPassword("password-of-your-truststore")
  );
NetServer server = vertx.createNetServer(options);
```
#### client


## HTTP Server and Client



## FileSystem



