package com.budgetview.importer.http;

import com.budgetview.client.http.HttpsClientTransport;
import com.budgetview.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DisconnectServlet extends ExceptionToStatusHttpServlet {
  private ServerRequestProcessingService serverRequestProcessingService;

  public DisconnectServlet(Directory directory) {
    serverRequestProcessingService = directory.get(ServerRequestProcessingService.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    throws IOException {
    String sessionId = httpServletRequest.getHeader(HttpsClientTransport.SESSION_ID);
    serverRequestProcessingService.disconnect(Long.parseLong(sessionId),
                                              SerializedInputOutputFactory.init(httpServletRequest.getInputStream()));
  }
}
