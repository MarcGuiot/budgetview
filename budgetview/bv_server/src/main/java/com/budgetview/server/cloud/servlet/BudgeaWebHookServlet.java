package com.budgetview.server.cloud.servlet;

import com.budgetview.server.utils.Log4J;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.Directory;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BudgeaWebHookServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("/budgea");
  private static Pattern pattern = Pattern.compile("Bearer (.*)");

  public BudgeaWebHookServlet(Directory directory) {
    ;
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    Log4J.dump(request, logger);

    String authorization = request.getHeader("Authorization");
    Matcher progressMatcher = pattern.matcher(authorization.trim());
    if (!progressMatcher.matches()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    String token = progressMatcher.group(1);
    logger.info(token);

    InputStream inputStream = request.getInputStream();
    JSONObject root = new JSONObject(IOUtils.toString(inputStream, "UTF-8"));
    for (Object c : root.getJSONArray("connections")) {
      JSONObject connection = (JSONObject)c;
      int userId = connection.getInt("id_user");


    }


    response.setStatus(HttpServletResponse.SC_OK);
  }
}
