package com.budgetview.desktop;

import com.budgetview.client.DataAccess;
import com.budgetview.client.DataAccessDecorator;
import com.budgetview.client.exceptions.BadPassword;
import com.budgetview.client.exceptions.RemoteException;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.client.exceptions.UserNotRegistered;
import com.budgetview.client.http.AutoRetryDataAccess;
import com.budgetview.client.http.EncryptToTransportDataAccess;
import com.budgetview.client.http.HttpsDataTransport;
import com.budgetview.client.local.LocalSessionDataTransport;
import com.budgetview.desktop.components.PicsouFrame;
import com.budgetview.desktop.components.dialogs.MessageAndDetailsDialog;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.layoutconfig.LayoutConfigService;
import com.budgetview.desktop.license.activation.LicenseCheckerThread;
import com.budgetview.desktop.startup.LoginPanel;
import com.budgetview.desktop.startup.SlaValidationDialog;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.desktop.utils.FrameSize;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.User;
import com.budgetview.model.UserPreferences;
import com.budgetview.session.SessionDirectory;
import com.budgetview.shared.encryption.PasswordBasedEncryptor;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import static org.globsframework.model.FieldValue.value;

public class MainWindow implements WindowManager {
  public static final String DEMO_USER_NAME = "anonymous";
  public static final String DEMO_PASSWORD = "password";
  private PicsouFrame frame;
  private WindowAdapter windowOpenListener;
  private Application application;
  private String serverAddress;
  private String prevaylerPath;
  private boolean dataInMemory;
  private Directory directory;
  private LoginPanel loginPanel;
  private SessionDirectory sessionDirectory;
  private DataAccessDecorator serverAccess = new DataAccessDecorator(null);
  private PicsouInit picsouInit;
  private MainPanel mainPanel;
  private boolean registered = false;
  static private ShutDownThread thread;

  // Il faut etre sur qu'on ne fera plus de modif dans le repository en dehors du thread de dispath swing
  // sinon le thread de login fait un invokeLater mais comme le main est concurrent avec le thread de dispatch
  // on a des concurrent modification
  private boolean initDone = false;
  private List<DataAccess.UserInfo> localUsers;
  private LicenseCheckerThread licenseCheckerThread;
  private boolean badJarVersion = false;

  public MainWindow(Application application, String serverAddress,
                    String prevaylerPath, boolean dataInMemory, Directory directory) throws Exception {
    this.application = application;
    this.serverAddress = serverAddress;
    this.prevaylerPath = prevaylerPath;
    this.dataInMemory = dataInMemory;
    this.directory = directory;

    if (thread != null) {
      Runtime.getRuntime().removeShutdownHook(thread);
    }
    thread = new ShutDownThread(serverAccess);
    Runtime.getRuntime().addShutdownHook(thread);

    this.frame = new PicsouFrame(Lang.get("application"), directory);
    this.frame.setSize(FrameSize.init(frame).targetFrameSize);

    UserConfigService userConfigService = directory.get(UserConfigService.class);
    DataAccess.LocalInfo info;
    try {
      info = initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    }
    catch (RuntimeException e) {
      StringWriter writer = new StringWriter();
      writer.append(e.getMessage());
      e.printStackTrace(new PrintWriter(writer));
      writer.append("\n\n\n");
      MessageAndDetailsDialog dialog =
        new MessageAndDetailsDialog("data.error.title", "data.error.message",
                                    writer.toString(), null, directory);
      dialog.show();
      throw e;
    }
    if (info != null) {
      if (Strings.isNotEmpty(info.getLang())) {
        Lang.setLang(info.getLang());
      }
      registered = userConfigService.retrieveUserStatus(info.getRepoId(), info.getCount(), info.getMail(),
                                                        info.getSignature(), info.getActivationCode(), serverAccess, dataInMemory);

// -- Mécanique pour éviter qu'un utilisateur pirate le système en restant toujours offline et en faisant des
// -- mises à jour manuelles - désactivé pour éviter les faux positifs
//      long downloadVersion = info.getDownloadVersion();
//      if (downloadVersion != -1) {
//        if (downloadVersion < Application.JAR_VERSION && downloadVersion > 57L) {
//          badJarVersion = true;
//          registered = false;
//        }
//      }

      if (info.getJarVersion() > Application.JAR_VERSION) {
        showDownloadJar(directory, userConfigService);
        throw new RuntimeException("End bad jar version");
      }
    }
    else {
      userConfigService.retrieveUserStatus(null, 0, null, null, null, serverAccess, dataInMemory);
    }
  }

