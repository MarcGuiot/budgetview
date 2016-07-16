package com.budgetview.license.servlet;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.servlet.http.HttpServlet;
import java.io.Console;
import java.io.File;

public class WebServer {

  public static final String HTTP_PORT_PROPERTY = "bv.http.port"; // License: null - Mobile: 8080
  public static final String HTTPS_PORT_PROPERTY = "bv.https.port"; // License: 443 - Mobile: 1443
  public static final String KEYSTORE = "bv.https.keystore"; // full bv.jks path

  private final Integer httpsPort;
  private final Integer httpPort;
  private final Server jetty;
  private final ServletContextHandler context;

  public WebServer(String defaultHost, Integer defaultHttpPort, Integer defaultHttpsPort) {

    QueuedThreadPool threadPool = new QueuedThreadPool();
    threadPool.setMaxThreads(50);
    jetty = new Server(threadPool);

    httpsPort = getPort(HTTPS_PORT_PROPERTY, defaultHttpsPort);
    if (httpsPort == null) {
      throw new InvalidParameter("HTTPS port must be set with " + HTTPS_PORT_PROPERTY);
    }

    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSecureScheme("https");
    httpConfig.setSecurePort(httpsPort);
    httpConfig.setOutputBufferSize(32768);

    String sslKeystorePassword = readPassword();
    String keystorePath = System.getProperty(KEYSTORE);
    File keystoreFile = new File(keystorePath);
    if (!keystoreFile.exists()) {
      throw new InvalidParameter("Could not load keystore file: " + keystorePath + " - must be set with " + KEYSTORE + " property");
    }

    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(keystorePath);
    sslContextFactory.setKeyStorePassword(sslKeystorePassword);
    sslContextFactory.setKeyManagerPassword(sslKeystorePassword);
    sslContextFactory.setTrustStorePath(keystorePath);
    sslContextFactory.setTrustStorePassword(sslKeystorePassword);
//      sslContextFactory.setIncludeCipherSuites(MobileConstants.CYPHER_SUITES);

//    SslSocketConnector sslConnector = new SslSocketConnector();
//    sslConnector.setHeaderBufferSize(1024 * 1024);
//    sslConnector.setRequestBufferSize(1024 * 1024);

    HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
    SecureRequestCustomizer src = new SecureRequestCustomizer();
    src.setStsMaxAge(2000);
    src.setStsIncludeSubDomains(true);
    httpsConfig.addCustomizer(src);

    ServerConnector https = new ServerConnector(jetty,
                                                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                                                new HttpConnectionFactory(httpsConfig));
    https.setPort(httpsPort);
    https.setIdleTimeout(500000);
    jetty.addConnector(https);

    httpPort = getPort(HTTP_PORT_PROPERTY, defaultHttpPort);
    if (httpPort != null) {
      ServerConnector http = new ServerConnector(jetty, new HttpConnectionFactory(httpConfig));
      http.setPort(httpPort);
      http.setIdleTimeout(30000);
      jetty.addConnector(http);
    }

    context = new ServletContextHandler(jetty, "/", ServletContextHandler.SESSIONS);
    context.setResourceBase("resources");
  }

  public void add(HttpServlet servlet, String path) {
    context.addServlet(new ServletHolder(servlet), path);
  }

  public void start() throws Exception {
    jetty.start();
  }

  public void stop() throws Exception {
    jetty.stop();
    jetty.join();
  }

  public Integer getHttpPort() {
    return httpPort;
  }

  private static Integer getPort(String property, Integer defaultValue) {
    String port = System.getProperty(property, "");
    if (Strings.isNullOrEmpty(port)) {
      return defaultValue;
    }
    return Integer.parseInt(port);
  }

  private static String readPassword() {
    Console console = System.console();
    if (console == null) {
      System.out.println("Couldn't get Console instance");
      System.exit(0);
    }

    char passwordArray[] = console.readPassword("Enter SSL keystore password: ");
    return new String(passwordArray);
  }

  public Integer getHttpsPort() {
    return httpsPort;
  }
}
