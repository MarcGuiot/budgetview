package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.server.ServerDirectory;
import org.globsframework.utils.directory.Directory;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainWindowLauncher {
  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {
    if (args.length > 1) {
      args = PicsouApplication.parseLanguage(args);
    }
    List<String> arguments = new ArrayList<String>();
    arguments.addAll(Arrays.asList(args));
    String user = parseArguments(arguments, "-u", "user");
    String password = parseArguments(arguments, "-p", "pwd");
    String snapshot = parseArguments(arguments, "-s", null);
    run(user, password, snapshot);
  }

  public static Directory run(String user, String password, String snapshot) throws Exception {
    PicsouApplication.clearRepositoryIfNeeded();
    PicsouApplication.changeDate();

    ServerDirectory serverDirectory;
    if (snapshot != null) {
      serverDirectory = new ServerDirectory(new FileInputStream(snapshot));
    }
    else {
      serverDirectory = new ServerDirectory(PicsouApplication.getLocalPrevaylerPath(), PicsouApplication.isDataInMemory());
    }
    Directory directory = PicsouApplication.createDirectory(new OpenRequestManager());
    directory.add(ServerDirectory.class, serverDirectory);
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                           directory);
    boolean newUser;
    boolean validUser = serverAccess.connect();
    try {
      serverAccess.createUser(user, password.toCharArray());
      newUser = true;
    }
    catch (UserAlreadyExists userAlreadyExists) {
      serverAccess.initConnection(user, password.toCharArray(), false);
      newUser = false;
    }
    PicsouInit init = PicsouInit.init(serverAccess, user, validUser, newUser, directory, false);

    MainWindow window = new MainWindow();
    MainPanel.init(init.getRepository(), init.getDirectory(), window).show();
    window.show();

    return directory;
  }

  private static String parseArguments(List<String> args, String key, String defaultValue) {
    for (Iterator<String> it = args.iterator(); it.hasNext();) {
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
