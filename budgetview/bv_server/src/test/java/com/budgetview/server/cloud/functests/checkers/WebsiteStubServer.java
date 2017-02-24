package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.web.WebServer;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class WebsiteStubServer {

  private static Logger logger = Logger.getLogger("WebsiteStubServer");

  private WebServer webServer;
  private String lastVisitedPage;

  public WebsiteStubServer(String... args) throws IOException {
    ConfigService configService = new ConfigService(args);
    this.webServer = new WebServer(configService);
  }

  public void init() {
    webServer.add(new StaticPageServlet(), "/*");
  }

  public void start() throws Exception {
    logger.info("starting server");
    webServer.start();
    logger.info("server started");
  }

  public void stop() throws Exception {
    logger.info("stopping server");
    webServer.stop();
    logger.info("server stopped");
  }

  public void checkLastVisitedPage(String path) {
    assertEquals(path, lastVisitedPage);
  }

  private class StaticPageServlet extends HttpServlet {
    private Logger logger = Logger.getLogger("WebsiteStubServer servlet");

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      logger.info("GET");
      response.setStatus(HttpServletResponse.SC_OK);
      WebsiteStubServer.this.lastVisitedPage = request.getContextPath();
    }
  }
}
