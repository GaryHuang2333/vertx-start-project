package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;

public class MainVerticle extends AbstractVerticle {

//  @Override
//  public void start() {
//    HttpServer httpServer = vertx.createHttpServer()
//      .requestHandler(request -> {
//        System.out.println("Welcome to Vertx " + request.getHeader("name"));
//        request.response().end("Welcome to Vertx " + request.getHeader("name"));
//
//      });
//    httpServer.listen(8080);
//  }


  @Override
  public void start(Promise<Void> startPromise){
    vertx.deployVerticle(RegisterVerticle.class.getName());
    startPromise.complete();
  }

}
