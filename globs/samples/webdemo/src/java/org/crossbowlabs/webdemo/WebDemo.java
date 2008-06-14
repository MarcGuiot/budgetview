package org.globsframework.webdemo;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import wicket.protocol.http.WicketServlet;

import java.util.Locale;

public class WebDemo {
  private static final int PORT = 8081;

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    Server jetty = new Server(PORT);

    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("classes");

    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", WebDemoApplication.class.getName());
    context.addServlet(holder, "/*");

    jetty.start();
  }
}
