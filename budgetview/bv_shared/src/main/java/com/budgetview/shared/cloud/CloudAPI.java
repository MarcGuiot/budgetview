package com.budgetview.shared.cloud;

import com.budgetview.shared.http.Http;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONObject;

import java.io.IOException;

public class CloudAPI {

  public void signup(String email) throws IOException {
    String url = "/signup";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email);
    execute(request, url);
  }

  public JSONObject validate(String email, String validationCode) throws IOException {
    String url = "/validate";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.VALIDATION_CODE, validationCode);
    HttpResponse response = execute(request, url);
    return new JSONObject(Files.loadStreamToString(response.getEntity().getContent(), "UTF-8"));
  }

  public void addConnection(String email, String bvToken, String budgeaToken, Integer budgeaUserId) throws IOException {
    if (Strings.isNullOrEmpty(email)) {
      throw new InvalidParameter("A proper email must be provided to create the connection");
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      throw new InvalidParameter("A proper token must be provided to create the connection");
    }
    if (Strings.isNullOrEmpty(budgeaToken)) {
      throw new InvalidParameter("A non-empty Budgea token must be provided to create the connection");
    }
    if (budgeaUserId == null) {
      throw new InvalidParameter("A budgea userId must be provided to create the connection");
    }

    String url = cloudUrl("/connections");
    Request request = Request.Post(url)
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.TOKEN, bvToken)
      .addHeader(CloudConstants.BUDGEA_TOKEN, budgeaToken)
      .addHeader(CloudConstants.BUDGEA_USER_ID, Integer.toString(budgeaUserId));
    execute(request, url);
  }

  public JSONObject getStatement(String email, String token, Integer lastUpdate) throws IOException {
    String url = lastUpdate == null ? "/statement" : "/statement/" + lastUpdate;
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.TOKEN, token);

    HttpResponse response = execute(request, url);
    return new JSONObject(Files.loadStreamToString(response.getEntity().getContent(), "UTF-8"));
  }

  public HttpResponse execute(Request request, String url) throws IOException {
    Response response = request.execute();
    HttpResponse httpResponse = response.returnResponse();
    return Http.checkResponse(url, httpResponse);
  }

  private String cloudUrl(String path) {
    return CloudConstants.getServerUrl(path);
  }
}

