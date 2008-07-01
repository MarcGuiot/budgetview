package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;

import javax.swing.*;

public class LabelComponent extends DefaultComponent<JLabel> {
  public LabelComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(JLabel.class, "label", properties, subSplitters, false);
  }

  protected void postCreateComponent(JLabel component, SplitsContext context) {
    component.setOpaque(false);
  }
}