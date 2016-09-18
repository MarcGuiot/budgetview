package com.budgetview.server.cloud.functests.checkers;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class WebServerTestUtils {

  private static final int MIN_PORT_NUMBER = 1100;
  private static final int MAX_PORT_NUMBER = 49151;

  public static void waitForPorts(int... ports) throws InterruptedException {
    for (int port : ports) {
      waitForPort(port);
    }
    Thread.sleep(800);
  }

  public static void waitForPort(int port) {
    while (!available(port)) {
      try {
        Thread.sleep(200);
      }
      catch (InterruptedException e) {
      }
    }
  }

  public static boolean available(int port) {
    if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
      throw new IllegalArgumentException("Invalid start port: " + port);
    }

    ServerSocket ss = null;
    DatagramSocket ds = null;
    try {
      ss = new ServerSocket(port);
      ss.setReuseAddress(true);
      ds = new DatagramSocket(port);
      ds.setReuseAddress(true);
      return true;
    }
    catch (IOException e) {
    }
    finally {
      if (ds != null) {
        ds.close();
      }

      if (ss != null) {
        try {
          ss.close();
        }
        catch (IOException e) {
                /* should not be thrown */
        }
      }
    }

    return false;
  }

}
