package com.budgetview.shared.cloud;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;

public class CloudAPI {
  private String rootUrl;

  public CloudAPI(String rootUrl) {
    this.rootUrl = rootUrl;
  }

  public void addConnection(String budgeaToken, Integer budgeaUserId) throws IOException {
    Request request = Request.Post(cloudUrl("/connections"))
      .bodyForm(Form.form()
                  .add("budgea_token", budgeaToken)
                  .add("budgea_user_id", Integer.toString(budgeaUserId))
                  .build(), Consts.UTF_8);

    Response response = request.execute();
    int statusCode = response.returnResponse().getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new IOException("Call to /connections returned " + statusCode + " instead of 200");
    }
  }

  private String cloudUrl(String s) {
    return rootUrl + s;
  }
}

