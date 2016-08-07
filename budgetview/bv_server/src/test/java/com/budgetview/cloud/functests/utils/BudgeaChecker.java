package com.budgetview.cloud.functests.utils;

import junit.framework.Assert;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class BudgeaChecker {
  private static final String token = "AO6jqstUP2HQMt3vQVPFhV6F9/k0Fw8Q";

  public void callWebhook(String json) throws IOException {
    Request request = Request.Post("http://127.0.0.1:8080/budgea")
      .addHeader("Authorization", "Bearer " + token)
      .bodyString(json, ContentType.APPLICATION_JSON);

    Response response = request.execute();
    Assert.assertEquals(200, response.returnResponse().getStatusLine().getStatusCode());
  }
}
