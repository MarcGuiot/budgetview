package com.budgetview.shared.cloud;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.utils.Files;

import java.io.IOException;

public class CloudAPI {

  public void addConnection(String email, String budgeaToken, Integer budgeaUserId) throws IOException {
    String url = cloudUrl("/connections");
    Request request = Request.Post(url)
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.BUDGEA_TOKEN, budgeaToken)
      .addHeader(CloudConstants.BUDGEA_USER_ID, Integer.toString(budgeaUserId));
    execute(request, url);
  }

  public String getStatement(String email) throws IOException {
    String url = cloudUrl("/statement");
    Request request = Request.Get(url)
      .addHeader(CloudConstants.EMAIL, email);

    HttpResponse response = execute(request, url);
    return Files.loadStreamToString(response.getEntity().getContent(), "UTF-8");
  }

  public HttpResponse execute(Request request, String url) throws IOException {
    Response response = request.execute();
    HttpResponse httpResponse = response.returnResponse();
    int statusCode = httpResponse.getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new IOException("Call to " + url + " returned " + statusCode + " instead of 200");
    }
    return httpResponse;
  }

  private String cloudUrl(String path) {
    return CloudConstants.getServerUrl(path);
  }
}

