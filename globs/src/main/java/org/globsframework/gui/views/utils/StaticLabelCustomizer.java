package org.globsframework.gui.views.utils;

import org.globsframework.gui.views.LabelCustomizer;

import javax.swing.*;

public abstract class StaticLabelCustomizer implements LabelCustomizer {
  public void process(JLabel label) {
    process(label, null, false, false, 0, 0);
  }
}
