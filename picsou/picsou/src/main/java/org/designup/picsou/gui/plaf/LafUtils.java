package org.designup.picsou.gui.plaf;

import javax.swing.*;

public class LafUtils {
  private LafUtils() {
  }

  public static void initUI(UIDefaults defaults, String className, String uiName) throws ClassNotFoundException {
    Class buttonClass = Class.forName(className);
    defaults.put(uiName, className);
    defaults.put(className, buttonClass);
  }
}
