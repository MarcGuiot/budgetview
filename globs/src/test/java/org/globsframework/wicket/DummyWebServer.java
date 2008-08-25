package org.globsframework.wicket;

import org.apache.wicket.protocol.http.WicketServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class DummyWebServer {
  private Server jetty;
  public static final int PORT = 8082;

  public void start() throws Exception {
    jetty = new Server(PORT);
    Context context = new Context(jetty, "/", Context.SESSIONS);
    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", DummyApplication.class.getName());
    context.addServlet(holder, "/*");

    ErrorHandler handler = new ErrorHandler();
    handler.setShowStacks(true);
    context.setErrorHandler(handler);
    jetty.start();
  }

  public void stop() throws Exception {
    if (jetty != null) {
      jetty.stop();
    }
  }

  public String getUrl(String path) {
    return "http://localhost:" + PORT + "/" + path;
  }
}
