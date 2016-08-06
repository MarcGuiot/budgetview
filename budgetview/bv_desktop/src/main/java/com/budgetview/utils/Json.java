package com.budgetview.utils;

import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import java.io.IOException;

public class Json {
  public static JSONObject json(Request request) throws IOException {
    return new JSONObject(request.execute().returnContent().asString());
  }
}
