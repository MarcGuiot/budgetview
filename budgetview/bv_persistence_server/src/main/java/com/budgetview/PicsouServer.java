package com.budgetview;

import com.budgetview.io.importer.http.*;
import com.budgetview.server.ServerDirectory;
import com.budgetview.server.session.SessionService;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PicsouServer {
  public static final String USE_SSHL = "picsou.server.useSsl";
  public static final String HOST_PROPERTY = "picsou.server.host";
  public static final String KEYSTORE = "picsou.server.keystore";
  public static final String DELETE_SERVER_PROPERTY = "picsou.server.prevayler.delete";
  public static final String SERVER_PREVAYLER_PATH_PROPERTY = "picsou.server.prevayler.path";
  private Server server;
  private ServerDirectory serverDirectory;
  private static final int ONE_HOUR_IN_MS = 60 * 60 * 1000;
  private Timer timer;
  private TimerTask timerTask;

  public static void main(String[] args) throws Exception {
    new PicsouServer().start();
  }

  public void start() throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    server = new Server();
    String useSsl = System.getProperty(USE_SSHL);
    if (useSsl == null || useSsl.equalsIgnoreCase("true")) {
      ServerConnector sslConnector = new ServerConnector(server);
      String host = System.getProperty(HOST_PROPERTY);
      if (host == null) {
        host = "localhost";
      }
      sslConnector.setHost(host);
      sslConnector.setPort(8443);
      HttpConfiguration https = new HttpConfiguration();
      https.addCustomizer(new SecureRequestCustomizer());
      SslContextFactory sslContextFactory = new SslContextFactory();
      String keyStore = System.getProperty(KEYSTORE);
      if (keyStore == null) {
        keyStore = "picsou_server/resources/.keystore";
      }
      sslContextFactory.setKeyStorePath(keyStore);
      sslContextFactory.setKeyStorePassword("ninja600");
      sslContextFactory.setKeyManagerPassword("ninja600");
      server.addConnector(sslConnector);

    }
    else {
      ServerConnector connector = new ServerConnector(server);
      connector.setHost("localhost");
      connector.setPort(8443);
      server.addConnector(connector);
    }

    String prevaylerPath = getServerPrevaylerPath();
    if (!initPrevaylerDirectory(prevaylerPath)) {
      return;
    }

    initGarbageSession();

    ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
    context.setResourceBase("classes");
    serverDirectory = new ServerDirectory(prevaylerPath, false);
    Directory directory = serverDirectory.getServiceDirectory();
    context.addServlet(new ServletHolder(new IdentifyUserServlet(directory)), "/identifyUser");
    context.addServlet(new ServletHolder(new ConfirmUserServlet(directory)), "/confirmUser");
    context.addServlet(new ServletHolder(new AddUserDataServlet(directory)), "/addUserData");
    context.addServlet(new ServletHolder(new GetUserDataServlet(directory)), "/getUserData");
    context.addServlet(new ServletHolder(new CreateUserServlet(directory)), "/createUser");
    context.addServlet(new ServletHolder(new DisconnectServlet(directory)), "/disconnect");
    context.addServlet(new ServletHolder(new ConnectServlet(directory)), "/connect");

    server.start();
  }

  private void initGarbageSession() {
    timerTask = new TimerTask() {
      public void run() {
        SessionService service = serverDirectory.getServiceDirectory().get(SessionService.class);
        service.flushStateBefore(scheduledExecutionTime() - ONE_HOUR_IN_MS);
      }
    };
    timer = new Timer("garbage session", true);
    timer.scheduleAtFixedRate(timerTask, ONE_HOUR_IN_MS, ONE_HOUR_IN_MS);
  }

  private boolean initPrevaylerDirectory(String prevaylerPath) {
    File prevaylerDirectory = new File(prevaylerPath);
    if ("true".equals(System.getProperty(DELETE_SERVER_PROPERTY))) {
      Files.deleteWithSubtree(prevaylerDirectory);
    }
    if (!prevaylerDirectory.exists()) {
      if (!prevaylerDirectory.mkdir()) {
        System.out.println("unable to create directory " + prevaylerDirectory.getAbsolutePath());
        return false;
      }
    }
    return true;
  }

  private String getServerPrevaylerPath() {
    String prevaylerPath = System.getProperty(SERVER_PREVAYLER_PATH_PROPERTY);
    if (prevaylerPath == null) {
      prevaylerPath = "data";
    }
    return prevaylerPath;
  }

  public void stop() {
    try {
      timerTask.cancel();
      timer.cancel();
      server.stop();
      serverDirectory.close();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
