package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.client.http.MD5PasswordBasedEncryptor;
import org.designup.picsou.client.http.PasswordBasedEncryptor;
import org.designup.picsou.client.http.RedirectPasswordBasedEncryptor;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.plaf.ApplicationLAF;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.gui.upgrade.UpgradeService;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.bank.BankPluginService;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;
import picsou.AwtExceptionHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.LogManager;
import java.util.regex.Pattern;

public class PicsouApplication {

  public static final String APPLICATION_VERSION = "0.40";
  public static final Long JAR_VERSION = 37L;
  public static final Long BANK_CONFIG_VERSION = 7L;
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

  private WindowAdapter windowOpenListener;
  private AbstractAction mrjDocumentListener;

  public static boolean EXIT_ON_DATA_ERROR = true;

  static {
    AwtExceptionHandler.registerHandler();
    PicsouMacLookAndFeel.initApplicationName();
    Gui.init();
  }

  public static void main(final String... args) throws Exception {
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
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        try {
          new PicsouApplication().run(args);
        }
        catch (Exception e) {
          Log.write("At startup ", e);
        }
      }
    });
  }

  static void changeDate() {
    String date = System.getProperty(FORCE_DATE);
    if (date != null) {
      TimeService.setCurrentDate(Dates.parse(date));
    }
  }

  private static void initLogger() {
    try {
      System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.Jdk14Logger");
      String sout = System.getProperty(LOG_SOUT);
      InputStream propertiesStream = PicsouApplication.class.getResourceAsStream("/logging.properties");
      LogManager.getLogManager().readConfiguration(propertiesStream);

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
    initEncryption();
    mrjDocumentListener = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ApplicationEvent event = (ApplicationEvent)e;
        openRequestManager.openFiles(Collections.singletonList(event.getFile()));
      }
    };
    MRJAdapter.addOpenDocumentListener(mrjDocumentListener);
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

    try {
      final MainWindow mainWindow = new MainWindow(this, getServerAddress(), getLocalPrevaylerPath(), isDataInMemory(), directory);
      mainWindow.show();
    }
    catch (InvalidState e) {
      Log.write("Erreur au lancement", e);
      showMultipleInstanceError(e.getMessage());
    }
  }

  private void initEncryption() {
    Thread thread = new Thread() {
      public void run() {
        try {
          MD5PasswordBasedEncryptor.init();
        }
        catch (NoSuchAlgorithmException e) {
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  void showMultipleInstanceError(String message) {
    SplitsBuilder builder = SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/utils/messageDialog.splits");

    builder.add("title", new JLabel(Lang.get("init.dialog.lock.title")));
    JEditorPane editorPane = new JEditorPane("text/html", Lang.get("init.dialog.lock.content", message));
    builder.add("message", editorPane);

    PicsouDialog dialog = PicsouDialog.create((JFrame)null, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), new AbstractAction(Lang.get("close")) {
      public void actionPerformed(ActionEvent e) {
        System.exit(1);
      }
    }, null);
    dialog.pack();
    dialog.showCentered();
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
    if (System.getProperty(LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      if (GuiUtils.isMacOSX()) {
        return System.getProperty("user.home") + "/Library/Application Support/CashPilot";
      }
      if (GuiUtils.isVista()) {
        return System.getProperty("user.home") + "/AppData/Local/CashPilot";
      }
      if (GuiUtils.isWindows()) {
        return System.getProperty("user.home") + "/Application Data/CashPilot";
      }
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
      MRJAdapter.removeOpenDocumentListener(mrjDocumentListener);
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

  private static Directory createDirectory(OpenRequestManager openRequestManager) throws IOException {
    Directory directory = new DefaultDirectory();
    directory.add(OpenRequestManager.class, openRequestManager);
    directory.add(ApplicationLAF.initUiService());
    directory.add(new TimeService());
    directory.add(new UpgradeService(directory));
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    directory.add(GlobModel.class, PicsouGuiModel.get());
    directory.add(SelectionService.class, new SelectionService());
    ApplicationColors.registerColorService(directory);
    directory.add(ImageLocator.class, Gui.IMAGE_LOCATOR);
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    directory.add(FontLocator.class, Gui.FONT_LOCATOR);
    directory.add(PasswordBasedEncryptor.class, new RedirectPasswordBasedEncryptor());
    directory.add(createConfigService());
    directory.add(new BankPluginService());
    return directory;
  }

  private static ConfigService createConfigService() {
    Long localConfigVersion;
    String configPath = getBankConfigPath();
    File lastJar = findLastJar(configPath);
    if (lastJar != null) {
      localConfigVersion = extractVersion(lastJar.getName());
    }
    else {
      localConfigVersion = BANK_CONFIG_VERSION;
    }
    return new ConfigService(APPLICATION_VERSION, JAR_VERSION, localConfigVersion, lastJar);
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
