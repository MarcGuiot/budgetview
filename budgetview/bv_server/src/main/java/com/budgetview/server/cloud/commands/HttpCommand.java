package com.budgetview.server.cloud.commands;

import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.json.JsonGlobWriter;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.json.JSONObject;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public abstract class HttpCommand implements Command {

  protected final HttpServletRequest request;
  protected final HttpServletResponse response;
  protected final Logger logger;

  public HttpCommand(HttpServletRequest request, HttpServletResponse response, Logger logger) {
    this.request = request;
    this.response = response;
    this.logger = logger;
  }

  public void run() throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    try {
      StringWriter stringContent = new StringWriter();
      JsonGlobWriter writer = new JsonGlobWriter(stringContent);
      int status = doRun(writer);
      response.setStatus(status);
      response.getWriter().print(stringContent.getBuffer());
    }
    catch (InvalidHeader invalidHeader) {
      logger.error(invalidHeader.getMessage());
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
    catch (Exception e) {
      logger.error("Error processing command " + getClass().getSimpleName(), e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  protected abstract int doRun(JsonGlobWriter writer) throws IOException, InvalidHeader;

  protected void setOk(JSONWriter writer) {
    writer.object();
    writer.key(CloudConstants.API_VERSION).value(CloudConstants.CURRENT_API_VERSION);
    writer.key(CloudConstants.STATUS).value("ok");
    writer.endObject();
  }

  protected Integer getOptionalIntHeader(String header) throws AuthenticatedCommand.InvalidHeader {
    String value = getOptionalStringHeader(header);
    if (value == null) {
      return null;
    }
    try {
      return Integer.parseInt(value.trim());
    }
    catch (NumberFormatException e) {
      throw new NonIntHeaderValue(header, value);
    }
  }

  protected int getIntHeader(String header) throws AuthenticatedCommand.InvalidHeader {
    String value = getStringHeader(header);
    try {
      return Integer.parseInt(value.trim());
    }
    catch (NumberFormatException e) {
      throw new NonIntHeaderValue(header, value);
    }
  }

  protected String getOptionalStringHeader(String header) {
    return request.getHeader(header);
  }

  protected String getStringHeader(String header) throws AuthenticatedCommand.InvalidHeader {
    String value = getOptionalStringHeader(header);
    if (Strings.isNullOrEmpty(value)) {
      throw new MissingHeader(header);
    }
    return value;
  }

  protected JSONObject getRequestBodyAsJson() throws IOException {
    InputStream inputStream = request.getInputStream();
    String json = Files.loadStreamToString(inputStream, "UTF-8");
    return new JSONObject(json);
  }

  protected abstract class InvalidHeader extends Exception {
    public InvalidHeader(String message) {
      super(message);
    }
  }

  protected class MissingHeader extends InvalidHeader {
    public MissingHeader(String header) {
      super("Missing header: " + header);
    }
  }

  protected class NonIntHeaderValue extends InvalidHeader {
    public NonIntHeaderValue(String header, String value) {
      super("Header: " + header + " should be an int but was: " + value);
    }
  }

  protected class InvalidHeaderValue extends InvalidHeader {
    public InvalidHeaderValue(String header, String value) {
      super("Invalid value: " + value + " for header: " + header);
    }
  }
}
