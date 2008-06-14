package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.components.JStyledPanel;

public class StyledPanelComponent extends AbstractPanelComponent<JStyledPanel> {
  protected StyledPanelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JStyledPanel.class, "styledPanel", properties, subSplitters, context);
  }
}
