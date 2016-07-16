package com.designup.siteweaver;

import com.designup.siteweaver.server.PageHandler;
import com.designup.siteweaver.server.upload.FileAccess;
import com.designup.siteweaver.server.upload.FtpFileAccess;
import com.designup.siteweaver.server.upload.NoOpFileAccess;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class SiteWeaver {

  public static void main(String args[]) throws Exception {

    checkUsage(args);

    Server server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setHost("localhost");
    connector.setPort(8080);
    connector.setSoLingerTime(1000 * 60 * 60);
    server.addConnector(connector);

    server.setHandler(new PageHandler(new File(args[0]), getFileAccess(args)));
    server.start();
    System.out.println("Listening on " + "http://localhost:" + connector.getPort() + "  --  " + "http://" + getHostAddress() + ":" + connector.getPort() + "\n");
    if (args.length > 1) {
      System.out.println("Available commands:\n" +
                         "  /!dump\n" +
                         "  /!diff\n" +
                         "  /!publish\n");
    }
    server.join();

    getFileAccess(args).dispose();
  }

  public static String getHostAddress() throws Exception {
    Enumeration e = NetworkInterface.getNetworkInterfaces();
    while (e.hasMoreElements()) {
      NetworkInterface n = (NetworkInterface) e.nextElement();
      Enumeration ee = n.getInetAddresses();
      while (ee.hasMoreElements()) {
        InetAddress i = (InetAddress) ee.nextElement();
        String address = i.getHostAddress();
        if (address.startsWith("192.")) {
          return address;
        }
      }
    }
    return "localhost";
  }

  private static FileAccess getFileAccess(String[] args) throws IOException {
    if (args.length > 1) {
      return new FtpFileAccess(args[1], normalizePath(args[2]), args[3], args[4]);
    }
    return new NoOpFileAccess();
  }

  private static String normalizePath(String arg) {
    if ("/".equals(arg.trim())) {
      return "";
    }
    return arg;
  }

  private static void checkUsage(String[] args) {
    if (args.length != 1 && args.length < 5) {
      System.out.println("Usage: java SiteWeaver <configFile> <ftp_host> <ftp_dir> <ftp_user> <ftp_pwd>");
      System.exit(-1);
    }
  }
}



