package com.budgetview.gui.plaf;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;

public class PicsouOptionPaneUI extends BasicOptionPaneUI {
  private static final PicsouOptionPaneUI OPTION_PANE_UI = new PicsouOptionPaneUI();

  public static ComponentUI createUI(JComponent c) {
    return OPTION_PANE_UI;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    c.setOpaque(false);
  }

  protected Container createMessageArea() {
    JPanel panel = (JPanel) super.createMessageArea();
    GuiUtils.opacify(panel);
    return panel;
  }

  protected Container createButtonArea() {
    JPanel panel = (JPanel) super.createButtonArea();
    panel.setOpaque(false);
    return panel;
  }

  protected boolean getSizeButtonsToSameWidth() {
    return false;
  }
}
