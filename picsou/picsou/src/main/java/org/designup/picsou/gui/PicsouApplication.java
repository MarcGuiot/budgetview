package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.Files;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouDescriptionService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PicsouApplication {

  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = "picsou.prevayler.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = "picsou.server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = "picsou.prevayler.delete";
  private static String DEFAULT_ADDRESS = "https://startupxp.dynalias.org";

  public static File[] initialFile;
  private static final int[] PORT = new int[]{5454, 3474, 14457, 9381};

  protected static ThreadReader thread;
  public static final String SINGLE_INSTANCE_DISABLED = "SINGLE_INSTANCE_DISABLED";

  static {
    MRJAdapter.addOpenDocumentListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ApplicationEvent event = (ApplicationEvent) e;
        initialFile = new File[]{event.getFile()};
      }
    });
  }

  public static void main(String... args) throws Exception {
    if (System.getProperty("mrj.version") != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    if (sendAlreadyOpen(args)) {
      return;
    }
    List<File> fileToOpen = new ArrayList<File>();
    for (String arg : args) {
      File file = new File(arg);
      if (file.exists()) {
        fileToOpen.add(file);
      }
    }
    if (!fileToOpen.isEmpty()) {
      initialFile = fileToOpen.toArray(new File[fileToOpen.size()]);
    }

    clearRepositoryIfNeeded();

    Directory directory = createDirectory();

    final MainWindow window = new MainWindow();
    final LoginPanel loginPanel = new LoginPanel(getServerAddress(), getLocalPrevaylerPath(), window, directory);
    window.setPanel(loginPanel.getJPanel());
    window.getFrame().addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
      }
    });
    window.show();
  }

  private static String getServerAddress() {
    return getSystemValue(DEFAULT_ADDRESS_PROPERTY, DEFAULT_ADDRESS);
  }

  public static String getLocalPrevaylerPath() {
    return getSystemValue(LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.picsou/data");
  }

  public static void clearRepositoryIfNeeded() {
    if ("true".equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      Files.deleteSubtree(new File(getLocalPrevaylerPath()));
    }
  }

  public static void shutdown() throws Exception {
    if (thread != null) {
      thread.requestShutdown();
      thread.join();
      thread = null;
    }
  }

  private static String getSystemValue(String propertyName, String defaultPropertyValue) {
    String value = null;
    try {
      value = System.getProperty(propertyName);
    }
    catch (Throwable e) {
    }
    if (value == null) {
      return defaultPropertyValue;
    }
    return value;
  }

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

  private static void readFromSocket(Socket socket, MainPanel mainPanel) throws IOException {
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

  private static File readFileName(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
    String fileName = (String) objectInputStream.readObject();
    File file = new File(fileName);
    if (file.exists() && file.isFile()) {
      tryReadToEnsureWeHaveTheRightToReadThisFile(file);
    }
    return file;
  }

  private static void tryReadToEnsureWeHaveTheRightToReadThisFile(File file) throws IOException {
    FileReader fileReader = new FileReader(file);
    fileReader.read();
  }

  private static boolean sendAlreadyOpen(String[] args) throws IOException, ClassNotFoundException {
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

  private static class ThreadReader extends Thread {
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

  public static Directory createDirectory() throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(GlobModel.class, PicsouGuiModel.get());
    directory.add(SelectionService.class, new SelectionService());
    PicsouColors.registerColorService(directory);
    directory.add(IconLocator.class, Gui.ICON_LOCATOR);

    UIManager.put("ColorService", directory.get(ColorService.class));

    return directory;
  }

}
