package com.budgetview.io.importer.http;

import com.budgetview.client.http.HttpsClientTransport;
import com.budgetview.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IdentifyUserServlet extends ExceptionToStatusHttpServlet {
  private ServerRequestProcessingService serverRequestProcessingService;

  public IdentifyUserServlet(Directory directory) {
    serverRequestProcessingService =
      directory.get(ServerRequestProcessingService.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    InputStream inputStream = httpServletRequest.getInputStream();
    OutputStream outputStream = httpServletResponse.getOutputStream();
    String sessionId = httpServletRequest.getHeader(HttpsClientTransport.SESSION_ID);
    serverRequestProcessingService.identify(Long.parseLong(sessionId), SerializedInputOutputFactory.init(inputStream),
                                            SerializedInputOutputFactory.init(outputStream));
  }
}
