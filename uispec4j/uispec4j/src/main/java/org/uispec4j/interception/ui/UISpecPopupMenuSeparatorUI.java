package org.uispec4j.interception.ui;

import org.uispec4j.interception.toolkit.Empty;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;
import java.awt.*;

///CLOVER:OFF

public class UISpecPopupMenuSeparatorUI extends BasicPopupMenuSeparatorUI {
  public static ComponentUI createUI(JComponent component) {
    return new UISpecPopupMenuSeparatorUI();
  }

  public void paint(Graphics g, JComponent c) {
  }

  protected void maybeUpdateLayoutState() {
  }

  public Dimension getPreferredSize(JComponent c) {
    return new Dimension(Empty.NULL_DIMENSION);
  }

  public Dimension getMaximumSize(JComponent c) {
    return new Dimension(Empty.NULL_DIMENSION);
  }

  public Dimension getMinimumSize(JComponent c) {
    return new Dimension(Empty.NULL_DIMENSION);
  }
}
