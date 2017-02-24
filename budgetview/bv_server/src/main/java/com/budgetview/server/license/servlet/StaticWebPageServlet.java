package com.budgetview.server.license.servlet;

import com.budgetview.server.config.ConfigService;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class StaticWebPageServlet extends HttpServlet {

  static Logger logger = Logger.getLogger("StaticWebPageServlet");

  private File file;
  private long lastModified = -1;
  private String content;

  public StaticWebPageServlet(Directory directory) {
    ConfigService configService = directory.get(ConfigService.class);
    String path = configService.get("bv.web.store.path");
    if (Strings.isNullOrEmpty(path)) {
      logger.error("bv.web.store.path not defined");
      file = null;
    }
    else {
      file = new File(path);
      if (!file.exists()) {
        logger.error("File " + file.getAbsolutePath() + " not found");
        file = null;
      }
    }
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (file == null) {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return;
    }

    long newLastModified = file.lastModified();
    if (newLastModified != lastModified) {
      content = Files.loadFileToString(file);
      lastModified = newLastModified;
    }

    response.getWriter().print(content);
    response.setStatus(HttpServletResponse.SC_OK);
  }
}
