package com.budgetview.shared.json;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Json {
  public static JSONObject json(Request request) throws IOException {
    Response response = request.execute();
    return json(response);
  }

  public static JSONObject json(Response response) throws IOException {
    String content = response.returnContent().asString();
    if (Strings.isNullOrEmpty(content)) {
      throw new IOException("Request returned no content");
    }
    try {
      return new JSONObject(content);
    }
    catch (JSONException e) {
      throw new IOException("Invalid content: " + content, e);
    }
  }

  public static JSONObject json(HttpResponse response) throws IOException {
    return new JSONObject(Files.loadStreamToString(response.getEntity().getContent(), "UTF-8"));
  }
}
