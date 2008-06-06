package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.Files;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouDescriptionService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PicsouApplication {

  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = "picsou.prevayler.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = "picsou.server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = "picsou.prevayler.delete";
  private static String DEFAULT_ADDRESS = "https://startupxp.dynalias.org";

  public static File[] initialFile;

  static {
    MRJAdapter.addOpenDocumentListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ApplicationEvent event = (ApplicationEvent) e;
        initialFile = new File[]{event.getFile()};
      }
    });
  }

  public static void main(String... args) throws Exception {
    Locale.setDefault(Locale.FRANCE);
    if (System.getProperty("mrj.version") != null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    if (SingleApplicationInstanceListener.sendAlreadyOpen(args)) {
      return;
    }
//  TODO:  SingleApplicationInstanceListener.listenForFile(panel);

    List<File> fileToOpen = new ArrayList<File>();
    for (String arg : args) {
      File file = new File(arg);
      if (file.exists()) {
        fileToOpen.add(file);
      }
    }
    if (!fileToOpen.isEmpty()) {
      initialFile = fileToOpen.toArray(new File[fileToOpen.size()]);
    }

    clearRepositoryIfNeeded();

    Directory directory = createDirectory();

    final MainWindow window = new MainWindow();
    final LoginPanel loginPanel = new LoginPanel(getServerAddress(), getLocalPrevaylerPath(), window, directory);
    window.setPanel(loginPanel.getJPanel());
    window.getFrame().addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
      }
    });
    window.show();
  }

  private static String getServerAddress() {
    return getSystemValue(DEFAULT_ADDRESS_PROPERTY, "");
  }

  public static String getLocalPrevaylerPath() {
    return getSystemValue(LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.picsou/data");
  }

  public static void clearRepositoryIfNeeded() {
    if ("true".equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      Files.deleteSubtree(new File(getLocalPrevaylerPath()));
    }
  }

  public static void shutdown() throws Exception {
    SingleApplicationInstanceListener.shutdown();
  }

  private static String getSystemValue(String propertyName, String defaultPropertyValue) {
    String value = null;
    try {
      value = System.getProperty(propertyName);
    }
    catch (Throwable e) {
    }
    if (value == null) {
      return defaultPropertyValue;
    }
    return value;
  }

  public static Directory createDirectory() throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(GlobModel.class, PicsouGuiModel.get());
    directory.add(SelectionService.class, new SelectionService());
    PicsouColors.registerColorService(directory);
    directory.add(IconLocator.class, Gui.ICON_LOCATOR);

    UIManager.put("ColorService", directory.get(ColorService.class));

    return directory;
  }

}
