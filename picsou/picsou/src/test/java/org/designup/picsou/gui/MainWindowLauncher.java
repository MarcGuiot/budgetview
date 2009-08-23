package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.server.ServerDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
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
    serverAccess.connect();
    boolean registered = false;
    try {
      registered = serverAccess.createUser(user, password.toCharArray());
    }
    catch (UserAlreadyExists userAlreadyExists) {
      registered = serverAccess.initConnection(user, password.toCharArray(), false);
    }
    PicsouInit init = PicsouInit.init(serverAccess, directory, registered);
    PicsouInit.PreLoadData data = init.loadUserData(user, false, registered);
    data.load();

    final PicsouFrame frame = new PicsouFrame("test");
    MainPanel.init(init.getRepository(), init.getDirectory(), new MainPanel.WindowManager() {
      public PicsouFrame getFrame() {
        return frame;
      }

      public void setPanel(JPanel panel) {
        frame.setContentPane(panel);
        frame.validate();
      }

      public void loggout() {
        System.exit(1);
      }

      public void logOutAndDeleteUser(String name, char[] passwd) {
      }
    })
      .show();
    frame.setSize(Gui.getWindowSize(1100, 800));
    GuiUtils.showCentered(frame);

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
