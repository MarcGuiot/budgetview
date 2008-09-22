package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.components.SelectionToggleUI;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.plaf.ButtonPanelItemUI;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.plaf.PicsouSplitPaneUI;
import org.designup.picsou.gui.plaf.WavePanelUI;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.components.HyperlinkButtonUI;
import org.globsframework.gui.splits.components.ShadowedLabelUI;
import org.globsframework.gui.splits.components.StyledPanelUI;
import org.globsframework.gui.splits.components.StyledToggleButtonUI;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

public class PicsouApplication {

  public static final Long APPLICATION_VERSION = 1L;
  public static final Long JAR_VERSION = 1L;
  public static final Long CONFIG_VERSION = 1L;
  private static final String JAR_DIRECTORY = "jars";
  private static final String CONFIG_DIRECTORY = "configs";
  public static final String PICSOU = "picsou";
  private static final String CONFIG = "config";
  private static final Pattern CONFIG_FILTER = Pattern.compile(CONFIG + "[0-9][0-9]*" + "\\.jar");

  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = PICSOU + ".prevayler.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = PICSOU + ".server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = PICSOU + ".prevayler.delete";
  public static String IS_DATA_IN_MEMORY = PICSOU + ".data.in.memory";
  public static String FORCE_DATE = PICSOU + ".today";
  private static String DEFAULT_ADDRESS = "https://startupxp.dynalias.org";
  public static final String REGISTER_URL = "https://91.121.123.100:8443"; //startupxp.dynalias.org";
  public static final String FTP_URL = "ftp://91.121.123.100"; //startupxp.dynalias.org";

  private OpenRequestManager openRequestManager = new OpenRequestManager();
  private SingleApplicationInstanceListener singleInstanceListener;
  private Directory directory;

  private static final String PANEL_UI = "org" + dot() + "designup.picsou.gui.plaf.WavePanelUI";
  private static final String LINK_BUTTON_UI = "org" + dot() + "globsframework.gui.splits.components.HyperlinkButtonUI";
  private static final String STYLED_TOGGLE_BUTTON_UI = "org" + dot() + "globsframework.gui.splits.components.StyledToggleButtonUI";
  private static final String STYLED_PANEL_UI = "org" + dot() + "globsframework.gui.splits.components.StyledPanelUI";
  private static final String SHADOWED_LABEL_UI = "org" + dot() + "globsframework.gui.splits.components.ShadowedLabelUI";
  private static final String SPLITPANE_UI = "org" + dot() + "designup.picsou.gui.plaf.PicsouSplitPaneUI";
  private static final String SERIES_TOGGLE_UI = "org" + dot() + "designup.picsou.gui.components.SelectionToggleUI";
  private static final String BUTTON_PANEL_UI = "org" + dot() + "designup.picsou.gui.plaf.ButtonPanelItemUI";

  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {
    Utils.beginRemove();
    changeDate();
    Utils.endRemove();
    initLogger();
    new PicsouApplication().run(args);
  }

  private static void changeDate() {
    String date = System.getProperty(FORCE_DATE);
    if (date != null) {
      TimeService.setCurrentDate(Dates.parseMonth(date));
    }
  }

