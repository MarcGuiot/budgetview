package com.budgetview.server.cloud.servlet;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class BudgeaServlet extends HttpServlet {

  static Logger logger = Logger.getLogger("/budgea");

  private ConfigService config;

  public BudgeaServlet(Directory directory) {
    this.config = directory.get(ConfigService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    Log4J.dump(request, logger);

    response.setStatus(HttpServletResponse.SC_OK);
  }
}
