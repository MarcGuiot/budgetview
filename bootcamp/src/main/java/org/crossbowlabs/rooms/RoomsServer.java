package org.globsframework.rooms;

import org.globsframework.rooms.web.RoomsApplication;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.apache.wicket.protocol.http.WicketServlet;

import java.util.Locale;

public class RoomsServer {
  private static final int PORT = 8081;

  public static void main(String[] args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    org.mortbay.jetty.Server jetty = new org.mortbay.jetty.Server(PORT);

    Context context = new Context(jetty, "/", Context.SESSIONS);
    context.setResourceBase("classes");

    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", RoomsApplication.class.getName());
    context.addServlet(holder, "/*");

    jetty.start();
  }

}
