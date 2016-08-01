package com.budgetview.license.servlet;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class DummyServer {

  public static final String KEYSTORE = "bv.server.keystore";

  public static void main(String[] args) throws Exception {
    // Since this example shows off SSL configuration, we need a keystore
    // with the appropriate key. These lookup of jetty.home is purely a hack
    // to get access to a keystore that we use in many unit tests and should
    // probably be a direct path to your own keystore.

    String keystorePath = System.getProperty(KEYSTORE);
    File keystoreFile = new File(keystorePath);
    if (!keystoreFile.exists()) {
      throw new FileNotFoundException(keystoreFile.getAbsolutePath());
    }

    // Create a basic jetty server object without declaring the port. Since
    // we are configuring connectors directly we'll be setting ports on
    // those connectors.
    Server server = new Server();

    // HTTP Configuration
    // HttpConfiguration is a collection of configuration information
    // appropriate for http and https. The default scheme for http is
    // <code>http</code> of course, as the default for secured http is
    // <code>https</code> but we show setting the scheme to show it can be
    // done. The port for secured communication is also set here.
    HttpConfiguration http_config = new HttpConfiguration();
    http_config.setSecureScheme("https");
    http_config.setSecurePort(8443);
    http_config.setOutputBufferSize(32768);

    // HTTP connector
    // The first server connector we create is the one for http, passing in
    // the http configuration we configured above so it can get things like
    // the output buffer size, etc. We also set the port (8080) and
    // configure an idle timeout.
    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
    http.setPort(8088);
    http.setIdleTimeout(30000);

    // SSL Context Factory for HTTPS
    // SSL requires a certificate so we configure a factory for ssl contents
    // with information pointing to what keystore the ssl connection needs
    // to know about. Much more configuration is available the ssl context,
    // including things like choosing the particular certificate out of a
    // keystore to be used.
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
    sslContextFactory.setKeyStorePassword("BvF69gEdG6");
    sslContextFactory.setKeyManagerPassword("BvF69gEdG6");
    sslContextFactory.setTrustStorePath(keystoreFile.getAbsolutePath());
    sslContextFactory.setTrustStorePassword("BvF69gEdG6");

    // HTTPS Configuration
    // A new HttpConfiguration object is needed for the next connector and
    // you can pass the old one as an argument to effectively clone the
    // contents. On this HttpConfiguration object we add a
    // SecureRequestCustomizer which is how a new connector is able to
    // resolve the https connection before handing control over to the Jetty
    // Server.
    HttpConfiguration https_config = new HttpConfiguration(http_config);
    SecureRequestCustomizer src = new SecureRequestCustomizer();
    src.setStsMaxAge(2000);
    src.setStsIncludeSubDomains(true);
    https_config.addCustomizer(src);

    // HTTPS connector
    // We create a second ServerConnector, passing in the http configuration
    // we just made along with the previously created ssl context factory.
    // Next we set the port and a longer idle timeout.
    ServerConnector https = new ServerConnector(server,
                                                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                                                new HttpConnectionFactory(https_config));
    https.setPort(8443);
    https.setIdleTimeout(500000);

    // Here you see the server having multiple connectors registered with
    // it, now requests can flow into the server from both http and https
    // urls to their respective ports and be processed accordingly by jetty.
    // A simple handler is also registered with the server so the example
    // has something to pass requests off to.

    // Set the connectors
    server.setConnectors(new Connector[]{http, https});

    // Set a handler
    server.setHandler(new HelloHandler());

    // Start the server
    server.start();
    server.join();
  }

  private static class HelloHandler extends AbstractHandler {
    final String greeting;
    final String body;

    public HelloHandler() {
      this("Hello World");
    }

    public HelloHandler(String greeting) {
      this(greeting, null);
    }

    public HelloHandler(String greeting, String body) {
      this.greeting = greeting;
      this.body = body;
    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {

      System.out.println("Request: " + request.getQueryString());

      response.setContentType("text/html; charset=utf-8");
      response.setStatus(HttpServletResponse.SC_OK);

      PrintWriter out = response.getWriter();

      out.println("<h1>" + greeting + "</h1>");
      if (body != null) {
        out.println(body);
      }

      baseRequest.setHandled(true);
    }
  }
}
