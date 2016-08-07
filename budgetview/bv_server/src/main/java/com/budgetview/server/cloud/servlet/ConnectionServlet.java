package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.Budgea;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectionServlet extends HttpServlet {

  static Logger logger = Logger.getLogger("/connections");

  private ConfigService config;

  public ConnectionServlet(Directory directory) {
    this.config = directory.get(ConfigService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    Log4J.dump(request, logger);

    String code = request.getParameter("budgea_token");
    if (Strings.isNullOrEmpty(code)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    registerBudgeaToken(code);
    response.setStatus(HttpServletResponse.SC_OK);
  }

  private void registerBudgeaToken(String code) throws IOException {
    logger.info("Budgea temporary token: " + code);
    Request request = Request.Post("https://budgetview.biapi.pro/2.0/auth/token/access")
      .bodyForm(Form.form()
                  .add("code", code)
                  .add("client_id", config.get(Budgea.CLIENT_ID))
                  .add("client_secret", config.get(Budgea.CLIENT_SECRET))
                  .build());

    Response response = request.execute();
    Log4J.dump(response, logger);
  }
}
