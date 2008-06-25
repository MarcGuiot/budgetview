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

public class MainWindowLauncher {
  private static final String COLOR_SELECTOR_PROPERTY = "ENABLE_COLOR_SELECTOR";
  private static OpenRequestManager openRequestManager = new OpenRequestManager();

  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {

    if (args.length > 1) {
      args = PicsouApplication.parseLanguage(args);
    }
    ServerDirectory serverDirectory = new ServerDirectory(PicsouApplication.getLocalPrevaylerPath(), false);
    Directory directory = serverDirectory.getServiceDirectory();
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(directory));
    run(serverAccess, args);
  }

  public static GlobRepository run(ServerAccess serverAccess, String[] args) throws Exception {
    try {
      serverAccess.createUser("user", "pwd".toCharArray());
    }
    catch (UserAlreadyExists userAlreadyExists) {
      serverAccess.initConnection("user", "pwd".toCharArray(), false);
    }
    Directory directory = PicsouApplication.createDirectory();
    directory.add(OpenRequestManager.class, openRequestManager);
    PicsouInit init = PicsouInit.init(serverAccess, "user", true, directory);

    MainWindow window = new MainWindow();
    MainPanel.show(init.getRepository(), init.getDirectory(), window);
    window.show();

    return init.getRepository();
  }
}
