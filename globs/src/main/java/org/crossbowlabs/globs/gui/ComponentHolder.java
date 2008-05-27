package org.crossbowlabs.globs.gui;

import javax.swing.*;

public interface ComponentHolder {
  public JComponent getComponent();

  void dispose();
}
