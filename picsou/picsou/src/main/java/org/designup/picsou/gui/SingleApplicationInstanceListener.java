package org.designup.picsou.gui;

import org.designup.picsou.gui.startup.OpenRequestManager;
import org.globsframework.utils.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SingleApplicationInstanceListener {
  public static final String SINGLE_INSTANCE_DISABLED = "SINGLE_INSTANCE_DISABLED";
  public static final int[] PORTS = new int[]{5454, 3474, 14457, 9381};
  private static final String USER_MESSAGE_KEY = "user";
  private static final String FILES_MESSAGE_KEY = "file";
  private static final String SHOW_MESSAGE_KEY = "show";
  private static final String RESPONSE_OK = "OK";
  private static final String RESPONSE_FAIL = "FAIL";
  private ThreadReader threadReader;
  private OpenRequestManager openRequestManager;


  enum ReturnState {
    EXIT,
    CONTINUE
  }

  SingleApplicationInstanceListener(OpenRequestManager openRequestManager) {
    this.openRequestManager = openRequestManager;
  }

  public ReturnState findRemoteOrListen() {
    if ("true".equalsIgnoreCase(System.getProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED))) {
      return ReturnState.CONTINUE;
    }
    List<ServerSocket> serverSockets = new ArrayList<ServerSocket>();
    for (int port : PORTS) {
      try {
        serverSockets.add(new ServerSocket(port));
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
    }
    return false;
  }


  private void readFromSocket(Socket socket) throws IOException {
    InetAddress remoteAddress = socket.getInetAddress();
    if (!remoteAddress.isLoopbackAddress()) {
      socket.close();
      return;
    }
    socket.setSoTimeout(2000);
    ObjectInputStream objectInputStream = null;
    ObjectOutputStream objectOutputStream = null;
    try {
      objectInputStream = new ObjectInputStream(socket.getInputStream());
      objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
      objectOutputStream.flush();
      try {
        while (true) {
          String message = (String)objectInputStream.readObject();
          if (USER_MESSAGE_KEY.equals(message)) {
            String identity = (String)objectInputStream.readObject();
            if (System.getProperty("user.name").equals(identity)) {
              objectOutputStream.writeObject(RESPONSE_OK);
              objectOutputStream.flush();
              continue;
            }
          }
          if (FILES_MESSAGE_KEY.equals(message)) {
            List<File> files = readFileName(objectInputStream);
            objectOutputStream.writeObject(RESPONSE_OK);
            objectOutputStream.flush();
            openRequestManager.openFiles(files);
            continue;
          }
          if (SHOW_MESSAGE_KEY.equals(message)) {
            readFileName(objectInputStream);
            objectOutputStream.writeObject(RESPONSE_OK);
            objectOutputStream.flush();
            bringToFront();
            continue;
          }
          break;
        }
        objectOutputStream.writeObject(RESPONSE_FAIL);
      }
      catch (Exception e) {
        objectOutputStream.writeObject(RESPONSE_FAIL);
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

  private void bringToFront() {
  }

  public void shutdown() throws Exception {
    if (threadReader != null) {
      threadReader.requestShutdown();
      threadReader.join();
      threadReader = null;
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
      socket.setSoTimeout(2000);
      output = new ObjectOutputStream(socket.getOutputStream());
      output.flush();
      input = new ObjectInputStream(socket.getInputStream());
      output.writeObject(USER_MESSAGE_KEY);
      output.writeObject(System.getProperty("user.name"));
      output.flush();
      String answer = (String)input.readObject();
      return socket != null && RESPONSE_OK.equals(answer);
    }

    public void close() throws IOException {
      socket.close();
      socket = null;
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
        return false;
      }
      finally {
        try {
          if (socket != null) {
            socket.close();
          }
          if (input != null) {
            input.close();
          }
          if (output != null) {
            output.close();
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
    }

    public void run() {
      long lastFail = 0;
      int countFailed = 0;
      while (countFailed < 10) {
        try {
          if (shutdownRequested) {
            return;
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
      serverSocket.close();
    }
  }
}
