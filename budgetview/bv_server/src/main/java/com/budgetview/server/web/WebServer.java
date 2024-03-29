package com.budgetview.server.web;

import com.budgetview.server.config.ConfigService;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.servlet.http.HttpServlet;
import java.io.File;

public class WebServer {

  public static final String HOST_PROPERTY = "bv.host";
  public static final String HTTP_PORT_PROPERTY = "bv.http.port"; // License: null - Mobile: 8080
  public static final String HTTPS_PORT_PROPERTY = "bv.https.port"; // License: 443 - Mobile: 1443
  public static final String KEYSTORE_PATH = "bv.https.keystore"; // full bv.jks path
  public static final String KEYSTORE_PWD = "bv.https.keystore.pwd"; // full bv.jks path

  private final String host;
  private final Integer httpsPort;
  private final Integer httpPort;
  private final Server jetty;
  private final ServletContextHandler context;

  public WebServer(ConfigService config) {

    host = config.get(HOST_PROPERTY);

    QueuedThreadPool threadPool = new QueuedThreadPool();
    threadPool.setMaxThreads(50);
    jetty = new Server(threadPool);

    httpsPort = config.getInt(HTTPS_PORT_PROPERTY);
    if (httpsPort == null) {
      throw new InvalidParameter("HTTPS port must be set with " + HTTPS_PORT_PROPERTY);
    }

    HttpConfiguration httpConfig = new HttpConfiguration();
    httpConfig.setSecureScheme("https");
    httpConfig.setSecurePort(httpsPort);
    httpConfig.setOutputBufferSize(32768);

    String sslKeystorePassword = config.get(KEYSTORE_PWD);
    String keystorePath = config.get(KEYSTORE_PATH);
    File keystoreFile = new File(keystorePath);
    if (!keystoreFile.exists()) {
      throw new InvalidParameter("Could not load keystore file: " + keystoreFile.getAbsolutePath() + " - must be set with " + KEYSTORE_PATH + " property");
    }

    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(keystorePath);
    sslContextFactory.setKeyStorePassword(sslKeystorePassword);
    sslContextFactory.setKeyManagerPassword(sslKeystorePassword);
    sslContextFactory.setTrustStorePath(keystorePath);
    sslContextFactory.setTrustStorePassword(sslKeystorePassword);

    HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
    SecureRequestCustomizer src = new SecureRequestCustomizer();
    src.setStsMaxAge(2000);
    src.setStsIncludeSubDomains(true);
    httpsConfig.addCustomizer(src);

    ServerConnector https = new ServerConnector(jetty,
                                                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                                                new HttpConnectionFactory(httpsConfig));
    https.setHost(host);
    https.setPort(httpsPort);
    https.setIdleTimeout(500000);
    jetty.addConnector(https);

    httpPort = config.getInt(HTTP_PORT_PROPERTY, null);
    if (httpPort != null) {
      ServerConnector http = new ServerConnector(jetty, new HttpConnectionFactory(httpConfig));
      http.setHost(host);
      http.setPort(httpPort);
      http.setIdleTimeout(30000);
      jetty.addConnector(http);
    }

    context = new ServletContextHandler(jetty, "/", ServletContextHandler.SESSIONS);
    context.setResourceBase("resources");
  }

  public void setHandler(Handler handler) {
    jetty.setHandler(handler);
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
    jetty.destroy();
  }

  public Integer getHttpsPort() {
    return httpsPort;
  }

  public String info() {
    StringBuilder builder = new StringBuilder();
    builder.append("listening on ");
    if (httpPort != null) {
      builder.append("http://").append(host).append(":").append(httpPort);
    }
    if (httpPort != null && httpsPort != null) {
      builder.append(" and ");
    }
    if (httpsPort != null) {
      builder.append("https://").append(host).append(":").append(httpsPort);
    }
    return builder.toString();
  }
}
