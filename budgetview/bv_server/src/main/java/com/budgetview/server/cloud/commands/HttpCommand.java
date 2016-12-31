package com.budgetview.server.cloud.commands;

import com.budgetview.shared.cloud.CloudConstants;
import org.apache.log4j.Logger;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.json.JSONWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class HttpCommand implements Command {

  protected final Directory directory;
  protected final HttpServletRequest request;
  protected final HttpServletResponse response;
  protected final Logger logger;

  public HttpCommand(Directory directory, HttpServletRequest request, HttpServletResponse response, Logger logger) {
    this.directory = directory;
    this.request = request;
    this.response = response;
    this.logger = logger;
  }

  public void run() throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    try {
      doRun();
    }
    catch (InvalidHeader invalidHeader) {
      logger.error(invalidHeader.getMessage());
      setBadRequest(response);
      return;
    }
  }

  protected abstract void doRun() throws IOException, InvalidHeader;

  protected void setOk(HttpServletResponse response) throws IOException {
    JSONWriter writer = new JSONWriter(response.getWriter());
    writer.object();
    setOk(response, writer);
    writer.endObject();
  }

  protected void setOk(HttpServletResponse response, JSONWriter writer) {
    writer.key(CloudConstants.STATUS).value("ok");
    response.setStatus(HttpServletResponse.SC_OK);
  }

  protected void setInternalError(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }

  protected void setUnauthorized(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  protected void setBadRequest(HttpServletResponse response) {
    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
      throw new NonIntHeader(header, value);
    }
  }

  protected int getIntHeader(String header) throws AuthenticatedCommand.InvalidHeader {
    String value = getStringHeader(header);
    try {
      return Integer.parseInt(value.trim());
    }
    catch (NumberFormatException e) {
      throw new NonIntHeader(header, value);
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

  protected class NonIntHeader extends InvalidHeader {
    public NonIntHeader(String header, String value) {
      super("Header should be an int: " + header + " has value: " + value);
    }
  }
}
