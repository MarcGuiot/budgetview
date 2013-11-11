package com.designup.siteweaver;

import com.designup.siteweaver.server.PageHandler;
import com.designup.siteweaver.server.upload.FileAccess;
import com.designup.siteweaver.server.upload.FtpFileAccess;
import com.designup.siteweaver.server.upload.NoOpFileAccess;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;

import java.io.File;
import java.io.IOException;

public class SiteWeaver {

  public static void main(String args[]) throws Exception {

    checkUsage(args);
    checkJavaVersion();

    Server server = new Server();

    Connector connector = new SelectChannelConnector();
    connector.setHost("localhost");
    connector.setPort(8080);
    connector.setMaxIdleTime(1000 * 60 * 60);
    server.addConnector(connector);

    server.setHandler(new PageHandler(new File(args[0]), getFileAccess(args)));

    server.start();
    String url = "http://" + connector.getHost() + ":" + connector.getPort();
    System.out.println("Listening on " + url + "\n" +
                       "Available commands:\n" +
                       "  /!dump\n" +
                       "  /!diff\n" +
                       "  /!publish\n");
    server.join();

    getFileAccess(args).dispose();
  }

  private static FileAccess getFileAccess(String[] args) throws IOException {
    if (args.length > 1) {
      return new FtpFileAccess(args[1], args[2], args[3], args[4]);
    }
    return new NoOpFileAccess();
  }

  private static void checkUsage(String[] args) {
    if (args.length != 1 && args.length < 5) {
      System.out.println("Usage: java SiteWeaver <configFile> <ftp_host> <ftp_dir> <ftp_user> <ftp_pwd>");
      System.exit(-1);
    }
  }

  private static void checkJavaVersion() {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion.compareTo("1.1.2") < 0) {
      System.out.println("WARNING: Swing must be run with a " +
                         "1.1.2 or higher version VM!!!");
      System.exit(-1);
    }
  }

}



