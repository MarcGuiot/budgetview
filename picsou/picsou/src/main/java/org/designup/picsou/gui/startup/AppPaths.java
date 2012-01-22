package org.designup.picsou.gui.startup;

import org.designup.picsou.gui.PicsouApplication;
import org.globsframework.gui.splits.utils.GuiUtils;

public class AppPaths {

  private static final String JAR_DIRECTORY = "jars";
  private static final String BANK_CONFIG_DIRECTORY = "configs";

  public static String getDataPath() {
    if (System.getProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY) == null) {
      if (GuiUtils.isMacOSX()) {
        return System.getProperty("user.home") + "/Library/Application Support/BudgetView";
      }
      if (GuiUtils.isVista()) {
        return System.getProperty("user.home") + "/AppData/Local/BudgetView";
      }
      if (GuiUtils.isWindows()) {
        return System.getProperty("user.home") + "/Application Data/BudgetView";
      }
    }
    return PicsouApplication.getSystemValue(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, System.getProperty("user.home") + "/.budgetview");
  }

  public static String getLocalPrevaylerPath() {
    return getDataPath() + "/data";
  }

  public static String getBankConfigPath() {
    return getDataPath() + "/" + BANK_CONFIG_DIRECTORY;
  }

  public static String getJarPath() {
    return getDataPath() + "/" + JAR_DIRECTORY;
  }
}
