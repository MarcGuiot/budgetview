package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.components.JStyledPanel;

public class StyledPanelComponent extends AbstractPanelComponent<JStyledPanel> {
  protected StyledPanelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JStyledPanel.class, "styledPanel", properties, subSplitters, context);
  }
}
