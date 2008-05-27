package org.designup.picsou.importer.http;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.server.ServerRequestProcessingService;

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
