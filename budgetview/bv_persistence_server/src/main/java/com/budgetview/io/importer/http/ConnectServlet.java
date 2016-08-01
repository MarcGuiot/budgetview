package com.budgetview.io.importer.http;

import com.budgetview.server.ServerRequestProcessingService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ConnectServlet extends ExceptionToStatusHttpServlet {
  private ServerRequestProcessingService serverRequestProcessingService;

  public ConnectServlet(Directory directory) {
    serverRequestProcessingService = directory.get(ServerRequestProcessingService.class);
  }

  protected void action(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    throws IOException {
    SerializedInput input = getFalseInByteArray();
    serverRequestProcessingService.connect(input,
                                           SerializedInputOutputFactory.init(httpServletResponse.getOutputStream()));
  }

  private SerializedInput getFalseInByteArray() {
    SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
    SerializedOutput output = serializedByteArrayOutput.getOutput();
    output.writeBoolean(false);
    return serializedByteArrayOutput.getInput();
  }
}