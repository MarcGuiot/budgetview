package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.Anchor;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.Fill;
import org.crossbowlabs.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.awt.*;

public class PanelComponent extends AbstractPanelComponent<JPanel> {
  protected PanelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JPanel.class, "panel", properties, subSplitters, context);
  }
}