package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(VertxUnitRunner.class)
public class RegisterVerticleTest {
  private Vertx vertx;
  private WebClient webClient;

  @Before
  public void before(TestContext testContext){
    vertx = Vertx.vertx();
    webClient = WebClient.create(vertx);
    vertx.deployVerticle(RegisterVerticle.class.getName(), testContext.asyncAssertSuccess());
  }

  @After
  public void after(){
    vertx.close();
    webClient.close();
  }

  @Test
  public void test(TestContext testContext){
    Map<String, String> person = new HashMap<>();
    person.put("name", "Tom");
    person.put("age", "17");
    person.put("gender", "male");
    person.put("grade", "junior");
    JsonObject jsonObject = new JsonObject((HashMap)person);

    webClient.head(8080, "localhost", "/register")
      .putHeader("Content-Type", "application/json")
      .putHeader("X-Requested-With", "XMLHttpRequest")
      .sendJsonObject(jsonObject, testContext.asyncAssertSuccess(bufferHttpResponse -> {
        System.out.println("bufferHttpResponse status code = " + bufferHttpResponse.statusCode());
        System.out.println("bufferHttpResponse status message = " + bufferHttpResponse.statusMessage());
        System.out.println("bufferHttpResponse body = [" + bufferHttpResponse.bodyAsJsonObject().toString() + "]");
        testContext.assertEquals(bufferHttpResponse.statusCode(), 200);
        testContext.assertNotNull(bufferHttpResponse.bodyAsJsonObject());
        testContext.assertTrue(bufferHttpResponse.bodyAsString().length() > 0);
      }));
  }
}