  private static void showDownloadJar(Directory directory, final UserConfigService userConfigService) throws InvocationTargetException, InterruptedException {
    if (userConfigService.downloadStep() < 0) {
      Log.write("[Main] Invalid version, no download");
      MessageDialog.show("jar.version.title", MessageType.ERROR, null, directory,
                         "jar.version.message");
    }
    else {
      final MessageDialog messageDialog = MessageDialog.create("jar.version.title", MessageType.INFO, null, directory,
                                                               "jar.version.step", "" + userConfigService.downloadStep());
      Executors.newSingleThreadExecutor()
        .submit(new Runnable() {
          public void run() {
            try {
              int current = userConfigService.downloadStep();
              while (current >= 0) {
                if (current == 100) {
                  changeMessage(Lang.get("jar.version.step.complete"));
                  return;
                }
                else {
                  messageDialog.changeMessage(Lang.get("jar.version.step", "" + current));
                }
                Thread.sleep(200);
                int newValue = userConfigService.downloadStep();
                int count = 0;
                while (newValue != 100 && newValue == current && count < 10) {
                  Thread.sleep(500);
                  count++;
                  newValue = userConfigService.downloadStep();
                }
                if (newValue != 100 && newValue == current) {
                  messageDialog.changeMessage(Lang.get("jar.version.message"));
                  return;
                }
                current = userConfigService.downloadStep();
              }
            }
            catch (InterruptedException e) {
            }
          }

          public void changeMessage(final String message) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                messageDialog.changeMessage(message);
              }
            });
          }
        });
      messageDialog.show();
    }
  }

  public void setPanel(JPanel panel) {
    frame.setContentPane(panel);
    frame.validate();
  }

  public PicsouFrame getFrame() {
    return frame;
  }

  public void shutdown() {
    if (thread == null) {
      return;
    }
    if (licenseCheckerThread != null) {
      try {
        LicenseCheckerThread local = licenseCheckerThread;
        local.interrupt();
        local.join(2000);
        if (local.isAlive()) {
          StackTraceElement[] stackTraceElements = local.getStackTrace();
          for (StackTraceElement element : stackTraceElements) {
            System.out.println(element.toString());
          }
          Thread.dumpStack();
        }
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    thread.run();
    Runtime.getRuntime().removeShutdownHook(thread);
    thread = null;
  }

  public void show() {
    loginPanel = new LoginPanel(this, directory);
    directory.add(JFrame.class, frame);
    picsouInit = PicsouInit.init(serverAccess, directory, registered, badJarVersion);
    final GlobRepository repository = picsouInit.getRepository();
    mainPanel = MainPanel.init(repository, picsouInit.getDirectory(), this);

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
        mainPanel.updateMobile();
        application.shutdown();
      }

      public void windowClosed(WindowEvent e) {
        mainPanel.updateMobile();
        application.shutdown();
      }

      public void windowActivated(WindowEvent e) {
        Glob userPrefs = repository.find(UserPreferences.KEY);
        Glob user = repository.find(User.KEY);
        if (userPrefs != null && user != null) {
          if (serverAccess.hasChanged()) {
            final PicsouInit.PreLoadData preLoadData =
              picsouInit.loadUserData(user.get(User.NAME), user.get(User.IS_DEMO_USER),
                                      user.get(User.AUTO_LOGIN));
            preLoadData.load();
          }
          else {
            if (TimeService.reset()) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  //check a user is connected
                  Glob userPrefs = repository.find(UserPreferences.KEY);
                  if (userPrefs == null) {
                    return;
                  }
                  serverAccess.takeSnapshot();
                  repository.update(CurrentMonth.KEY,
                                    value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                                    value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
                }
              });
            }
          }
        }
      }
    });

    boolean autoLogin = false;
    String user = null;
    for (DataAccess.UserInfo userInfo : localUsers) {
      if (userInfo.autologin) {
        autoLogin = true;
        user = userInfo.name;
      }
    }
    if (autoLogin && localUsers.size() == 1) {
      autoLogin(user);
    }
    else if (localUsers.isEmpty()) {
      if (SlaValidationDialog.termsAccepted(null, directory)) {
        login(LoginPanel.AUTOLOG_USER, LoginPanel.AUTOLOG_USER.toCharArray(), true, false, true);
      }
      else {
        setPanel(loginPanel.prepareForDisplay(localUsers));
      }
    }
    else {
      setPanel(loginPanel.prepareForDisplay(localUsers));
    }

    directory.get(LayoutConfigService.class).show(frame);

    licenseCheckerThread = LicenseCheckerThread.launch(directory, repository);
    synchronized (this) {
      initDone = true;
      notify();
    }
  }

  public void autoLogin(String user) {
    LoginFunctor functor = new LoginFunctor(new AutoLoginDataLoadingDisplay(), user, user.toCharArray(), false, false, true);
    Thread thread = new Thread(functor);
    thread.setDaemon(true);
    thread.start();
  }

  public void login(String user, char[] password, boolean createUser, boolean useDemoAccount, boolean autoLog) {
    LoginFunctor functor = new LoginFunctor(new OnPanelDataLoadingDisplay(), user, password, createUser, useDemoAccount, autoLog);
    Thread thread = new Thread(functor);
    thread.setDaemon(true);
    thread.start();
  }

  public void logout() {
    mainPanel.updateMobile();
    //  TODO il faudrait pouvoir faire un reset sinon le repository n'est pas deconnecté
    // mais probleme avec les bank qui ne sont initialisé qu'une fois.
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    picsouInit.partialReset();
    frame.setJMenuBar(null);
    setPanel(loginPanel.prepareForDisplay(localUsers));
    directory.get(UndoRedoService.class).reset();
  }

  public void logOutAndDeleteUser(String name, char[] password) {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    try {
      serverAccess.deleteUser(name, password);
    }
    catch (UserNotRegistered e) {
    }
    catch (RemoteException e) {
      MessageDialog.show("delete.user.fail.title", MessageType.ERROR, frame, directory, "delete.user.fail.content");
    }
    logout();
  }

  public void logOutAndOpenDemo() {
    login(DEMO_USER_NAME, DEMO_PASSWORD.toCharArray(), false, true, false);
  }

  public void logOutAndAutoLogin() {
    initServerAccess(serverAddress, prevaylerPath, dataInMemory);
    login(LoginPanel.AUTOLOG_USER, LoginPanel.AUTOLOG_USER.toCharArray(), false, false, true);
    directory.get(UndoRedoService.class).reset();
  }

  private DataAccess.LocalInfo initServerAccess(String remoteAdress, String prevaylerPath, boolean dataInMemory) {
    if (remoteAdress.startsWith("http")) {
      this.serverAccess.takeSnapshot();
      this.serverAccess.disconnect();
      DataAccess dataAccess = new AutoRetryDataAccess(
        new EncryptToTransportDataAccess(new HttpsDataTransport(remoteAdress), directory));
      this.serverAccess.setDataAccess(dataAccess);
    }
    else {
      this.serverAccess.takeSnapshot();
      this.serverAccess.disconnect();
      if (sessionDirectory != null) {
        sessionDirectory.close();
      }
      sessionDirectory = new SessionDirectory(prevaylerPath, dataInMemory);
      thread.sessionDirectory = sessionDirectory;
      DataAccess dataAccess =
        new EncryptToTransportDataAccess(new LocalSessionDataTransport(sessionDirectory.getServiceDirectory()),
                                         directory);
      this.serverAccess.setDataAccess(dataAccess);
    }
    DataAccess.LocalInfo info = serverAccess.connect(Application.JAR_VERSION);
    localUsers = serverAccess.getLocalUsers();
    return info;
  }

  private void initDemoServerAccess() {
    serverAccess.disconnect();

    String lang = Lang.getLang();
    if (Strings.isNullOrEmpty(lang)) {
      lang = Lang.EN.getLanguage();
    }
    String name = "/demo/demo-" + lang + ".snapshot";
    InputStream stream = this.getClass().getResourceAsStream(name);
    if (stream == null) {
      throw new InvalidState("Could not find '" + name + "' in classpath");
    }
    if (sessionDirectory != null) {
      sessionDirectory.close();
    }
    sessionDirectory = new SessionDirectory(stream);
    thread.sessionDirectory = sessionDirectory;
    DataAccess dataAccess = new EncryptToTransportDataAccess(new LocalSessionDataTransport(sessionDirectory.getServiceDirectory()),
                                                             directory);

    this.serverAccess.setDataAccess(dataAccess);
    localUsers = Collections.emptyList();
    this.serverAccess.connect(Application.JAR_VERSION);
  }

  interface DataLoadingDisplay {

    void showErrorMessage(String message);

    void showPasswordErrorMessage(String message, String arg);

    void showErrorText(String message);

    void complete();
  }

  class OnPanelDataLoadingDisplay implements DataLoadingDisplay {

    public void showErrorMessage(String key) {
      loginPanel.displayErrorMessage(key);
    }

    public void showPasswordErrorMessage(String key, String complement) {
      serverAccess.disconnect();
      initServerAccess(serverAddress, prevaylerPath, dataInMemory);
      loginPanel.displayBadPasswordMessage(key, complement);
    }

    public void showErrorText(String message) {
      serverAccess.disconnect();
      initServerAccess(serverAddress, prevaylerPath, dataInMemory);
      loginPanel.displayErrorText(message);
    }

    public void complete() {
      loginPanel.stopProgressBar();
    }
  }

  class AutoLoginDataLoadingDisplay extends OnPanelDataLoadingDisplay {
    public void showErrorMessage(String key) {
      setPanel(loginPanel.prepareForDisplay(localUsers));
      super.showErrorMessage(key);
    }

    public void complete() {
    }

    public void showPasswordErrorMessage(String key, String complement) {
      setPanel(loginPanel.prepareForDisplay(localUsers));
      super.showPasswordErrorMessage(key, complement);
    }

    public void showErrorText(String message) {
      setPanel(loginPanel.prepareForDisplay(localUsers));
      super.showErrorText(message);
    }
  }

  private class LoginFunctor implements Runnable {
    private String user;
    private char[] password;
    private boolean createUser;
    private boolean useDemoAccount;
    private boolean autoLog;
    private DataLoadingDisplay dataLoadingDisplay;

    public LoginFunctor(DataLoadingDisplay dataLoadingDisplay, String user, char[] password,
                        boolean createUser, boolean useDemoAccount, boolean autoLog) {
      this.dataLoadingDisplay = dataLoadingDisplay;
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
          serverAccess.createUser(DEMO_USER_NAME, DEMO_PASSWORD.toCharArray(), false);
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
              dataLoadingDisplay.showErrorText(e.getMessage());
              logout();
              return;
            }
            mainPanel.prepareForDisplay();
            frame.setFocusTraversalPolicy(null);
          }
        });
      }
      catch (UnexpectedApplicationState e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorMessage("data.load.error.journal");
          }
        });
      }
      catch (UserAlreadyExists e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorMessage("login.user.exists");
          }
        });
      }
      catch (BadPassword e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (UserNotRegistered e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorMessage("login.invalid.credentials");
          }
        });
      }
      catch (final PasswordBasedEncryptor.EncryptFail e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showPasswordErrorMessage("login.password.error", e.getMessage());
          }
        });
      }
      catch (final InvalidState e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorText(e.getMessage());
            logout();
          }
        });
        e.printStackTrace();
      }
      catch (final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.showErrorMessage("login.server.connection.failure");
            final StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            e.printStackTrace();
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                JTextArea textArea = new JTextArea(stringWriter.toString());
                GuiUtils.show(textArea);
                logout();
              }
            });
          }
        });
      }
      finally {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            dataLoadingDisplay.complete();
          }
        });
      }
    }
  }

  private static class ShutDownThread extends Thread {
    private DataAccess dataAccess;
    private SessionDirectory sessionDirectory = null;

    public ShutDownThread(DataAccess dataAccess) {
      this.dataAccess = dataAccess;
    }

    synchronized public void run() {
      try {
        if (dataAccess != null) {
          dataAccess.takeSnapshot();
          dataAccess.disconnect();
          dataAccess = null;
        }
        if (sessionDirectory != null) {
          sessionDirectory.close();
          sessionDirectory = null;
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }


}
