package com.budgetview;

import sun.security.action.GetPropertyAction;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class Main {
  private static final String MAC_PLATFORM_ID = "Mac OS X";
  private static final String WINDOWS_PLATFORM_ID = "Windows";
  private static final String PICSOU = "budgetview";
  private static final Pattern FILTER = Pattern.compile(PICSOU + "[0-9][0-9]*" + "\\.jar");
  private static final String JAR_DIRECTORY = File.separator + "jars";
  public static final String BUDGET_VIEW = File.separator + "BudgetView";

  public static void main(String[] args) throws Exception {
    new Main().go(args);
  }

  private void go(String[] args) throws Exception {

    String pathToInstallDir = System.getProperty("budgetview.exe.dir");
    if (pathToInstallDir == null) {
      if (isMacOSX()) {
        pathToInstallDir = System.getProperty("user.dir") + "/BudgetView.app/Contents/Resources/";
      }
      else {
        pathToInstallDir = System.getProperty("user.dir");
      }
    }

    File jarFile = null;
    Long installedVersion = null;
    if (pathToInstallDir != null) {
      jarFile = findLastJar(pathToInstallDir);
      if (jarFile != null) {
        installedVersion = extractVersion(jarFile.getName());
      }
    }

    File downloadedFileName = loadJar();
    Long downloadedVersion = null;
    if (downloadedFileName != null) {
      downloadedVersion = extractVersion(downloadedFileName.getName());
    }

    if (installedVersion != null && downloadedVersion != null) {
      if (downloadedVersion > installedVersion) {
        jarFile = downloadedFileName;
      }
    }
    if (jarFile == null) {
      jarFile = downloadedFileName;
    }
    if (jarFile == null) {
      dumpLog("JAR file not found", null, pathToInstallDir, jarFile, installedVersion, downloadedVersion);
      throw new RuntimeException();
    }

    URL url;
    try {
      url = new URL("file", "", jarFile.getAbsolutePath());
      ClassLoader parent = getClass().getClassLoader();
      URLClassLoader loader = new URLClassLoader(new URL[]{url}, parent);
      Class<?> applicationClass = loader.loadClass("picsou.Main");
      Method method = applicationClass.getMethod("main", String[].class);
      method.invoke(null, new Object[]{args});
    }
    catch (Exception e) {
      dumpLog(e.getMessage(), e, pathToInstallDir, jarFile, installedVersion, downloadedVersion);
      throw new RuntimeException(e);
    }
  }

  private Long extractVersion(String fileName) {
    if (fileName != null && FILTER.matcher(fileName).matches()) {
      return Long.parseLong(fileName.substring(fileName.indexOf(PICSOU) + PICSOU.length(),
                                               fileName.indexOf(".")));
    }
    return null;
  }

  private File findLastJar(String path) {
    File directory = new File(path);
    File[] files = directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return FILTER.matcher(name).matches();
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

  public static String getCodePath() {
    if (System.getProperty(PICSOU + ".jar.path") == null) {
      return getJarPath();
    }
    return System.getProperty(PICSOU + ".jar.path", System.getProperty("user.home")) + "/." + PICSOU + JAR_DIRECTORY;
  }

  public static String getJarPath() {
    if (isMacOSX()) {
      return System.getProperty("user.home") + "/Library/Application Support/BudgetView" + JAR_DIRECTORY;
    }
    if (isWindows()) {
      String windowsXPPath = System.getProperty("user.home") + "/Application Data";
      String windowsVistaPath = System.getProperty("user.home") + "/AppData/Local";
      if (new File(windowsXPPath + BUDGET_VIEW + JAR_DIRECTORY).exists()) {
        return windowsXPPath + BUDGET_VIEW + JAR_DIRECTORY;
      }
      if (new File(windowsVistaPath + BUDGET_VIEW + JAR_DIRECTORY).exists()) {
        return windowsVistaPath + BUDGET_VIEW + JAR_DIRECTORY;
      }
      if (new File(windowsVistaPath).exists()) {
        return windowsVistaPath + BUDGET_VIEW + JAR_DIRECTORY;
      }
      if (new File(windowsXPPath).exists()) {
        return windowsXPPath + BUDGET_VIEW + JAR_DIRECTORY;
      }
      if (isVista() || isWin7() || isWin8()) {
        return System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + BUDGET_VIEW;
      }
      if (isXP()) {
        return System.getProperty("user.home") + File.separator + "Application Data" + BUDGET_VIEW;
      }
    }
    return System.getProperty("user.home") + "/.budgetview" + JAR_DIRECTORY;
  }

  private File loadJar() {
    String path = getCodePath();
    return findLastJar(path);
  }

  public static boolean isVista() {
    String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    return os.contains(WINDOWS_PLATFORM_ID) && os.toLowerCase().contains("vista");
  }

  public static boolean isWin7() {
    String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    return os.contains(WINDOWS_PLATFORM_ID) && os.toLowerCase().contains("7");
  }

  public static boolean isWin8() {
    String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    return os.contains(WINDOWS_PLATFORM_ID) && os.toLowerCase().contains("8");
  }

  public static boolean isXP() {
    String os = (String)AccessController.doPrivileged(new GetPropertyAction("os.name"));
    return os.contains(WINDOWS_PLATFORM_ID) && os.toLowerCase().contains("xp");
  }

  public static boolean isWindows() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(WINDOWS_PLATFORM_ID);
  }

  public static boolean isMacOSX() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(MAC_PLATFORM_ID);
  }

  private void dumpLog(String message, Exception e,
                       String pathToInstallDir, File jarFile,
                       Long installedVersion, Long downloadedVersion) throws IOException {

    Writer writer = new OutputStreamWriter(System.err);

    dumpTitle(writer, "Variables");

    dumpValue(writer, "errorMessage", message);
    dumpValue(writer, "pathToInstallDir", pathToInstallDir);
    dumpValue(writer, "jarFile", jarFile);
    dumpValue(writer, "installedVersion", installedVersion);
    dumpValue(writer, "downloadedVersion", downloadedVersion);

    if (e != null) {
      dumpTitle(writer, "Exception");
      e.printStackTrace(new PrintWriter(writer));
    }

    dumpTitle(writer, "System properties");

    dumpProperty(writer, "java.vm.vendor");
    dumpProperty(writer, "java.version");
    dumpProperty(writer, "java.vm.version");
    dumpProperty(writer, "java.runtime.version");
    dumpProperty(writer, "java.specification.version");
    dumpProperty(writer, "os.name");
    dumpProperty(writer, "os.version");
    dumpProperty(writer, "os.arch");
    dumpProperty(writer, "sun.arch.data.model");
    dumpProperty(writer, "file.encoding");
    dumpProperty(writer, "user.language");

    writer.close();
  }

  private void dumpTitle(Writer writer, String title) throws IOException {
    writer.write("\n\n==========  " + title + "  ==========\n\n");
  }

  private void dumpProperty(Writer writer, String key) throws IOException {
    dumpValue(writer, key, System.getProperty(key));
  }

  private void dumpValue(Writer writer, String key, Object value) throws IOException {
    writer.write(key + " => " + value + "\n");
  }
}
