package org.globsframework.gui.views;

import org.globsframework.model.Glob;

import javax.swing.*;

public interface LabelCustomizer {
  void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column);

  static final LabelCustomizer NULL = new LabelCustomizer() {
    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    }
  };
}
