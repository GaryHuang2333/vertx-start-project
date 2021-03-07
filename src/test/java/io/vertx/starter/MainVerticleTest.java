package io.vertx.starter;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

//  // <vertx.version>4.0.0</vertx.version>
//  @Test
//  public void testThatTheServerIsStarted(TestContext tc) {
//    Async async = tc.async();
//    vertx.createHttpClient().request(HttpMethod.GET, 8080, "localhost", "/", tc.asyncAssertSuccess(req -> {
//      req.send(tc.asyncAssertSuccess(resp -> {
//        tc.assertEquals(200, resp.statusCode());
//        resp.body(tc.asyncAssertSuccess(body -> {
//          tc.assertTrue(body.length() > 0);
//          async.complete();
//        }));
//      }));
//    }));
//  }

  @Test
  public void testThatTheServerIsStarted3(TestContext tc) {
    Async async = tc.async();
    Handler<AsyncResult<HttpClientRequest>> asyncResultHandler = tc.asyncAssertSuccess(httpClientRequest -> {
      httpClientRequest.send(tc.asyncAssertSuccess(httpClientResponse -> {
        System.out.println("response status code = [" + httpClientResponse.statusCode() + "]");
        System.out.println("response status message = [" + httpClientResponse.statusMessage() + "]");
        tc.assertEquals(httpClientResponse.statusCode(), 200);
        httpClientResponse.body(tc.asyncAssertSuccess(bodyBuffer -> {
          System.out.println("response body = [" + bodyBuffer.toString() + "]");
          tc.assertTrue(bodyBuffer.length() > 0);
        }));

        httpClientResponse.bodyHandler(buffer -> {
          tc.assertTrue(buffer.length() > 0);
          async.complete();
        });
      }));
    });

    HttpClient httpClient = vertx.createHttpClient();
    httpClient.request(HttpMethod.GET, 8080, "localhost", "/", asyncResultHandler);

  }


  // <vertx.version>3.5.2</vertx.version>
//  @Test
//  public void testThatTheServerIsStarted2(TestContext tc) {
//    Async async = tc.async();
//    vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
//      tc.assertEquals(response.statusCode(), 200);
//      response.bodyHandler(body -> {
//        tc.assertTrue(body.length() > 0);
//        async.complete();
//      });
//    });
//  }
}
