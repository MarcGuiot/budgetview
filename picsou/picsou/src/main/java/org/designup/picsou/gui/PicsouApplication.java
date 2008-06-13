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
import org.crossbowlabs.splits.TextLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouDescriptionService;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
    PicsouMacLookAndFeel.initApplicationName();
    MRJAdapter.addOpenDocumentListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ApplicationEvent event = (ApplicationEvent)e;
        initialFile = new File[]{event.getFile()};
      }
    });
  }

  public static void main(String... args) throws Exception {
    Locale.setDefault(Locale.ENGLISH);
    if (args.length > 1) {
      args = parseLanguage(args);
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

  public static String[] parseLanguage(String... args) {
    if (args[0].equals("-l")) {
      if (args[1].equals("fr")) {
        Locale.setDefault(Locale.FRANCE);
        Lang.setLocale(Locale.FRANCE);
      }
      String[] strings = new String[args.length - 2];
      System.arraycopy(args, 2, strings, 0, args.length - 2);
      args = strings;
    }
    return args;
  }

  private static String getServerAddress() {
    return getSystemValue(DEFAULT_ADDRESS_PROPERTY, "");
  }

  public static String getLocalPrevaylerPath() {
    return getSystemValue(LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.picsou/data");
  }

  public static void clearRepository() {
    Files.deleteSubtree(new File(getLocalPrevaylerPath()));
  }

  public static void clearRepositoryIfNeeded() {
    if ("true".equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      clearRepository();
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
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);

    UIManager.put("ColorService", directory.get(ColorService.class));

    return directory;
  }

}
