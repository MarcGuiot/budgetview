package com.budgetview.server.utils;

import com.budgetview.server.config.ConfigService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.fluent.Response;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.OperationFailed;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public class Log4J {

  private static final String LOG4_PROPERTIES_PATH = "budgetview.log4.config.path";

  public static void init(ConfigService config) throws Exception {

    File file = new File(config.get(LOG4_PROPERTIES_PATH));
    if (!file.exists()) {
      throw new InvalidParameter("log4 properties file '" + file.getAbsolutePath() + "' does not exist - check " + LOG4_PROPERTIES_PATH);
    }

    try {
      PropertyConfigurator.configure(file.getAbsolutePath());
    }
    catch (Exception e) {
      throw new OperationFailed("Failed to init log4j - /log4j.properties file not found in JAR");
    }
  }

  public static void dump(HttpServletRequest request, Logger logger) {

    StringBuilder builder = new StringBuilder();
    builder.append("  Method: " + request.getMethod() + "\n");
    Enumeration headerNames = request.getHeaderNames();
    builder.append("  Headers:\n");
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      builder.append("    " + headerName + ":  " + request.getHeader(headerName) + "\n");
    }
    builder.append("  Parameters:\n");
    Enumeration params = request.getParameterNames();
    while (params.hasMoreElements()) {
      String paramName = (String) params.nextElement();
      builder.append("    " + paramName + ": " + request.getParameter(paramName) + "\n");
    }

    logger.info("Request: \n" + builder.toString());
  }

  public static void dump(Response response, Logger logger) throws IOException {
    StatusLine statusLine = response.returnResponse().getStatusLine();
    logger.info(statusLine.getStatusCode() + " / " + statusLine.getReasonPhrase());

  }
}
