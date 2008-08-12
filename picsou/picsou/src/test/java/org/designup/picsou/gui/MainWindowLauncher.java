package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.server.ServerDirectory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class MainWindowLauncher {
  private static OpenRequestManager openRequestManager = new OpenRequestManager();
  private static String user;
  private static String password;

  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {

    if (args.length > 1) {
      args = PicsouApplication.parseLanguage(args);
    }
    List<String> arguments = new ArrayList<String>();
    arguments.addAll(Arrays.asList(args));
    user = parseArguments(arguments, "-u", "user");
    password = parseArguments(arguments, "-p", "pwd");
    ServerDirectory serverDirectory = new ServerDirectory(PicsouApplication.getLocalPrevaylerPath(), false);
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                           PicsouApplication.createDirectory());
    run(serverAccess, PicsouApplication.createDirectory());
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

  private static GlobRepository run(ServerAccess serverAccess, Directory directory) throws Exception {
    boolean newUser;
    try {
      serverAccess.connect();
      serverAccess.createUser(user, password.toCharArray());
      newUser = true;
    }
    catch (UserAlreadyExists userAlreadyExists) {
      serverAccess.initConnection(user, password.toCharArray(), false);
      newUser = false;
    }
    directory.add(OpenRequestManager.class, openRequestManager);
    PicsouInit init = PicsouInit.init(serverAccess, user, newUser, directory);

    MainWindow window = new MainWindow();
    MainPanel.show(init.getRepository(), init.getDirectory(), window);
    window.show();

    return init.getRepository();
  }
}
