package com.budgetview.shared.cloud;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

import java.io.IOException;

public class CloudAPI {

  public void addConnection(String email, String budgeaToken, Integer budgeaUserId) throws IOException {
    Request request = Request.Post(cloudUrl("/connections"))
      .bodyForm(Form.form()
                  .add(CloudConstants.EMAIL, email)
                  .add(CloudConstants.BUDGEA_TOKEN, budgeaToken)
                  .add(CloudConstants.BUDGEA_USER_ID, Integer.toString(budgeaUserId))
                  .build(), Consts.UTF_8);

    Response response = request.execute();
    int statusCode = response.returnResponse().getStatusLine().getStatusCode();
    if (statusCode != 200) {
      throw new IOException("POST to /connections returned " + statusCode + " instead of 200");
    }
  }

  private String cloudUrl(String path) {
    return CloudConstants.getServerUrl(path);
  }
}

