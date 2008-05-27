package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.utils.DoubleOperation;
import org.crossbowlabs.splits.utils.SplitsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BorderLayoutComponent extends AbstractSplitter {
  private static final String BORDER_POS = "borderPos";
  private static final Set<String> BORDER_POS_VALUES =
    new HashSet(Arrays.asList("center", "west", "east", "north", "south"));

  public BorderLayoutComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters, context);
  }

  public ComponentStretch createRawStretch() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BorderLayout());

    Splitter[] subSplitters = getSubSplitters();
    for (Splitter splitter : subSplitters) {
      String pos = splitter.getProperties().get(BORDER_POS);
      if ((pos == null) || (!BORDER_POS_VALUES.contains(pos))) {
        throw new SplitsException(getBorderPosErrorMessage(splitter));
      }
      panel.add(splitter.getComponentStretch(false).getComponent(), SplitsUtils.capitalize(pos));
    }
    return createContainerStretch(panel, subSplitters, DoubleOperation.SUM);
  }

  private String getBorderPosErrorMessage(Splitter splitter) {
    return "Element '" + splitter.getName() + "' in " + getName() + " must have a property '" + BORDER_POS +
           "' set to [center|north|south|east|west]";
  }

  public String getName() {
    return "borderLayout";
  }
}