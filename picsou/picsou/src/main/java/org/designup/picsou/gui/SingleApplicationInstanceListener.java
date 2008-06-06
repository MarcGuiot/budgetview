package org.designup.picsou.gui;

import org.crossbowlabs.globs.utils.Log;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.*;

public class SingleApplicationInstanceListener {
  public static final int[] PORT = new int[]{5454, 3474, 14457, 9381};
  public static ThreadReader thread;
  public static final String SINGLE_INSTANCE_DISABLED = "SINGLE_INSTANCE_DISABLED";

  public static void listenForFile(final MainPanel mainPanel) throws IOException {
    if ("true".equals(System.getProperty(SINGLE_INSTANCE_DISABLED))) {
      return;
    }

    ServerSocket serverSocket = null;
    int port = 0;
    for (int i = 0; i < PORT.length; i++) {
      port = PORT[i];
      try {
        serverSocket = null;
        serverSocket = new ServerSocket(port);
        break;
      }
      catch (IOException e) {
      }
    }
    if (serverSocket == null) {
      Log.write("No free port found");
      return;
    }
    Log.write("listen " + port);

    thread = new ThreadReader(serverSocket, mainPanel);
    thread.setDaemon(true);
    thread.start();
  }

  public static void readFromSocket(Socket socket, MainPanel mainPanel) throws IOException {
    InetAddress remoteAddress = socket.getInetAddress();
    if (!remoteAddress.isLoopbackAddress()) {
      socket.close();
      return;
    }
    socket.setSoTimeout(1000);
    ObjectInputStream objectInputStream = null;
    ObjectOutputStream objectOutputStream = null;
    try {
      objectInputStream = new ObjectInputStream(socket.getInputStream());
      objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      objectOutputStream.flush();
      try {
        String message = (String) objectInputStream.readObject();
        if ("file".equals(message)) {
          File file = readFileName(objectInputStream);
          objectOutputStream.writeObject("OK");
          objectOutputStream.flush();
          mainPanel.openFile(file, false);
          return;
        }
        if ("show".equals(message)) {
          readFileName(objectInputStream);
          objectOutputStream.writeObject("OK");
          objectOutputStream.flush();
          mainPanel.openInFront();
          return;
        }
        objectOutputStream.writeObject("FAIL");
      }
      catch (Exception e) {
        objectOutputStream.writeObject("FAIL");
      }
      objectOutputStream.flush();
    }
    finally {
      try {
        socket.close();
        if (objectOutputStream != null) {
          objectOutputStream.close();
        }
        if (objectInputStream != null) {
          objectInputStream.close();
        }
      }
      catch (IOException e) {
        Log.write("Close error");
      }
    }
  }

  public static void shutdown() throws Exception {
    if (SingleApplicationInstanceListener.thread != null) {
      SingleApplicationInstanceListener.thread.requestShutdown();
      SingleApplicationInstanceListener.thread.join();
      SingleApplicationInstanceListener.thread = null;
    }
  }

  public static File readFileName(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
    String fileName = (String) objectInputStream.readObject();
    File file = new File(fileName);
    if (file.exists() && file.isFile()) {
      tryReadToEnsureWeHaveTheRightToReadThisFile(file);
    }
    return file;
  }

  public static void tryReadToEnsureWeHaveTheRightToReadThisFile(File file) throws IOException {
    FileReader fileReader = new FileReader(file);
    fileReader.read();
  }

  public static boolean sendAlreadyOpen(String[] args) throws IOException, ClassNotFoundException {
    if ("true".equals(System.getProperty(SINGLE_INSTANCE_DISABLED))) {
      return false;
    }
    Socket socket = null;
    ObjectOutputStream objectOutputStream = null;
    ObjectInputStream objectInputStream = null;
    File tempFile = null;
    for (int port : PORT) {
      try {
        socket = new Socket((String) null, port);
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.flush();
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        if (args.length == 0) {
          tempFile = File.createTempFile("empty", "txt");
          objectOutputStream.writeObject("show");
          objectOutputStream.writeObject(tempFile.getAbsolutePath());
        }
        else {
          objectOutputStream.writeObject("file");
          objectOutputStream.writeObject(args[0]);
        }
        objectOutputStream.flush();
        String result = (String) objectInputStream.readObject();
        if ("OK".equals(result)) {
          return true;
        }
      }
      catch (Exception e) {
      }
      finally {
        try {
          if (socket != null) {
            socket.close();
          }
          if (objectInputStream != null) {
            objectInputStream.close();
          }
          if (objectOutputStream != null) {
            objectOutputStream.close();
          }
          if (tempFile != null) {
            tempFile.delete();
          }
        }
        catch (Exception e) {
          Log.write("close error");
        }
      }
    }
    return false;
  }

  public static class ThreadReader extends Thread {
    private final ServerSocket serverSocket;
    private final MainPanel mainPanel;
    private boolean shutdownRequested;

    public ThreadReader(ServerSocket serverSocket, MainPanel mainPanel) {
      this.serverSocket = serverSocket;
      this.mainPanel = mainPanel;
    }

    public void run() {
      long lastFail = 0;
      int countFailed = 0;
      while (countFailed < 10) {
        try {
          if (shutdownRequested) {
            return;
          }
          Socket socket = serverSocket.accept();
          readFromSocket(socket, mainPanel);
        }
        catch (IOException e) {
          Log.write("accept failed");
          if (System.currentTimeMillis() - lastFail < 200) {
            Log.write("wait");
            countFailed++;
            try {
              Thread.sleep(200);
            }
            catch (InterruptedException e1) {
            }
          }
          else {
            countFailed = 0;
          }
          lastFail = System.currentTimeMillis();
        }
      }
    }

    public void requestShutdown() throws IOException {
      shutdownRequested = true;
      try {
        serverSocket.close();
      }
      finally {
        shutdownRequested = false;
      }
    }
  }
}
