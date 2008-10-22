package org.designup.picsou.gui.startup;

import org.globsframework.utils.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SingleApplicationInstanceListener {
  public static final String SINGLE_INSTANCE_DISABLED = "SINGLE_INSTANCE_DISABLED";
  public static final int[] PORTS = new int[]{5454, 3474, 14457, 9381};
  public static int ACCEPT_TIMEOUT = 200000;
  public static int REMOTE_APPLICATION_DETECTION_TIMEOUT = 2000;

  private static final String USER_MESSAGE_KEY = "user";
  private static final String FILES_MESSAGE_KEY = "file";
  private static final String SHOW_MESSAGE_KEY = "show";
  private static final String RESPONSE_OK = "OK";
  private static final String RESPONSE_FAIL = "FAIL";
  private ThreadReader threadReader;
  private OpenRequestManager openRequestManager;

  public enum ReturnState {
    EXIT,
    CONTINUE
  }

  public SingleApplicationInstanceListener(OpenRequestManager openRequestManager) {
    this.openRequestManager = openRequestManager;
  }

  public ReturnState findRemoteOrListen() {
    if ("true".equalsIgnoreCase(System.getProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED))) {
      return ReturnState.CONTINUE;
    }
    List<ServerSocket> serverSockets = new ArrayList<ServerSocket>();
    for (int port : PORTS) {
      try {
        serverSockets.add(new ServerSocket(port, 4, Inet4Address.getByAddress(new byte[]{127, 0, 0, 1})));
      }
      catch (BindException bindException) {
        if (canConnectToSameApplication(serverSockets, port)) {
          return ReturnState.EXIT;
        }
      }
      catch (SocketException bindException) {
        if (canConnectToSameApplication(serverSockets, port)) {
          return ReturnState.EXIT;
        }
      }
      catch (Exception e) {
        // Port is empty
      }
    }

    if (serverSockets.isEmpty()) {
      return ReturnState.CONTINUE;
    }
    ServerSocket serverSocket = serverSockets.remove(0);
    threadReader = new ThreadReader(serverSocket);
    threadReader.start();
    for (ServerSocket socket : serverSockets) {
      try {
        socket.close();
      }
      catch (IOException e) {
        // Ignore
      }
    }
    return ReturnState.CONTINUE;
  }

  private boolean canConnectToSameApplication(List<ServerSocket> serverSockets, int port) {
    try {
      SendToRemoteCalback sendToRemoteCalback = new SendToRemoteCalback(port);
      boolean sameApplication = sendToRemoteCalback.checkSameApplication();
      if (sameApplication) {
        openRequestManager.pushCallback(sendToRemoteCalback);
        for (ServerSocket socket : serverSockets) {
          socket.close();
        }
        return true;
      }
    }
    catch (Exception e) {
      Log.write("socket", e);
    }
    return false;
  }


  private void readFromSocket(Socket socket) throws IOException {
    InetAddress remoteAddress = socket.getInetAddress();
    if (!remoteAddress.isLoopbackAddress()) {
      socket.close();
      return;
    }
    socket.setSoTimeout(ACCEPT_TIMEOUT);
    ObjectInputStream inputStream = null;
    ObjectOutputStream outputStream = null;
    try {
      inputStream = new ObjectInputStream(socket.getInputStream());
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      outputStream.flush();
      try {
        String firstMessage = (String)inputStream.readObject();
        if (USER_MESSAGE_KEY.equals(firstMessage)) {
          String identity = (String)inputStream.readObject();
          if (System.getProperty("user.name").equals(identity)) {
            outputStream.writeObject(RESPONSE_OK);
          }
          else {
            outputStream.writeObject(RESPONSE_FAIL);
            return;
          }
        }
        outputStream.flush();
        String message = (String)inputStream.readObject();
        if (FILES_MESSAGE_KEY.equals(message)) {
          List<File> files = readFileName(inputStream);
          outputStream.writeObject(RESPONSE_OK);
          openRequestManager.openFiles(files);
        }
        else if (SHOW_MESSAGE_KEY.equals(message)) {
          readFileName(inputStream);
          outputStream.writeObject(RESPONSE_OK);
          bringToFront();
        }
        else {
          outputStream.writeObject(RESPONSE_FAIL);
        }
        outputStream.flush();
      }
      catch (Exception e) {
        outputStream.writeObject(RESPONSE_FAIL);
        Log.write("socket", e);
      }
    }
    finally {
      try {
        socket.close();
        if (outputStream != null) {
          outputStream.close();
        }
        if (inputStream != null) {
          inputStream.close();
        }
      }
      catch (IOException e) {
        Log.write("Close error");
      }
    }
  }

  private void bringToFront() {
  }

  public void shutdown() {
    if (threadReader != null) {
      try {
        threadReader.requestShutdown();
        threadReader.interrupt();
        threadReader.join();
        threadReader = null;
      }
      catch (Exception e) {
      }
    }
  }

  private List<File> readFileName(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
    List<File> filesToOpen = new ArrayList<File>();
    int fileCount = objectInputStream.readInt();
    for (int i = 0; i < fileCount; i++) {
      String fileName = (String)objectInputStream.readObject();
      File file = new File(fileName);
      if (file.exists() && file.isFile()) {
        tryReadToEnsureWeHaveTheRightToReadThisFile(file);
        filesToOpen.add(file);
      }
    }
    return filesToOpen;
  }

  private void tryReadToEnsureWeHaveTheRightToReadThisFile(File file) throws IOException {
    FileReader fileReader = new FileReader(file);
    fileReader.read();
  }


  static class SendToRemoteCalback implements OpenRequestManager.Callback {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private int port;

    SendToRemoteCalback(int port) throws IOException, ClassNotFoundException {
      this.port = port;
    }

    public boolean checkSameApplication() throws IOException, ClassNotFoundException {
      socket = new Socket((String)null, port);
      socket.setSoTimeout(REMOTE_APPLICATION_DETECTION_TIMEOUT);
      output = new ObjectOutputStream(socket.getOutputStream());
      output.flush();
      input = new ObjectInputStream(socket.getInputStream());
      output.writeObject(USER_MESSAGE_KEY);
      output.writeObject(System.getProperty("user.name"));
      output.flush();
      String answer = (String)input.readObject();
      return socket != null && RESPONSE_OK.equals(answer);
    }

    public boolean accept() {
      return true;
    }

    public void openFiles(List<File> file) {
      trySendToRemote(file);
    }

    public void bringToFront() {
    }

    private boolean trySendToRemote(List<File> files) {
      File tempFile = null;
      try {
        if (files.size() == 0) {
          tempFile = File.createTempFile("empty", "txt");
          output.writeObject(SHOW_MESSAGE_KEY);
          output.writeObject(tempFile.getAbsolutePath());
        }
        else {
          output.writeObject(FILES_MESSAGE_KEY);
          output.writeInt(files.size());
          for (File file : files) {
            output.writeObject(file.getAbsolutePath());
          }
        }
        output.flush();
        String result = (String)input.readObject();
        return RESPONSE_OK.equals(result);
      }
      catch (Exception e) {
        Log.write("socket", e);
        return false;
      }
      finally {
        try {
          if (input != null) {
            input.close();
          }
          if (output != null) {
            output.close();
          }
          if (socket != null) {
            socket.close();
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
  }

  public class ThreadReader extends Thread {
    private final ServerSocket serverSocket;
    private boolean shutdownRequested;

    public ThreadReader(ServerSocket serverSocket) {
      this.serverSocket = serverSocket;
      setDaemon(true);
    }

    public void run() {
      long lastFail = 0;
      int countFailed = 0;
      while (countFailed < 10) {
        try {
          synchronized (this) {
            if (shutdownRequested) {
              return;
            }
          }
          final Socket socket = serverSocket.accept();
          Thread thread = new Thread() {
            public void run() {
              try {
                readFromSocket(socket);
              }
              catch (IOException e) {
              }
            }
          };
          thread.setDaemon(true);
          thread.start();
        }
        catch (IOException e) {
          synchronized (this) {
            if (shutdownRequested) {
              return;
            }
          }
          Log.write("accept failed");
          if (System.currentTimeMillis() - lastFail < 200) {
            Log.write("wait");
            countFailed++;
            try {
              Thread.sleep(100);
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
      synchronized (this) {
        shutdownRequested = true;
        serverSocket.close();
      }
    }
  }
}
