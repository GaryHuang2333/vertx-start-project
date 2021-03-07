package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

import java.sql.SQLOutput;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() {
//    vertx.createHttpServer()
//        .requestHandler(request -> request.response().end("Hello Vert.x!"))
//        .listen(8080);

    HttpServer httpServer = vertx.createHttpServer()
      .requestHandler(request -> {
        System.out.println("Welcome to Vertx " + request.getHeader("name"));
        request.response().end("Welcome to Vertx " + request.getHeader("name"));

      });
    httpServer.listen(8080);
  }

}
