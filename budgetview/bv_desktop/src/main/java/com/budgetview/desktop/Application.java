package com.budgetview.desktop;

import com.budgetview.bank.BankPluginService;
import com.budgetview.bank.BankSynchroService;
import com.budgetview.client.mail.MailService;
import com.budgetview.desktop.accounts.utils.AccountPositionNotificationHandler;
import com.budgetview.desktop.addons.AddOnService;
import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.desktop.description.PicsouDescriptionService;
import com.budgetview.desktop.mobile.MobileService;
import com.budgetview.desktop.model.PicsouGuiModel;
import com.budgetview.desktop.notifications.NotificationService;
import com.budgetview.desktop.notifications.standard.StandardMessageNotificationHandler;
import com.budgetview.desktop.plaf.ApplicationLAF;
import com.budgetview.desktop.plaf.PicsouMacLookAndFeel;
import com.budgetview.desktop.printing.PrinterService;
import com.budgetview.desktop.printing.utils.DefaultPrinterService;
import com.budgetview.desktop.startup.AppPaths;
import com.budgetview.desktop.startup.components.AppLogger;
import com.budgetview.desktop.startup.components.OpenFilesHandler;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.startup.components.SingleApplicationInstanceListener;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.upgrade.UpgradeService;
import com.budgetview.desktop.utils.ApplicationColors;
import com.budgetview.desktop.utils.AwtExceptionHandler;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.desktop.utils.MacOSXHooks;
import com.budgetview.shared.encryption.MD5PasswordBasedEncryptor;
import com.budgetview.shared.encryption.PasswordBasedEncryptor;
import com.budgetview.shared.encryption.RedirectPasswordBasedEncryptor;
import com.budgetview.utils.Lang;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.parameters.ConfiguredPropertiesService;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.AddIfNotPresentDirectory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class Application {

  public static final String APPLICATION_VERSION = "5.0";
  public static Long JAR_VERSION = 150L;
  public static final Long BANK_CONFIG_VERSION = 7L;

  public static final String APPNAME = "budgetview";
  private static final String CONFIG = "config";
  private static final Pattern CONFIG_FILTER = Pattern.compile(CONFIG + "[0-9][0-9]*" + "\\.jar");

  public static final String LOG_TO_SOUT = APPNAME + ".log.sout";
  public static final String LOCAL_PREVAYLER_PATH_PROPERTY = APPNAME + ".prevayler.path";
  public static final String LOCAL_DATA_PATH_PROPERTY = APPNAME + ".data.path";
  public static final String LOCAL_CODE_PROPERTY = APPNAME + ".jar.path";
  public static final String DEFAULT_ADDRESS_PROPERTY = APPNAME + ".server.url";
  public static String DELETE_LOCAL_PREVAYLER_PROPERTY = APPNAME + ".prevayler.delete";
  public static String IS_DATA_IN_MEMORY = APPNAME + ".data.in.memory";

  public static final String TODAY = ".today";
  public static String FORCE_DATE = APPNAME + TODAY;

  public static final String DISABLE_BACKUP = APPNAME + ".disable.import";
  public static final String USER_FEEDBACK_DISABLED = APPNAME + ".userFeedback.disabled";

  public static boolean EXIT_ON_DATA_ERROR = true;

  private OpenRequestManager openRequestManager = new OpenRequestManager();
  private SingleApplicationInstanceListener singleInstanceListener;
  private OpenFilesHandler mrjDocumentListener;

  private DefaultDirectory directory = new DefaultDirectory();

  static {
    BasicConfigurator.configure(new NullAppender());
    Logger.getRootLogger().setLevel(Level.FATAL);
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

    try {
      new Application().run(args);
    }
    catch (Exception e) {
      showError(e);
      Log.write("At startup ", e);
      System.exit(-1);
    }
  }

  public static void changeDate() {
    String date = System.getProperty(FORCE_DATE);
    if (date != null) {
      TimeService.setCurrentDate(Dates.parse(date));
    }
  }

  public void run(String... args) throws Exception {
    initEncryption();
    mrjDocumentListener = new OpenFilesHandler() {
      public void run(List<File> files) {
        openRequestManager.openFiles(files);
      }
    };
    MacOSXHooks.addOpenDocumentListener(mrjDocumentListener);

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

    singleInstanceListener = new SingleApplicationInstanceListener(directory, openRequestManager);
    if (singleInstanceListener.findRemoteOrListen() == SingleApplicationInstanceListener.ReturnState.EXIT) {
      Thread.sleep(2000);
      return;
    }
    AppLogger.init();
    clearRepositoryIfNeeded();

    preinitDirectory(directory);
    initDirectory(directory, openRequestManager);

    try {
      final MainWindow mainWindow = new MainWindow(this, getServerAddress(), AppPaths.getCurrentDataPath(), isDataInMemory(), directory);
      mainWindow.show();
    }
    catch (InvalidState e) {
      Log.write("Erreur au lancement", e);
      showMultipleInstanceError(e.getMessage());
    }
    catch (Exception e) {
      showError(e);
      Log.write("Erreur au lancement exit", e);
      System.exit(1);
    }
  }

  private static void showError(Exception e) {
    StringWriter out = new StringWriter();
    e.printStackTrace(new PrintWriter(out));
    JDialog dialog = new JDialog((Frame) null, true);
    dialog.setSize(900, 700);
    dialog.getContentPane()
      .add(new JTextArea(out.toString()));
    dialog.setVisible(true);
  }

  private void initEncryption() {
    Thread thread = new Thread() {
      public void run() {
        try {
          MD5PasswordBasedEncryptor.init();
        }
        catch (Exception e) {
          // Ignore
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  private void showMultipleInstanceError(String message) {
    MessageDialog.show("init.dialog.lock.title", MessageType.ERROR, null, directory, "init.dialog.lock.content", message);
    System.exit(1);
  }

  public static String[] parseLanguage(String... args) {
    List<String> arguments = new ArrayList<String>();
    arguments.addAll(Arrays.asList(args));
    for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); ) {
      String arg = iterator.next();
      if (arg.equals("-l")) {
        iterator.remove();
        if (!iterator.hasNext()) {
          return args;
        }
        String lang = iterator.next();
        iterator.remove();
        Lang.setLang(lang);
        return arguments.toArray(new String[arguments.size()]);
      }
    }
    return args;
  }

  private static String getServerAddress() {
    return getSystemValue(DEFAULT_ADDRESS_PROPERTY, "");
  }

  public static boolean isDataInMemory() {
    return "true".equalsIgnoreCase(System.getProperty(IS_DATA_IN_MEMORY));
  }

  public static void clearRepository() {
    Files.deleteWithSubtree(new File(AppPaths.getDefaultDataPath()));
  }

  public static void clearRepositoryIfNeeded() {
    if ("true".equalsIgnoreCase(System.getProperty(DELETE_LOCAL_PREVAYLER_PROPERTY))) {
      clearRepository();
    }
  }

  public void shutdown() {
    try {
      MacOSXHooks.removeOpenDocumentListener(mrjDocumentListener);
      singleInstanceListener.shutdown();
      if (directory != null) {
        directory.get(ColorService.class).removeAllListeners();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getSystemValue(String propertyName, String defaultPropertyValue) {
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
    initDirectory(directory, new OpenRequestManager());
    return directory;
  }

  private static void initDirectory(Directory directory,
                                    OpenRequestManager openRequestManager) throws IOException {
    AddIfNotPresentDirectory wrapper = new AddIfNotPresentDirectory(directory);
    ApplicationColors.registerColorService(wrapper);
    wrapper.add(OpenRequestManager.class, openRequestManager);
    wrapper.add(OpenRequestManager.class, openRequestManager);
    wrapper.add(ApplicationLAF.initUiService());
    wrapper.add(ApplicationLAF.initLayoutService());
    wrapper.add(ApplicationLAF.initRepeatLayoutService());
    wrapper.add(ConfiguredPropertiesService.class, new ConfiguredPropertiesService());
    wrapper.add(new TimeService());
    wrapper.add(new UpgradeService(directory));
    wrapper.add(DescriptionService.class, new PicsouDescriptionService());
    wrapper.add(GlobModel.class, PicsouGuiModel.get());
    wrapper.add(SelectionService.class, new SelectionService());
    wrapper.add(ImageLocator.class, Gui.IMAGE_LOCATOR);
    wrapper.add(TextLocator.class, Lang.TEXT_LOCATOR);
    wrapper.add(FontLocator.class, Gui.FONT_LOCATOR);
    wrapper.add(PasswordBasedEncryptor.class, new RedirectPasswordBasedEncryptor());
    wrapper.add(createConfigService());
    wrapper.add(new MobileService());
    wrapper.add(new MailService());
    wrapper.add(new CloudService());
    wrapper.add(new BankPluginService());
    wrapper.add(new BankSynchroService());
    wrapper.add(PrinterService.class, new DefaultPrinterService());
    wrapper.add(new AddOnService());

    NotificationService notificationService = new NotificationService();
    notificationService.addHandler(new AccountPositionNotificationHandler());
    notificationService.addHandler(new StandardMessageNotificationHandler());
    wrapper.add(notificationService);
  }

  protected void preinitDirectory(Directory directory) {
  }

  private static UserConfigService createConfigService() {
    Long localConfigVersion;
    String configPath = AppPaths.getBankConfigPath();
    File lastJar = findLastJar(configPath);
    if (lastJar != null) {
      localConfigVersion = extractVersion(lastJar.getName());
    }
    else {
      localConfigVersion = BANK_CONFIG_VERSION;
    }
    return new UserConfigService(APPLICATION_VERSION, JAR_VERSION, localConfigVersion, lastJar);
  }

  private static File findLastJar(String path) {
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

  private static Long extractVersion(String fileName) {
    if (fileName != null && CONFIG_FILTER.matcher(fileName).matches()) {
      return Long.parseLong(fileName.substring(fileName.indexOf(CONFIG) + CONFIG.length(),
                                               fileName.indexOf(".")));
    }
    return null;
  }
}
