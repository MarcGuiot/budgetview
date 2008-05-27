package org.designup.picsou.importer.http;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.serialization.SerializedInputOutputFactory;
import org.designup.picsou.server.ServerRequestProcessingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateUserServlet extends ExceptionToStatusHttpServlet {
  private ServerRequestProcessingService serverRequestProcessingService;

  public CreateUserServlet(Directory directory) {
    serverRequestProcessingService = directory.get(ServerRequestProcessingService.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
    serverRequestProcessingService.createUser(
      SerializedInputOutputFactory.init(httpServletRequest.getInputStream()),
      SerializedInputOutputFactory.init(httpServletResponse.getOutputStream()));
  }
}
