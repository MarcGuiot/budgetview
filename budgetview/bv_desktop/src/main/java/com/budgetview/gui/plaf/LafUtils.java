package com.budgetview.gui.plaf;

import javax.swing.*;

public class LafUtils {
  private LafUtils() {
  }

  public static void initUI(UIDefaults defaults, Class buttonClass, String uiName) throws ClassNotFoundException {
    String className = buttonClass.getName();
    defaults.put(uiName, className);
    defaults.put(className, buttonClass);
  }
}
