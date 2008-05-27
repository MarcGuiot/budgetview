package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;

import javax.swing.*;

public class LabelComponent extends DefaultComponent<JLabel> {
  public LabelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JLabel.class, "label", context, properties, subSplitters, false);
  }

  protected void processComponent(JLabel label, SplitProperties properties, SplitsContext context) {
    label.setOpaque(false);
  }
}