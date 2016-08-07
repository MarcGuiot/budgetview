package com.budgetview.server.cloud.servlet;

import com.budgetview.server.cloud.Budgea;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.Log4J;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PingServlet extends HttpServlet {

  static Logger logger = Logger.getLogger("/ping");

  public PingServlet(Directory directory) {

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    Log4J.dump(request, logger);
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Log4J.dump(request, logger);
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<head>");
    writer.println("<title>Test BV Cloud</title>");
    writer.println("</head>");
    writer.println("<body>");
    writer.println("Ping OK!");
    writer.println("</body>");
    writer.println("</html>");
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
