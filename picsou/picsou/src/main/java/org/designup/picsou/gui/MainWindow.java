package org.designup.picsou.gui;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PicsouWindowsLookAndFeel;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.ServerAccessDecorator;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.ConnectionRetryServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.http.HttpsClientTransport;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.User;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainWindow implements MainPanel.WindowManager {
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

  // Il faut etre sur qu'on ne fera plus de modif dans le repository en dehors du thread de dispath swing
  // sinon le thread de login fait un invokeLater mais comme le main est concourrent avec le thread de dispatch
  // on a des concurrent modification
  private boolean initDone = false;

  static {
    try {
      if (GuiUtils.isMacOSX()) {
        UIManager.setLookAndFeel(new PicsouMacLookAndFeel());
      }
      else {
        Options.setUseSystemFonts(true);
        Options.setUseNarrowButtons(false);

        PicsouWindowsLookAndFeel.set3DEnabled(true);
        PicsouWindowsLookAndFeel.setHighContrastFocusColorsEnabled(false);
        PicsouWindowsLookAndFeel.setSelectTextOnKeyboardFocusGained(false);

        UIManager.put("FileChooser.useSystemIcons", Boolean.TRUE);
        UIManager.setLookAndFeel(new PicsouWindowsLookAndFeel());
      }
      JDialog.setDefaultLookAndFeelDecorated(false);
      ToolTipManager.sharedInstance().setInitialDelay(500);
      ToolTipManager.sharedInstance().setDismissDelay(100000);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public MainWindow(PicsouApplication picsouApplication, String serverAddress,
                    String prevaylerPath, boolean dataInMemory, Directory directory) throws Exception {
    this.picsouApplication = picsouApplication;
    this.serverAddress = serverAddress;
    this.prevaylerPath = prevaylerPath;
    this.dataInMemory = dataInMemory;
    this.directory = directory;
    frame = new PicsouFrame(Lang.get("application"));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageIcon icon = Gui.IMAGE_LOCATOR.get("app_icon_128.png");
    frame.setIconImage(icon.getImage());
    ServerAccess.LocalInfo info = initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    ConfigService configService = directory.get(ConfigService.class);
    if (info != null) {
      registered = configService.update(info.getRepoId(), info.getCount(), info.getMail(),
                                        info.getSignature(), info.getActivationCode());
    }
    else {
      configService.update(null, 0, null, null, null);
    }
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

    setPanel(loginPanel.preparePanelForShow());
    frame.setSize(Gui.getWindowSize(1100, 800));
    GuiUtils.showCentered(frame);
    LicenseCheckerThread.launch(directory, picsouInit.getRepository());
    synchronized (this){
      initDone = true;
      notify();
    }
  }

  public void loggin(String user, char[] password, boolean createUser, boolean useDemoAccount) {
    LoginFunctor functor = new LoginFunctor(loginPanel, user, password, createUser, useDemoAccount);
    Thread thread = new Thread(functor);
    thread.setDaemon(true);
    thread.start();
  }

  private ServerAccess.LocalInfo initServerAccess(String remoteAdress, String prevaylerPath, boolean dataInMemory) {
    if (!remoteAdress.startsWith("http")) {
      if (serverDirectory != null) {
        serverDirectory.close();
      }
      serverDirectory = new ServerDirectory(prevaylerPath, dataInMemory);
      ServerAccess serverAccess =
        new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                             directory);
      this.serverAccess.setServerAccess(serverAccess);
    }
    else {
      ServerAccess serverAccess = new ConnectionRetryServerAccess(
        new EncrypterToTransportServerAccess(new HttpsClientTransport(remoteAdress), directory));
      this.serverAccess.setServerAccess(serverAccess);
    }
    return serverAccess.connect();
  }

  private void initDemoServerAccess() {
    InputStream stream = this.getClass().getResourceAsStream("/demo/demo.snapshot");
    if (serverDirectory != null) {
      serverDirectory.close();
    }
    serverDirectory = new ServerDirectory(stream);
    ServerAccess serverAccess = new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                                                     directory);

    this.serverAccess.setServerAccess(serverAccess);
    this.serverAccess.connect();
  }

  public void loggout() {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    frame.setJMenuBar(null);
    setPanel(loginPanel.preparePanelForShow());
    directory.get(UndoRedoService.class).reset();
  }

  public void logOutAndDeleteUser(String name, char[] password) {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    serverAccess.deleteUser(name, password);
    loggout();
  }

  private class LoginFunctor implements Runnable {
    private LoginPanel loginPanel;
    private String user;
    private char[] password;
    private boolean createUser;
    private boolean useDemoAccount;

    public LoginFunctor(LoginPanel loginPanel, String user, char[] password, boolean createUser, boolean useDemoAccount) {
      this.loginPanel = loginPanel;
      this.user = user;
      this.password = password;
      this.createUser = createUser;
      this.useDemoAccount = useDemoAccount;
    }

    public void run() {
      try {
        if (useDemoAccount) {
          initDemoServerAccess();
          serverAccess.createUser("anonymous", "password".toCharArray());
        }
        else {
          if (createUser) {
            serverAccess.createUser(user, password);
          }
          else {
            serverAccess.initConnection(user, password, false);
          }
        }

        final PicsouInit.PreLoadData preLoadData = picsouInit.loadUserData(user, useDemoAccount, registered);

        synchronized (MainWindow.this){
          while (!MainWindow.this.initDone){
            MainWindow.this.wait(5000);
          }
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            preLoadData.load();
            mainPanel.show();
            frame.setFocusTraversalPolicy(null);
          }
        });
      }
      catch (UserAlreadyExists e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayErrorMessage("login.user.exists");
          }
        });
      }
      catch (BadPassword e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (UserNotRegistered e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (final PasswordBasedEncryptor.EncryptFail e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayBadPasswordMessage("login.password.error", e.getMessage());
          }
        });
      }
      catch (final InvalidData e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayErrorText(e.getMessage());
          }
        });
        e.printStackTrace();
      }
      catch (final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            loginPanel.displayErrorMessage("login.server.connection.failure");
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
            loginPanel.setComponentsEnabled(true);
            loginPanel.stopProgressBar();
          }
        });
      }
    }
  }


  private static class ShutDownThread extends Thread {
    private ServerAccess serverAccess;
    private ServerDirectory serverDirectory;

    public ShutDownThread(ServerAccess serverAccess, ServerDirectory serverDirectory) {
      this.serverAccess = serverAccess;
      this.serverDirectory = serverDirectory;
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
  
  private static class LicenseCheckerThread extends Thread {
    private Directory directory;
    private GlobRepository repository;

    public static void launch(Directory directory, GlobRepository repository) {
      LicenseCheckerThread thread = new LicenseCheckerThread(directory, repository);
      thread.setDaemon(true);
      thread.start();
    }

    private LicenseCheckerThread(Directory directory, GlobRepository repository) {
      this.directory = directory;
      this.repository = repository;
    }

    public void run() {
      ConfigService.waitEndOfConfigRequest(directory);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          repository.startChangeSet();
          try {
            ConfigService.check(directory, repository);
            repository.update(User.KEY, User.CONNECTED, true);
            try {
              Thread.sleep(100);
            }
            catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          finally {
            repository.completeChangeSet();
          }
        }
      });
    }
  }

}
