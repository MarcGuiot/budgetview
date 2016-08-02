package com.budgetview.desktop.plaf;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class PicsouWindowsLabelUI extends BasicLabelUI {
  private static final PicsouWindowsLabelUI LABEL_UI = new PicsouWindowsLabelUI();

  public static ComponentUI createUI(JComponent c) {
    return LABEL_UI;
  }

  public void paint(Graphics g, JComponent c) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    super.paint(g, c);
  }
}
