package org.uispec4j.interception.ui;

import org.uispec4j.interception.toolkit.Empty;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextPaneUI;
import java.awt.*;

///CLOVER:OFF

public class UISpecTextPaneUI extends BasicTextPaneUI {
  public static ComponentUI createUI(JComponent component) {
    return new UISpecTextPaneUI();
  }

  protected void paintSafely(Graphics g) {
  }

  protected void paintBackground(Graphics g) {
  }

  protected void maybeUpdateLayoutState() {
  }

  public Dimension getPreferredSize(JComponent c) {
    return Empty.NULL_DIMENSION;
  }

  public Dimension getMaximumSize(JComponent c) {
    return Empty.NULL_DIMENSION;
  }

  public Dimension getMinimumSize(JComponent c) {
    return Empty.NULL_DIMENSION;
  }
}
