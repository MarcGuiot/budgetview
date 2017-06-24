package com.budgetview.server.common;

import com.budgetview.server.config.ConfigService;
import org.apache.log4j.Logger;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ServerStatusServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("ServerStatusServlet");

  private final String authenticationCode;

  public ServerStatusServlet(Directory directory) {
    authenticationCode = directory.get(ConfigService.class).get("budgetview.status.authorization");
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    if (!authenticationCode.equals(request.getHeader("Authorization"))) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.println("<html>");
    writer.println("<body>");
    writer.println("Status: " + getStatus());
    writer.println("</body>");
    writer.println("</html>");

    response.setStatus(HttpServletResponse.SC_OK);
  }

  private String getStatus() {
    double usage = memoryUsage();
    boolean lowMemory = usage > 95;
    if (lowMemory) {
      logger.error("Low memory warning: " + usage);
    }
    return lowMemory ? "low memory" : "OK";
  }

  private double memoryUsage() {
    Runtime instance = Runtime.getRuntime();
    return (double) instance.freeMemory() * 100 / (double) instance.totalMemory();
  }
}
