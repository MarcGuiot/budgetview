package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.UserDataService;
import com.budgetview.server.cloud.UserDataSet;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BudgeaServlet extends HttpServlet {

  static Logger logger = Logger.getLogger("/budgea");

  Pattern pattern = Pattern.compile("Bearer (.*)");

  private final UserDataService userDataService;

  public BudgeaServlet(Directory directory) {
    this.userDataService = directory.get(UserDataService.class);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    String authorization = request.getHeader("Authorization");
    Matcher progressMatcher = pattern.matcher(authorization.trim());
    if (!progressMatcher.matches()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = progressMatcher.group(1);
    logger.info(token);

    UserDataSet dataSet = userDataService.getDataSet(token);

    InputStream inputStream = request.getInputStream();
    JSONObject root = new JSONObject(IOUtils.toString(inputStream, "UTF-8"));
    dataSet.processBudgeaUpdate(root);

    response.setStatus(HttpServletResponse.SC_OK);
  }
}
