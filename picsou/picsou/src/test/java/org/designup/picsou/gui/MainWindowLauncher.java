package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.layoutconfig.LayoutConfigService;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.AppPaths;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class MainWindowLauncher {

  static {
    PicsouMacLookAndFeel.initApplicationName();
    Gui.init();
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

    Directory directory = PicsouApplication.createDirectory();
    MRJAdapter.addAboutListener(new AboutAction(directory));

    ServerDirectory serverDirectory = createServerDirectory(snapshot);
    directory.add(ServerDirectory.class, serverDirectory);
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()),
                                           directory);
    serverAccess.connect(PicsouApplication.JAR_VERSION);

    boolean registered = isRegistered(user, password, serverAccess);

    final PicsouFrame frame = new PicsouFrame(Lang.get("application"), directory);
    directory.add(JFrame.class, frame);
    PicsouInit init = PicsouInit.init(serverAccess, directory, registered, false);
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

    directory.get(LayoutConfigService.class).show(frame, init.getRepository());

    return directory;
  }

  private static ServerDirectory createServerDirectory(String snapshot) throws FileNotFoundException {
    ServerDirectory serverDirectory;
    if (snapshot != null) {
      serverDirectory = new ServerDirectory(new FileInputStream(snapshot));
    }
    else {
      serverDirectory = new ServerDirectory(AppPaths.getCurrentDataPath(), PicsouApplication.isDataInMemory());
    }
    return serverDirectory;
  }

  private static boolean isRegistered(String user, String password, ServerAccess serverAccess) {
    boolean registered = false;
    try {
      registered = serverAccess.createUser(user, password.toCharArray(), false);
    }
    catch (UserAlreadyExists e) {
      registered = serverAccess.initConnection(user, password.toCharArray(), false);
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
