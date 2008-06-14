package org.globsframework.gui;

import javax.swing.*;

public interface ComponentHolder {
  public JComponent getComponent();

  void dispose();
}
