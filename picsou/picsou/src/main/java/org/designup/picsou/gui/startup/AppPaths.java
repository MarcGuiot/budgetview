package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.server.persistence.direct.DirectAccountDataManager;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Files;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

public class AppPaths {

  private static final String JAR_DIRECTORY = File.separator + "jars";
  private static final String BANK_CONFIG_DIRECTORY = File.separator + "configs";
  private static final String BUDGET_VIEW = File.separator + "BudgetView";

  public static String getDefaultDataPath() {
    if (System.getProperty(PicsouApplication.LOCAL_DATA_PATH_PROPERTY) != null) {
      return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_DATA_PATH_PROPERTY, System.getProperty("user.home") + "/.budgetview");
    }
    if (System.getProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY) != null) {
      return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.budgetview");
    }
    if (GuiUtils.isMacOSX()) {
      return System.getProperty("user.home") + "/Library/Application Support/BudgetView";
    }
    if (GuiUtils.isWindows()) {
      String windowsXPPath = System.getProperty("user.home") + File.separator + "Application Data";
      String windowsVistaPath = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local";

      if (new File(windowsXPPath + BUDGET_VIEW + JAR_DIRECTORY).exists()) {
        return windowsXPPath + BUDGET_VIEW;
      }
      if (new File(windowsVistaPath + BUDGET_VIEW + JAR_DIRECTORY).exists()) {
        return windowsVistaPath + BUDGET_VIEW;
      }
      if (new File(windowsVistaPath).exists()) {
        return windowsVistaPath + BUDGET_VIEW;
      }
      if (new File(windowsXPPath).exists()) {
        return windowsXPPath + BUDGET_VIEW;
      }
      if (GuiUtils.isVista() || GuiUtils.isWin7() || GuiUtils.isWin8()) {
        return System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + BUDGET_VIEW;
      }
      if (GuiUtils.isXP()) {
        return System.getProperty("user.home") + File.separator + "Application Data" + BUDGET_VIEW;
      }
    }
    return System.getProperty("user.home") + "/.budgetview";
  }

  public static String getCodePath() {
    if (System.getProperty(PicsouApplication.LOCAL_CODE_PROPERTY) == null) {
      return getDefaultDataPath();
    }
    return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_CODE_PROPERTY, System.getProperty("user.home") + "/.budgetview");
  }

  public static boolean isNewDataDir(String path) {
    return !Utils.equal(getCurrentStoragePath(), path);
  }

  public static boolean isEmptyDataDir(String path) {
    File dir = new File(path, "data");
    return !dir.exists() || dir.list().length == 0;
  }

  public static boolean moveDataDirTo(String newPath, Ref<String> message, boolean overwrite) {
    File newPathDir = new File(newPath, "data");
    if (!newPathDir.exists()) {
      if (!newPathDir.mkdirs()) {
        message.set(Lang.get("data.path.create.dir.fail", newPathDir.getAbsolutePath()));
        return false;
      }
    }

    boolean copyData = true;
    File[] files = newPathDir.listFiles();
    if (!Utils.isEmpty(files)) {
      if (overwrite) {
        if (!Files.deleteSubtreeOnly(newPathDir)) {
          message.set(Lang.get("data.path.delete.fail", newPathDir.getAbsolutePath()));
          return false;
        }
        copyData = true;
      }
      else {
        copyData = false;
      }
    }

    if (copyData) {
      String previousDataPath = getCurrentDataPath();
      File previousDataDir = new File(previousDataPath);
      if (previousDataDir.exists() && previousDataDir.isDirectory()) {
        if (!Files.copyDirectory(previousDataDir, newPathDir,
                                 Collections.singleton(DirectAccountDataManager.LOCK_FILE_NAME))) {
          message.set(Lang.get("data.path.copy.fail", previousDataDir.getAbsolutePath(), newPathDir.getAbsolutePath()));
          return false;
        }
      }
    }

    AppPaths.updateRedirect(newPath, message);
    return true;
  }

  public static String getRedirect() {
    File file = new File(getCodePath(), "redirect.txt");
    if (file.exists()) {
      return Files.loadFileToString(file).trim();
    }
    return null;
  }

  public static boolean updateRedirect(String path, Ref<String> message) {
    File file = new File(getCodePath(), "redirect.txt");
    if (isDefaultDataPath(path)) {
      if (file.exists()) {
        if (!file.delete()) {
          message.set(Lang.get("data.path.redirect.delete.fail", file.getAbsolutePath()));
          return false;
        }
      }
      return true;
    }

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

  public static boolean isDefaultDataPath(String path) {
    return Strings.isNullOrEmpty(path) || path.trim().equals(getDefaultDataPath());
  }

  public static String getCurrentDataPath() {
    return getCurrentStoragePath() + "/data";
  }

  public static String getCurrentStoragePath() {
    File file = new File(getCodePath(), "redirect.txt");
    if (file.exists()) {
      String newDataPath = Files.loadFileToString(file).trim();
      if (Strings.isNotEmpty(newDataPath)) {
        File newFilePath = new File(newDataPath);
        if (!newFilePath.exists()) {
          newFilePath.mkdirs();
        }
        if (newFilePath.exists() && newFilePath.isDirectory()) {
          return newFilePath.getAbsolutePath();
        }
      }
    }
    return getDefaultDataPath();
  }

  public static String getBankConfigPath() {
    return getCodePath() + BANK_CONFIG_DIRECTORY;
  }

  public static String getJarPath() {
    return getCodePath() + JAR_DIRECTORY;
  }
}
