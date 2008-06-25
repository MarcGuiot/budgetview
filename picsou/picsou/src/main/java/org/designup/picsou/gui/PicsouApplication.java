package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PicsouApplication {

  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = "picsou.prevayler.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = "picsou.server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = "picsou.prevayler.delete";
  public static String IS_DATA_IN_MEMORY = "picsou.data.in.memory";
  private static String DEFAULT_ADDRESS = "https://startupxp.dynalias.org";

  private OpenRequestManager openRequestManager = new OpenRequestManager();
  private SingleApplicationInstanceListener singleInstanceListener;

  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {
    new PicsouApplication().run(args);
  }

  public void run(String... args) throws Exception {
    MRJAdapter.addOpenDocumentListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ApplicationEvent event = (ApplicationEvent)e;
        openRequestManager.openFiles(Collections.singletonList(event.getFile()));
      }
    });
    Locale.setDefault(Locale.ENGLISH);
    if (args.length > 1) {
      args = parseLanguage(args);
    }
    List<File> fileToOpen = new ArrayList<File>();
    for (String arg : args) {
      File file = new File(arg);
      if (file.exists()) {
        fileToOpen.add(file);
      }
    }
    if (!fileToOpen.isEmpty()) {
      openRequestManager.openFiles(fileToOpen);
    }

    singleInstanceListener = new SingleApplicationInstanceListener(openRequestManager);
    if (singleInstanceListener.findRemoteOrListen() == SingleApplicationInstanceListener.ReturnState.EXIT) {
      Thread.sleep(2000);
      return;
    }

    clearRepositoryIfNeeded();

    Directory directory = createDirectory();
    directory.add(openRequestManager);

    final MainWindow mainWindow = new MainWindow();
    final LoginPanel loginPanel = new LoginPanel(getServerAddress(), getLocalPrevaylerPath(), isDataInMemory(),
                                                 mainWindow, directory);
    mainWindow.getFrame().addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
      }
    });
    mainWindow.show();
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

  public boolean isDataInMemory() {
    return "true".equalsIgnoreCase(System.getProperty(IS_DATA_IN_MEMORY));
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

  public void shutdown() throws Exception {
    singleInstanceListener.shutdown();
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
    directory.add(FontLocator.class, Gui.FONT_LOCATOR);

    UIManager.put("ColorService", directory.get(ColorService.class));

    return directory;
  }

}
