package org.globsframework.gui.splits;


import junit.framework.Assert;

import javax.swing.*;

public class DummySplitsNode implements SplitsNode<JPanel> {

  private JPanel panel = new JPanel();
  private String lastStyle;

  public DummySplitsNode() {
  }

  public JPanel getComponent() {
    return panel;
  }

  public void applyStyle(String styleId) {
    this.lastStyle = styleId;
  }

  public void checkLastStyle(String styleId) {
    Assert.assertEquals(styleId, lastStyle);
  }

  public void reapplyStyle() {
  }
}
