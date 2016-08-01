package com.budgetview.gui.components;

import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsNode;

import javax.swing.*;

public class MandatoryFieldFlag {
  private SplitsNode<JLabel> node;

  public MandatoryFieldFlag(String name, SplitsBuilder builder) {
    this.node = builder.add(name, new JLabel());
  }

  public void clear() {
    node.applyStyle("mandatoryFieldFlagHidden");
  }

  public void update(boolean shown) {
    node.applyStyle(shown ? "mandatoryFieldFlagShown" : "mandatoryFieldFlagHidden");
  }
}
