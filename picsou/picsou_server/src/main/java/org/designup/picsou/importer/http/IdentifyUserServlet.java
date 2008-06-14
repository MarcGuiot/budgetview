package org.designup.picsou.importer.http;

import org.designup.picsou.server.ServerRequestProcessingService;
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
    serverRequestProcessingService.identify(SerializedInputOutputFactory.init(inputStream),
                                            SerializedInputOutputFactory.init(outputStream));
  }
}
