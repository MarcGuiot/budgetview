package com.budgetview.server.cloud.servlet;

import com.budgetview.server.config.ConfigService;
import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class UserServlet extends HttpServlet {

  private ConfigService config;

  public UserServlet(Directory directory) {
    this.config = directory.get(ConfigService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");


//    registerBudgeaWebhook();


  }

  private void registerBudgeaWebhook(String code) {
    Request request = Request.Post("https://budgetview.biapi.pro/2.0/users/me/connections")
      .bodyForm(Form.form()
                  .add("id_bank", "")
                  .add("id_bank", "")
                  .add("id_bank", "")
                  .build());

  }
}
