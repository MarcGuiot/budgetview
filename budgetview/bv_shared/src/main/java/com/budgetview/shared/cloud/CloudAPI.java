package com.budgetview.shared.cloud;

import com.budgetview.shared.http.Http;
import com.budgetview.shared.model.Provider;
import org.apache.http.client.fluent.Request;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.json.JSONObject;

import java.io.IOException;

public class CloudAPI {

  public JSONObject signup(String email) throws IOException {
    String url = "/signup";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email);
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject validate(String email, String validationCode) throws IOException {
    String url = "/validate";
    Request request = Request.Post(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.VALIDATION_CODE, validationCode);
    return Http.executeAndGetJson(url, request);
  }

  public JSONObject getTemporaryBudgeaToken(String email, String bvToken) throws IOException {
    if (Strings.isNullOrEmpty(email)) {
      throw new InvalidParameter("A proper email must be provided to get a token");
    }
    if (Strings.isNullOrEmpty(bvToken)) {
      throw new InvalidParameter("A proper token must be provided to get a token");
    }
    String url = "/budgea/token";
    Request request = Request.Get(CloudConstants.getServerUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.BV_TOKEN,  bvToken);
    return Http.executeAndGetJson(url, request);
  }

  public void addBudgeaConnection(String email, String bvToken, String budgeaToken, Integer budgeaUserId) throws IOException {
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
      .addHeader(CloudConstants.BV_TOKEN, bvToken)
      .addHeader(CloudConstants.PROVIDER, Integer.toString(Provider.BUDGEA.getId()))
      .addHeader(CloudConstants.BUDGEA_TOKEN, budgeaToken)
      .addHeader(CloudConstants.BUDGEA_USER_ID, Integer.toString(budgeaUserId));
    Http.execute(request, url);
  }

  public JSONObject getConnections(String email, String bvToken) throws IOException {
    String url = "/connections";
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.BV_TOKEN, bvToken);

    return Http.executeAndGetJson(url, request);
  }

  public JSONObject getStatement(String email, String bvToken, Integer lastUpdate) throws IOException {
    String url = lastUpdate == null ? "/statement" : "/statement/" + lastUpdate;
    Request request = Request.Get(cloudUrl(url))
      .addHeader(CloudConstants.EMAIL, email)
      .addHeader(CloudConstants.BV_TOKEN, bvToken);

    return Http.executeAndGetJson(url, request);
  }

  private String cloudUrl(String path) {
    return CloudConstants.getServerUrl(path);
  }
}

