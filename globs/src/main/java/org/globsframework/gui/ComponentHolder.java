package org.globsframework.gui;

import org.globsframework.gui.splits.utils.Disposable;

import javax.swing.*;

public interface ComponentHolder extends Disposable {
  public JComponent getComponent();

  ComponentHolder setName(String name);
}
