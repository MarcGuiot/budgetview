package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.ServerAccessDecorator;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.RemoteException;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.ConnectionRetryServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.license.LicenseCheckerThread;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.DataCheckerAction;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public class MainWindow implements WindowManager {
  private PicsouFrame frame;
  private WindowAdapter windowOpenListener;
  private PicsouApplication picsouApplication;
  private String serverAddress;
  private String prevaylerPath;
  private boolean dataInMemory;
  private Directory directory;
  private LoginPanel loginPanel;
  private ServerDirectory serverDirectory;
  private ServerAccessDecorator serverAccess = new ServerAccessDecorator(null);
  private PicsouInit picsouInit;
  private MainPanel mainPanel;
  private boolean registered = false;
  static private ShutDownThread thread;

  // Il faut etre sur qu'on ne fera plus de modif dans le repository en dehors du thread de dispath swing
  // sinon le thread de login fait un invokeLater mais comme le main est concourrent avec le thread de dispatch
  // on a des concurrent modification
  private boolean initDone = false;
  private List<ServerAccess.UserInfo> localUsers;

  public MainWindow(PicsouApplication picsouApplication, String serverAddress,
                    String prevaylerPath, boolean dataInMemory, Directory directory) throws Exception {
    this.picsouApplication = picsouApplication;
    this.serverAddress = serverAddress;
    this.prevaylerPath = prevaylerPath;
    this.dataInMemory = dataInMemory;
    this.directory = directory;

    if (thread == null) {
      thread = new ShutDownThread(serverAccess);
      Runtime.getRuntime().addShutdownHook(thread);
    }

    this.frame = new PicsouFrame(Lang.get("application"));

    ConfigService configService = directory.get(ConfigService.class);
    ServerAccess.LocalInfo info = initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    if (info != null) {
      registered = configService.update(info.getRepoId(), info.getCount(), info.getMail(),
                                        info.getSignature(), info.getActivationCode());
    }
    else {
      configService.update(null, 0, null, null, null);
    }

    MRJAdapter.addAboutListener(new AboutAction(directory));
  }

  public void setPanel(JPanel panel) {
    frame.setContentPane(panel);
    frame.validate();
  }

  public PicsouFrame getFrame() {
    return frame;
  }

  public void show() {
    loginPanel = new LoginPanel(this, directory);
    picsouInit = PicsouInit.init(serverAccess, directory, registered);
    mainPanel = MainPanel.init(picsouInit.getRepository(), picsouInit.getDirectory(), this);

    windowOpenListener = new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
        frame.removeWindowListener(windowOpenListener);
        windowOpenListener = null;
      }
    };
    frame.addWindowListener(windowOpenListener);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        picsouApplication.shutdown();
      }
    });

    boolean autoLogin = false;
    String user = null;
    for (ServerAccess.UserInfo userInfo : localUsers) {
      if (userInfo.autologin) {
        autoLogin = true;
        user = userInfo.name;
      }
    }
    if (autoLogin && localUsers.size() == 1) {
      autoLogin(user);
    }
    else {
      setPanel(loginPanel.preparePanelForShow(localUsers));
    }
    GuiUtils.setSizeWithinScreen(frame, 1100, 800);
    GuiUtils.showCentered(frame);
    LicenseCheckerThread.launch(directory, picsouInit.getRepository());
    synchronized (this) {
      initDone = true;
      notify();
    }
  }

  public void autoLogin(String user) {
    LoginFunctor functor = new LoginFunctor(new AutoLoginFeedback(), user, user.toCharArray(), false, false, true);
    Thread thread = new Thread(functor);
    thread.setDaemon(true);
    thread.start();
  }

  public void login(String user, char[] password, boolean createUser, boolean useDemoAccount, boolean autoLog) {
    LoginFunctor functor = new LoginFunctor(new OnPanelFeedback(), user, password, createUser, useDemoAccount, autoLog);
    Thread thread = new Thread(functor);
    thread.setDaemon(true);
    thread.start();
  }

  public void logout() {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    frame.setJMenuBar(null);
    setPanel(loginPanel.preparePanelForShow(localUsers));
    directory.get(UndoRedoService.class).reset();
  }

  public void logOutAndDeleteUser(String name, char[] password) {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    try {
      serverAccess.deleteUser(name, password);
    }
    catch (RemoteException e) {
      MessageDialog.createMessageDialog("delete.user.fail.title", "delete.user.fail.content", frame, directory)
        .show();
    }
    logout();
  }

  private ServerAccess.LocalInfo initServerAccess(String remoteAdress, String prevaylerPath, boolean dataInMemory) {
    if (!remoteAdress.startsWith("http")) {
      this.serverAccess.takeSnapshot();
      this.serverAccess.disconnect();
      if (serverDirectory != null) {
        serverDirectory.close();
      }
      serverDirectory = new ServerDirectory(prevaylerPath, dataInMemory);
      thread.serverDirectory = serverDirectory;
      ServerAccess serverAccess =
        new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                             directory);
      this.serverAccess.setServerAccess(serverAccess);
    }
    else {
      this.serverAccess.takeSnapshot();
      this.serverAccess.disconnect();
      ServerAccess serverAccess = new ConnectionRetryServerAccess(
        new EncrypterToTransportServerAccess(new HttpsClientTransport(remoteAdress), directory));
      this.serverAccess.setServerAccess(serverAccess);
    }
    ServerAccess.LocalInfo info = serverAccess.connect();
    localUsers = serverAccess.getLocalUsers();
    return info;
  }

  private void initDemoServerAccess() {
    InputStream stream = this.getClass().getResourceAsStream("/demo/demo.snapshot");
    if (serverDirectory != null) {
      serverDirectory.close();
    }
    serverDirectory = new ServerDirectory(stream);
    thread.serverDirectory = serverDirectory;
    ServerAccess serverAccess = new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                                                     directory);

    this.serverAccess.setServerAccess(serverAccess);
    localUsers = Collections.emptyList();
    this.serverAccess.connect();
  }

  interface FeedbackLoadingData {

    void displayErrorMessage(String message);

    void complete();

    void displayBadPasswordMessage(String message, String arg);

    void displayErrorText(String message);
  }

  class OnPanelFeedback implements FeedbackLoadingData {

    public void displayErrorMessage(String key) {
      loginPanel.displayErrorMessage(key);
    }

    public void complete() {
      loginPanel.stopProgressBar();
    }

    public void displayBadPasswordMessage(String key, String complement) {
      loginPanel.displayBadPasswordMessage(key, complement);
    }

    public void displayErrorText(String message) {
      loginPanel.displayErrorText(message);
    }
  }

  class AutoLoginFeedback extends OnPanelFeedback {
    public void displayErrorMessage(String key) {
      setPanel(loginPanel.preparePanelForShow(localUsers));
      super.displayErrorMessage(key);
    }

    public void complete() {
    }

    public void displayBadPasswordMessage(String key, String complement) {
      setPanel(loginPanel.preparePanelForShow(localUsers));
      super.displayBadPasswordMessage(key, complement);
    }

    public void displayErrorText(String message) {
      setPanel(loginPanel.preparePanelForShow(localUsers));
      super.displayErrorText(message);
    }
  }

  private class LoginFunctor implements Runnable {
    private String user;
    private char[] password;
    private boolean createUser;
    private boolean useDemoAccount;
    private boolean autoLog;
    private FeedbackLoadingData feedbackLoadingData;

    public LoginFunctor(FeedbackLoadingData feedbackLoadingData, String user, char[] password,
                        boolean createUser, boolean useDemoAccount, boolean autoLog) {
      this.feedbackLoadingData = feedbackLoadingData;
      this.user = user;
      this.password = password;
      this.createUser = createUser;
      this.useDemoAccount = useDemoAccount;
      this.autoLog = autoLog;
    }

    public void run() {
      try {
        if (useDemoAccount) {
          initDemoServerAccess();
          serverAccess.createUser("anonymous", "password".toCharArray(), false);
        }
        else {
          if (createUser) {
            serverAccess.createUser(user, password, autoLog);
          }
          else {
            serverAccess.initConnection(user, password, false);
          }
        }

        final PicsouInit.PreLoadData preLoadData = picsouInit.loadUserData(user, useDemoAccount, autoLog);

        synchronized (MainWindow.this) {
          while (!MainWindow.this.initDone) {
            MainWindow.this.wait(5000);
          }
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            try {
              preLoadData.load();
            }
            catch (Exception e) {
              DataCheckerAction action = new DataCheckerAction(picsouInit.getRepository(), picsouInit.getDirectory());
              action.actionPerformed(null);
            }
            mainPanel.show();
            frame.setFocusTraversalPolicy(null);
          }
        });
      }
      catch (UserAlreadyExists e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayErrorMessage("login.user.exists");
          }
        });
      }
      catch (BadPassword e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (UserNotRegistered e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (final PasswordBasedEncryptor.EncryptFail e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayBadPasswordMessage("login.password.error", e.getMessage());
          }
        });
      }
      catch (final InvalidState e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayErrorText(e.getMessage());
          }
        });
        e.printStackTrace();
      }
      catch (final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.displayErrorMessage("login.server.connection.failure");
            final StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                JTextArea textArea = new JTextArea(stringWriter.toString());
                GuiUtils.show(textArea);
              }
            });
          }
        });
      }
      finally {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            feedbackLoadingData.complete();
          }
        });
      }
    }
  }

  private static class ShutDownThread extends Thread {
    private ServerAccess serverAccess;
    private ServerDirectory serverDirectory = null;

    public ShutDownThread(ServerAccess serverAccess) {
      this.serverAccess = serverAccess;
    }

    public void run() {
      try {
        if (serverAccess != null) {
          serverAccess.takeSnapshot();
          serverAccess.disconnect();
          serverAccess = null;
        }
        if (serverDirectory != null) {
          serverDirectory.close();
          serverDirectory = null;
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }


}
