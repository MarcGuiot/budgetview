package com.budgetview.desktop;

import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.client.http.EncryptToTransportDataAccess;
import com.budgetview.desktop.components.PicsouFrame;
import com.budgetview.desktop.components.layoutconfig.LayoutConfigService;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.client.DataAccess;
import com.budgetview.client.local.LocalSessionDataTransport;
import com.budgetview.desktop.plaf.PicsouMacLookAndFeel;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.session.SessionDirectory;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class DevDesktopApp {

  static {
    PicsouMacLookAndFeel.initApplicationName();
    Gui.init();
  }

  public static void main(String... args) throws Exception {
    if (args.length > 1) {
      args = Application.parseLanguage(args);
    }
    List<String> arguments = new ArrayList<String>();
    arguments.addAll(Arrays.asList(args));
    String user = parseArguments(arguments, "-u", "user");
    String password = parseArguments(arguments, "-p", "pwd");
    String snapshot = parseArguments(arguments, "-s", null);
    run(user, password, snapshot);
  }

  public static Directory run(String user, String password, String snapshot) throws Exception {
    Application.clearRepositoryIfNeeded();
    Application.changeDate();
    Log.setDebugEnabled(true);

    Directory directory = Application.createDirectory();

    SessionDirectory sessionDirectory = createServerDirectory(snapshot);
    directory.add(SessionDirectory.class, sessionDirectory);
    DataAccess dataAccess =
      new EncryptToTransportDataAccess(new LocalSessionDataTransport(sessionDirectory.getServiceDirectory()),
                                       directory);
    dataAccess.connect(Application.JAR_VERSION);

    boolean registered = isRegistered(user, password, dataAccess);

    final PicsouFrame frame = new PicsouFrame(Lang.get("application"), directory);
    directory.add(JFrame.class, frame);
    PicsouInit init = PicsouInit.init(dataAccess, directory, registered, false);
    init.loadUserData(user, false, false).load();

    Directory initDirectory = init.getDirectory();
    MainPanel.init(init.getRepository(), initDirectory, new WindowManager() {
      public PicsouFrame getFrame() {
        return frame;
      }

      public void setPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.validate();
      }

      public void logout() {
        System.exit(1);
      }

      public void logOutAndDeleteUser(String name, char[] passwd) {
        System.exit(1);
      }

      public void logOutAndOpenDemo() {
        System.exit(1);
      }

      public void logOutAndAutoLogin() {
        System.exit(1);
      }

      public void shutdown() {
        System.exit(1);
      }
    })
      .prepareForDisplay();

    directory.get(LayoutConfigService.class).show(frame);

    return directory;
  }

  private static SessionDirectory createServerDirectory(String snapshot) throws FileNotFoundException {
    SessionDirectory sessionDirectory;
    if (snapshot != null) {
      sessionDirectory = new SessionDirectory(new FileInputStream(snapshot));
    }
    else {
      sessionDirectory = new SessionDirectory(AppPaths.getCurrentDataPath(), Application.isDataInMemory());
    }
    return sessionDirectory;
  }

  private static boolean isRegistered(String user, String password, DataAccess dataAccess) {
    boolean registered = false;
    try {
      registered = dataAccess.createUser(user, password.toCharArray(), false);
    }
    catch (UserAlreadyExists e) {
      registered = dataAccess.initConnection(user, password.toCharArray(), false);
    }
    return registered;
  }

  private static String parseArguments(List<String> args, String key, String defaultValue) {
    for (Iterator<String> it = args.iterator(); it.hasNext(); ) {
      String arg = it.next();
      if (key.equals(arg)) {
        it.remove();
        if (it.hasNext()) {
          String value = it.next();
          it.remove();
          return value;
        }
      }
    }
    return defaultValue;
  }
}
