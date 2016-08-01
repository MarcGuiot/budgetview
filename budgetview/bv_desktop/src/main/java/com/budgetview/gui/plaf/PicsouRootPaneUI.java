package com.budgetview.gui.plaf;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicRootPaneUI;

public class PicsouRootPaneUI extends BasicRootPaneUI {
  private static final PicsouRootPaneUI ROOT_PANE_UI = new PicsouRootPaneUI();

  public static ComponentUI createUI(JComponent c) {
    return ROOT_PANE_UI;
  }

  protected void installDefaults(JRootPane c) {
    super.installDefaults(c);
    c.setWindowDecorationStyle(JRootPane.NONE);
  }
}
