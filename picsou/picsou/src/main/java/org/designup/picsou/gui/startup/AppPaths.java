package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Files;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;

import java.io.*;

public class AppPaths {

  private static final String JAR_DIRECTORY = "jars";
  private static final String BANK_CONFIG_DIRECTORY = "configs";

  public static String getRootDataPath() {
    if (System.getProperty(PicsouApplication.LOCAL_DATA_PATH_PROPERTY) == null) {
      if (System.getProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
        if (GuiUtils.isMacOSX()) {
          return System.getProperty("user.home") + "/Library/Application Support/BudgetView";
        }
        else if (GuiUtils.isVista()) {
          return System.getProperty("user.home") + "/AppData/Local/BudgetView";
        }
        else if (GuiUtils.isWindows()) {
          return System.getProperty("user.home") + "/Application Data/BudgetView";
        }
        else {
          return System.getProperty("user.home") + "/.budgetview";
        }
      }
      else {
        return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.budgetview");
      }
    }
    else {
      return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_DATA_PATH_PROPERTY, System.getProperty("user.home") + "/.budgetview");
    }
  }

  public static String getCodePath() {
    if (System.getProperty(PicsouApplication.LOCAL_CODE_PROPERTY) == null) {
      return getRootDataPath();
    }
    return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_CODE_PROPERTY, System.getProperty("user.home") + "/.budgetview");
  }


  public static boolean newRedirect(String path, Ref<String> message) {
    File pathDir = new File(path, "data");
    if (!pathDir.exists()) {
      if (!pathDir.mkdirs()) {
        message.set(Lang.get("data.path.create.dir.fail", pathDir.getAbsolutePath()));
        return false;
      }
    }
    File[] files = pathDir.listFiles();
    if (files == null || files.length == 0) {
      String currentDataPath = getDataPath();
      File currentDataDir = new File(currentDataPath);
      if (currentDataDir.exists() && currentDataDir.isDirectory()) {
        if (!Files.copyDirectory(currentDataDir, pathDir)){
          message.set(Lang.get("data.path.copy.fail", currentDataDir.getAbsolutePath(), pathDir.getAbsolutePath()));
          return false;
        }
        else {
          return true;
        }
      }
    }
    return true;
  }

  public static String getRedirect() {
    File file = new File(getCodePath(), "redirect.txt");
    if (file.exists()){
      return Files.loadFileToString(file).trim();
    }
    return null;
  }

  public static boolean updateRedirect(String path, Ref<String> message) {
    File file = new File(getCodePath(), "redirect.txt");
    if (Strings.isNullOrEmpty(path)) {
      if (file.exists()) {
        boolean delete = file.delete();
        if (!delete) {
          message.set(Lang.get("data.path.redirect.delete.fail", file.getAbsolutePath()));
          return false;
        }
      }
      return true;
    }
    else {
      try {
        Writer writer = new FileWriter(file);
        writer.write(path);
        writer.close();
        return true;
      }
      catch (IOException e) {
        message.set(Lang.get("data.path.redirect.create.fail", file.getAbsolutePath()));
        return false;
      }
    }
  }


  public static String getDataPath() {
    File file = new File(getCodePath(), "redirect.txt");
    if (file.exists()) {
      String newDataPath = Files.loadFileToString(file).trim();
      if (Strings.isNotEmpty(newDataPath)) {
        File newFilePath = new File(newDataPath);
        if (!newFilePath.exists()) {
          newFilePath.mkdirs();
        }
        if (newFilePath.exists() && newFilePath.isDirectory()) {
          return new File(newFilePath, "data").getAbsolutePath();
        }
      }
    }
    String path = getRootDataPath();
    return path + "/data";
  }

  public static String getBankConfigPath() {
    return getCodePath() + "/" + BANK_CONFIG_DIRECTORY;
  }

  public static String getJarPath() {
    return getCodePath() + "/" + JAR_DIRECTORY;
  }
}
