package com.budgetview.desktop.cloud.actions;

import com.budgetview.desktop.cloud.CloudService;
import org.apache.http.Consts;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloudDemoAction extends AbstractAction {
  private final GlobRepository repository;
  private final CloudService cloudService;

  public CloudDemoAction(GlobRepository repository, Directory directory) {
    super("Run cloud demo");
    this.repository = repository;
    this.cloudService = directory.get(CloudService.class);
  }

  public void actionPerformed(ActionEvent e) {
    try {

      Request request = Request.Post("http://127.0.0.1:8080/budgea")
        .bodyForm(Form.form()
                    .add("email", "regis@mybudgetview.fr")
                    .add("code", "zefsdsdfq")
                    .build(), Consts.UTF_8);

      Response response = request.execute();
      System.out.println("CloudDemoAction.actionPerformed: " + response.returnContent().asString());
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
