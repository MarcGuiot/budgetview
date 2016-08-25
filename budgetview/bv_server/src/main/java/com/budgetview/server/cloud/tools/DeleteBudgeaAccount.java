package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.budgea.Budgea;
import com.budgetview.shared.cloud.BudgeaConstants;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

public class DeleteBudgeaAccount {

  public static void main(String[] args) {
    try {
      int idUser = 476;
      String token = "AO6jqstUP2HQMt3vQVPFhV6F9/k0Fw8Q";

      Request request = Request.Put(BudgeaConstants.getServerUrl("/users/" + idUser + "/connections"))
        .addHeader("Authorization", "Bearer " + token)
        .bodyForm(Form.form()
                    .add("client_id", Budgea.CLIENT_ID)
                    .add("client_secret", Budgea.CLIENT_SECRET)
                    .build());


      Response response = request.execute();
      System.out.println("Response: " + response.returnContent().asString());
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
