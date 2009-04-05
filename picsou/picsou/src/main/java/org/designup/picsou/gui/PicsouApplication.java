package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.components.ArrowButtonUI;
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
import org.designup.picsou.gui.upgrade.UpgradeService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.utils.GuiUtils;
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
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class PicsouApplication {

  public static final String APPLICATION_VERSION = "0.17";
  public static final Long JAR_VERSION = 9L;
  public static final Long BANK_CONFIG_VERSION = 5L;
  private static final String JAR_DIRECTORY = "jars";
  private static final String BANK_CONFIG_DIRECTORY = "configs";
  public static final String APPNAME = "cashpilot";
  private static final String CONFIG = "config";
  private static final Pattern CONFIG_FILTER = Pattern.compile(CONFIG + "[0-9][0-9]*" + "\\.jar");

  public static final String LOG_SOUT = APPNAME + ".log.sout";
  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = APPNAME + ".prevayler.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = APPNAME + ".server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = APPNAME + ".prevayler.delete";
  public static String IS_DATA_IN_MEMORY = APPNAME + ".data.in.memory";
  public static String FORCE_DATE = APPNAME + ".today";
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
  private static final String ARROW_BUTTON_UI = "org" + dot() + "designup.picsou.gui.components.ArrowButtonUI";
  private WindowAdapter windowOpenListener;

  static {
    PicsouMacLookAndFeel.initApplicationName();
  }

  public static void main(String... args) throws Exception {
    if (args.length > 0) {
      if ("-v".equals(args[0])) {
        if (args.length > 1) {
          if ("-jar".equals(args[1])) {
            System.out.println("Jar version: " + JAR_VERSION);
            return;
          }
          if ("-soft".equals(args[1])) {
            System.out.println("Software version: " + APPLICATION_VERSION);
            return;
          }
          if ("-config".equals(args[1])) {
            System.out.println("Bank config version: " + BANK_CONFIG_VERSION);
            return;
          }
        }
        System.out.println("Software version: " + APPLICATION_VERSION);
        System.out.println("Jar version: " + JAR_VERSION);
        System.out.println("Bank config version: " + BANK_CONFIG_VERSION);
        return;
      }
    }
    Utils.beginRemove();
    changeDate();
    Utils.endRemove();
    new PicsouApplication().run(args);
  }

  static void changeDate() {
    String date = System.getProperty(FORCE_DATE);
    if (date != null) {
      TimeService.setCurrentDate(Dates.parse(date));
    }
  }

  private static void initLogger() {
    try {
      String sout = System.getProperty(LOG_SOUT);
      if ("true".equalsIgnoreCase(sout)) {
        return;
      }
      File logFilePath = new File(getDataPath() + "/" + "logs");
      logFilePath.mkdirs();
      File logFile = new File(logFilePath, "log.txt");
      if (logFile.exists() && logFile.length() > 2 * 1024 * 1024) {
        File oldFile = new File(logFilePath, "oldLog.txt");
        if (oldFile.exists()) {
          oldFile.delete();
        }
        logFile.renameTo(oldFile);
      }
      FileOutputStream stream = new FileOutputStream(logFile, true);
      PrintStream output = new PrintStream(stream);
      output.println("---------------------------");
      output.println("version : " + PicsouApplication.JAR_VERSION + " ; " + TimeService.getToday());
      Log.init(output);
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

    initLogger();
    clearRepositoryIfNeeded();

    directory = createDirectory(openRequestManager);

    final MainWindow mainWindow = new MainWindow();
    final LoginPanel loginPanel = new LoginPanel(getServerAddress(), getLocalPrevaylerPath(), isDataInMemory(),
                                                 mainWindow, directory);
    windowOpenListener = new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        loginPanel.initFocus();
        // pour que loginPanel passe au GC
        mainWindow.getFrame().removeWindowListener(windowOpenListener);
        windowOpenListener = null;
      }
    };
    mainWindow.getFrame().addWindowListener(windowOpenListener);
    mainWindow.getFrame().addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.out.println("PicsouApplication.windowClosing");
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

  public static boolean isDataInMemory() {
    return "true".equalsIgnoreCase(System.getProperty(IS_DATA_IN_MEMORY));
  }

  public static String getLocalPrevaylerPath() {
    return getDataPath() + "/data";
  }

  public static String getDataPath() {
    if (GuiUtils.isMacOSX() && System.getProperty(LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      return System.getProperty("user.home") + "/Library/Application Support/CashPilot";
    }
    if (GuiUtils.isVista() && System.getProperty(LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      return System.getProperty("user.home") + "/AppData/Local/CashPilot";
    }
    if (GuiUtils.isWindows() && System.getProperty(LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      return System.getProperty("user.home") + "/Application Data/CashPilot";
    }
    return getSystemValue(LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.cashpilot");
  }

  public static String getBankConfigPath() {
    return getDataPath() + "/" + PicsouApplication.BANK_CONFIG_DIRECTORY;
  }

  public static String getJarPath() {
    return getDataPath() + "/" + PicsouApplication.JAR_DIRECTORY;
  }

  public static void clearRepository() {
    Files.deleteSubtree(new File(getDataPath()));
  }

  public static void clearRepositoryIfNeeded() {
    if ("true".equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      clearRepository();
    }
  }

  public void shutdown() {
    try {
      singleInstanceListener.shutdown();
      if (directory != null) {
        directory.get(ColorService.class).removeAllListeners();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
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
    return createDirectory(new OpenRequestManager());
  }

  public static Directory createDirectory(OpenRequestManager openRequestManager) throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(OpenRequestManager.class, openRequestManager);
    directory.add(initUiService());
    directory.add(new TimeService());
    directory.add(new UpgradeService(directory));
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(GlobModel.class, PicsouGuiModel.get());
    directory.add(SelectionService.class, new SelectionService());
    PicsouColors.registerColorService(directory);
    directory.add(ImageLocator.class, Gui.IMAGE_LOCATOR);
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    directory.add(FontLocator.class, Gui.FONT_LOCATOR);

    Long localConfigVersion;
    String configPath = getBankConfigPath();
    File lastJar = findLastJar(configPath);
    if (lastJar != null) {
      localConfigVersion = extractVersion(lastJar.getName());
    }
    else {
      localConfigVersion = BANK_CONFIG_VERSION;
    }

    directory.add(new ConfigService(APPLICATION_VERSION, JAR_VERSION, localConfigVersion, lastJar));

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
    uiService.registerClass(ARROW_BUTTON_UI, ArrowButtonUI.class);
    return uiService;
  }

  private static String dot() {
    return ".";
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
