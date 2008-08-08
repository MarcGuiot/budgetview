package org.designup.picsou.start;

import sun.security.action.GetPropertyAction;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

public class Main {
  private static final String MAC_PLATFORM_ID = "Mac OS X";
  private static final String LINUX_PLATFORM_ID = "Linux";
  private static final String PICSOU = "picsou";
  private static final Pattern FILTER = Pattern.compile(PICSOU + ".*" + "\\.jar");

  public static void main(String[] args) {
    new Main().go(args);
  }

  private void go(String[] args) {
    String pathToInstallDir = System.getProperty("user.dir");
    File jarFile = findLastJar(pathToInstallDir);
    Long installedVersion = null;
    if (jarFile != null) {
      installedVersion = extractVersion(jarFile.getName());
    }
    File downloadedFileName = loadJar();
    Long downloadedVersion = null;
    if (downloadedFileName != null) {
      downloadedVersion = extractVersion(downloadedFileName.getName());
    }

    if (installedVersion != null && downloadedVersion != null) {
      if (installedVersion > downloadedVersion) {
        jarFile = downloadedFileName;
      }
    }
    if (jarFile == null) {
      jarFile = downloadedFileName;
    }
    if (jarFile == null) {
      throw new RuntimeException("missing jar file");
    }
    URL url;
    try {
      url = new URL("file", "", jarFile.getAbsolutePath());
      ClassLoader parent = getClass().getClassLoader();
      URLClassLoader loader = new URLClassLoader(new URL[]{url}, parent);
      Class<?> applicationClass = loader.loadClass("org.designup.picsou.gui.PicsouApplication");
      Method method = applicationClass.getMethod("main", String[].class);
      method.invoke(null, new Object[]{args});
    }
    catch (Exception e) {
      e.printStackTrace();
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

  private File loadJar() {
    String path = System.getProperty("user.home") + "/." + PICSOU + "/jars";
    if (isMacOSX()) {
      //    path = "";
    }
    File jarFile = findLastJar(path);
    if (jarFile == null) {
      path = System.getProperty("user.home") + "/." + PICSOU + "/jars";
      jarFile = findLastJar(path);
    }
    return jarFile;
  }

  public static boolean isMacOSX() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(MAC_PLATFORM_ID);
  }

  public static boolean isLinux() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(LINUX_PLATFORM_ID);
  }

}
