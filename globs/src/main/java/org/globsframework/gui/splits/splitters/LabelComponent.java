package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;

import javax.swing.*;

public class LabelComponent extends DefaultComponent<JLabel> {
  public LabelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JLabel.class, "label", context, properties, subSplitters, false);
  }

  protected void processComponent(JLabel label, SplitProperties properties, SplitsContext context) {
    label.setOpaque(false);
  }
}