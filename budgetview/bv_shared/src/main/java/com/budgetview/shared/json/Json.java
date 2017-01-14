package com.budgetview.shared.json;

import com.budgetview.shared.http.Http;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class Json {
  public static JSONObject json(Request request, String url) throws IOException {
    return Http.executeAndGetJson(url, request);
  }

  public static JSONObject json(Response response) throws IOException {
    return parse(response.returnContent().asString());
  }

  public static JSONObject json(HttpResponse response) throws IOException {
    InputStream stream = response.getEntity().getContent();
    if (stream == null) {
      return null;
    }
    String content = Files.loadStreamToString(stream, "UTF-8");
    if (Strings.isNullOrEmpty(content)) {
      return null;
    }
    return parse(content);
  }

  public static JSONObject parse(String content) throws IOException {
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
}
