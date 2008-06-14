package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;

import javax.swing.*;

public class PanelComponent extends AbstractPanelComponent<JPanel> {
  protected PanelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JPanel.class, "panel", properties, subSplitters, context);
  }
}