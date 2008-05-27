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

public class StyledPanelComponent extends DefaultComponent<JStyledPanel> {
  protected StyledPanelComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JStyledPanel.class, "styledPanel", context, properties, subSplitters, true);
    if (subSplitters.length > 1) {
      throw new SplitsException("styledPanel components cannot have more than one subcomponent");

    }
    if (subSplitters.length == 1) {
      ComponentStretch stretch = subSplitters[0].getComponentStretch(true);
      Component component = stretch.getComponent();
      if (component instanceof JPanel) {
        ((JPanel)component).setOpaque(false);
      }
      GridBagBuilder
        .init(this.component)
        .add(component,
             0, 0, 1, 1, 1.0, 1.0,
             Fill.BOTH, Anchor.CENTER,
             stretch.getInsets());
    }
  }
}
