package org.designup.picsou.gui;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorServiceEditor;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.server.ServerDirectory;

import java.awt.*;
import java.io.File;

public class MainWindowLauncher {
  private static final String COLOR_SELECTOR_PROPERTY = "ENABLE_COLOR_SELECTOR";

  public static void main(String[] args) throws Exception {
    PicsouApplication.clearRepositoryIfNeeded();
    ServerDirectory serverDirectory = new ServerDirectory(PicsouApplication.getLocalPrevaylerPath(), false);
    Directory directory = serverDirectory.getServiceDirectory();
    ServerAccess serverAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(directory));
    run(serverAccess, args);
  }

  public static GlobRepository run(ServerAccess serverAccess, String[] args) throws Exception {
    serverAccess.createUser("user", "pwd".toCharArray());
    Directory directory = PicsouApplication.createDirectory();
    PicsouInit init = PicsouInit.init(serverAccess, "user", true, directory);

    MainWindow window = new MainWindow();
    MainPanel panel = new MainPanel(init.getRepository(), init.getDirectory(), window.getFrame());
    window.setPanel(panel.getJPanel());
    if (args.length > 0) {
      panel.openFile(new File(args[0]), true);
    }
    window.show();

    if ("true".equalsIgnoreCase(System.getProperty(COLOR_SELECTOR_PROPERTY))) {
      showColorEditor(init.getDirectory(), window.getFrame());
    }
    return init.getRepository();
  }

  public static void showColorEditor(Directory directory, Container container) {
    ColorServiceEditor.showInFrame(directory.get(ColorService.class), container);
  }
}
