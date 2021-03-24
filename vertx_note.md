

refer to https://vertx.io/docs/vertx-core/java/

# Vert.x Core Manual

create vert.x instance

```java
Vertx vertx = Vertx.vertx();
```

## Future

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

### Future composition

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



### Future coordination

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



### CompletionStage interoperability ???

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



## Verticles

```
public class MyVerticle extends AbstractVerticle {

 // Called when verticle is deployed
 public void start() {
 }

 // Optional - called when verticle is undeployed
 public void stop() {
 }

}
```

