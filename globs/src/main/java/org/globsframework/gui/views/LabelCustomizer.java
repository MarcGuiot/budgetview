package org.globsframework.gui.views;

import org.globsframework.model.Glob;

import javax.swing.*;

public interface LabelCustomizer {
  void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column);

  public static final LabelCustomizer NO_OP = new LabelCustomizer() {
    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    }
  };
}
