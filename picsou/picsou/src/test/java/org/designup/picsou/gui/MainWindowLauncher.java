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
    Directory directory = serverDirectory.getServiceDirectory();
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(directory), directory);
    run(serverAccess);
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

  public static GlobRepository run(ServerAccess serverAccess) throws Exception {
    try {
      serverAccess.connect();
      serverAccess.createUser(user, password.toCharArray());
    }
    catch (UserAlreadyExists userAlreadyExists) {
      serverAccess.initConnection(user, password.toCharArray(), false);
    }
    Directory directory = PicsouApplication.createDirectory();
    directory.add(OpenRequestManager.class, openRequestManager);
    PicsouInit init = PicsouInit.init(serverAccess, user, true, directory);

    MainWindow window = new MainWindow();
    MainPanel.show(init.getRepository(), init.getDirectory(), window);
    window.show();

    return init.getRepository();
  }
}
