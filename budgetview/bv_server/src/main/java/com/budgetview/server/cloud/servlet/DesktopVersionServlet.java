package com.budgetview.server.cloud.servlet;

import com.budgetview.server.config.ConfigService;
import org.apache.log4j.Logger;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class DesktopVersionServlet extends HttpServlet {

  private static Logger logger = Logger.getLogger("DesktopVersionServlet");

  private Directory directory;

  public DesktopVersionServlet(Directory directory) {
    this.directory = directory;
    loadVersion();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    Version version = loadVersion();
    if (version == null || !version.isValid()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    StringWriter stringContent = new StringWriter();
    JSONWriter writer = new JSONWriter(stringContent);
    writer.object();
    writer.key("jar").value(version.getJar());
    writer.key("version").value(version.getVersion());
    writer.endObject();
    response.setStatus(HttpServletResponse.SC_OK);
    response.getWriter().print(stringContent.getBuffer());

  }

  private Version loadVersion() {
    String path = directory.get(ConfigService.class).get("budgetview.desktop.version.path");
    Properties properties = new Properties();
    try {
      Files.loadProperties(properties, path);
    }
    catch (ResourceAccessFailed resourceAccessFailed) {
      logger.error("Could not load version file from " + path, resourceAccessFailed);
      return null;
    }

    Version version = new Version(properties.getProperty("budgetview.desktop.jar", ""),
                                  properties.getProperty("budgetview.desktop.version", ""));
    if (!version.isValid()) {
      logger.error("Invalid parameters in " + path + " : " + version);
    }
    return version;
  }

  private class Version {
    private String jar;
    private String version;

    public Version(String jar, String version) {
      this.jar = jar;
      this.version = version;
    }

    public boolean isValid() {
      try {
        if (Integer.parseInt(jar) < 1) {
          return false;
        }
      }
      catch (NumberFormatException e) {
        return false;
      }
      return Strings.isNotEmpty(version);
    }

    public Integer getJar() {
      return Integer.parseInt(jar);
    }

    public String getVersion() {
      return version;
    }

    public String toString() {
      return jar + " / " + version;
    }
  }
}