  private static void initLogger() {
    InputStream stream = PicsouApplication.class.getClassLoader().getResourceAsStream("logging.properties");
    try {
      LogManager.getLogManager().readConfiguration(stream);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
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
      if (file.exists() && !file.isDirectory()) {
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

    directory = createDirectory();
    directory.add(openRequestManager);

    final MainWindow mainWindow = new MainWindow();
    final LoginPanel loginPanel = new LoginPanel(getServerAddress(), getLocalPrevaylerPath(), isDataInMemory(),
                                                 mainWindow, directory);
    mainWindow.getFrame().addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
      }

      public void windowClosing(WindowEvent e) {
        shutdown();
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
      else if (args[1].equals("en")) {
        Locale.setDefault(Locale.ENGLISH);
        Lang.setLocale(Locale.ENGLISH);
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
    return "true" .equalsIgnoreCase(System.getProperty(IS_DATA_IN_MEMORY));
  }

  public static String getLocalPrevaylerPath() {
    return getPicsouPath() + "/data";
  }

  public static String getPicsouPath() {
    if (Gui.isMacOSX() && System.getProperty(LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      return System.getProperty("user.home") + "/Library/Application Support/" + PICSOU;
    }
    return getSystemValue(LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.picsou");
  }

  public static String getPicsouConfigPath() {
    return getPicsouPath() + "/" + PicsouApplication.CONFIG_DIRECTORY;
  }

  public static String getPicsouJarPath() {
    return getPicsouPath() + "/" + PicsouApplication.JAR_DIRECTORY;
  }

  public static void clearRepository() {
    Files.deleteSubtree(new File(getLocalPrevaylerPath()));
  }

  public static void clearRepositoryIfNeeded() {
    if ("true" .equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      clearRepository();
    }
  }

  public void shutdown() {
    singleInstanceListener.shutdown();
    if (directory != null) {
      directory.get(ColorService.class).removeAllListeners();
    }
  }

  private static String getSystemValue(String propertyName, String defaultPropertyValue) {
    String value;
    try {
      value = System.getProperty(propertyName);
    }
    catch (Throwable e) {
      value = null;
    }
    if (value == null) {
      return defaultPropertyValue;
    }
    return value;
  }

  public static Directory createDirectory() throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(new TimeService());
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(GlobModel.class, PicsouGuiModel.get());
    directory.add(SelectionService.class, new SelectionService());
    PicsouColors.registerColorService(directory);
    directory.add(IconLocator.class, Gui.ICON_LOCATOR);
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    directory.add(FontLocator.class, Gui.FONT_LOCATOR);
    UIService uiService = initUiService();
    directory.add(uiService);
    Long localConfigVersion = getConfigVersion();
    String configPath = getPicsouPath() + "/config";
    File lastJar = findLastJar(configPath);
    if (lastJar != null) {
      localConfigVersion = extractVersion(lastJar.getName());
    }
    else {
      localConfigVersion = CONFIG_VERSION;
    }

    directory.add(new ConfigService(APPLICATION_VERSION, JAR_VERSION, localConfigVersion,
                                    lastJar));

    UIManager.put("ColorService", directory.get(ColorService.class));

    return directory;
  }

  private static UIService initUiService() {
    UIService uiService = new UIService();
    uiService.registerClass(PANEL_UI, WavePanelUI.class);
    uiService.registerClass(LINK_BUTTON_UI, HyperlinkButtonUI.class);
    uiService.registerClass(STYLED_TOGGLE_BUTTON_UI, StyledToggleButtonUI.class);
    uiService.registerClass(STYLED_PANEL_UI, StyledPanelUI.class);
    uiService.registerClass(SHADOWED_LABEL_UI, ShadowedLabelUI.class);
    uiService.registerClass(SPLITPANE_UI, PicsouSplitPaneUI.class);
    uiService.registerClass(SERIES_TOGGLE_UI, SelectionToggleUI.class);
    uiService.registerClass(BUTTON_PANEL_UI, ButtonPanelItemUI.class);
    return uiService;
  }

  private static String dot() {
    return ".";
  }

  static private Long getConfigVersion() {
    String configPath = getPicsouPath() + "/config";
    File lastJar = findLastJar(configPath);
    if (lastJar != null) {
      return extractVersion(lastJar.getName());
    }
    return PicsouApplication.CONFIG_VERSION;
  }

  static private File findLastJar(String path) {
    File directory = new File(path);
    File[] files = directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return CONFIG_FILTER.matcher(name).matches();
      }
    });
    if (files == null || files.length == 0) {
      return null;
    }
    Arrays.sort(files, new Comparator<File>() {
      public int compare(File s1, File s2) {
        return extractVersion(s1.getName()).compareTo(extractVersion(s2.getName()));
      }
    });

    return files[files.length - 1];
  }

  static private Long extractVersion(String fileName) {
    if (fileName != null && CONFIG_FILTER.matcher(fileName).matches()) {
      return Long.parseLong(fileName.substring(fileName.indexOf(CONFIG) + CONFIG.length(),
                                               fileName.indexOf(".")));
    }
    return null;
  }
}
