package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.Budgea;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;

public class ForceBudgeaUpdate {

//  {
//    "last_update": "2016-08-07 17:55:12",
//    "expire": null,
//    "active": true,
//    "id_user": 476,
//    "id_bank": 40,
//    "id": 272,
//    "error": null
//  }

  public static void main(String[] args) {
    try {

      int idUser = 476;
      String token = "AO6jqstUP2HQMt3vQVPFhV6F9/k0Fw8Q";

      Request request = Request.Put("https://budgetview.biapi.pro/2.0/users/" + idUser + "/connections")
        .addHeader("Authorization", "Bearer " + token)
      .bodyForm(Form.form()
                  .add("client_id", "60443827")
                  .add("client_secret", "E9W5QStthEi7mh7+ARAZV2wIRS0eY4o7")
                  .build());

      Response response = request.execute();
      System.out.println("Response: " + response.returnContent().asString());
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }
}
