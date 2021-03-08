package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

public class RegisterVerticle extends AbstractVerticle {

  @Override
  public void start(){
    Router basicRouter = Router.router(vertx);
    basicRouter.route("/").handler(routingContext -> {
      routingContext.response()
        .putHeader("content-type", "text/plain")
        .end("Welcome to " + getClass().getSimpleName());
    });

    Router registerRouter = Router.router(vertx);
    registerRouter.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.putHeader("content-type", "text/plain");
      response.end("Let`s get register");
    });

    basicRouter.mountSubRouter("/register", registerRouter);

    HttpServer httpServer = vertx.createHttpServer();
    httpServer.requestHandler(basicRouter).listen(8080);
  }
}
