package org.designup.picsou.importer.http;

import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateUserServlet extends ExceptionToStatusHttpServlet {
  private ServerRequestProcessingService serverRequestProcessingService;

  public CreateUserServlet(Directory directory) {
    serverRequestProcessingService = directory.get(ServerRequestProcessingService.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    String sessionId = httpServletRequest.getHeader(HttpsClientTransport.SESSION_ID);
    serverRequestProcessingService.createUser(Long.parseLong(sessionId),
                                              SerializedInputOutputFactory.init(httpServletRequest.getInputStream()),
                                              SerializedInputOutputFactory.init(httpServletResponse.getOutputStream()));
  }
}
