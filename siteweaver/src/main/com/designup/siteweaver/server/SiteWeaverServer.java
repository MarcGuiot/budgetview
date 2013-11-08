package com.designup.siteweaver.server;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;

import java.io.File;

public class SiteWeaverServer {
  public static void main(String[] args) throws Exception {
    Server server = new Server();

    Connector connector = new SelectChannelConnector();
    connector.setHost("localhost");
    connector.setPort(8080);
    server.addConnector(connector);

    File configFilePath = new File(args[0]);

    server.setHandler(new PageHandler(configFilePath));

    server.start();
    String url = "http://" + connector.getHost() + ":" + connector.getPort();
    System.out.println("Listening on " + url + " (site content available at " + url + "/dump)");
    server.join();
  }
}

