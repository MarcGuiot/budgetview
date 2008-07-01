package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.utils.DoubleOperation;
import org.globsframework.gui.splits.utils.SplitsUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BorderLayoutComponent extends AbstractSplitter {
  private static final String BORDER_POS = "borderPos";
  private static final Set<String> BORDER_POS_VALUES =
    new HashSet(Arrays.asList("center", "west", "east", "north", "south"));

  public BorderLayoutComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BorderLayout());

    Splitter[] subSplitters = getSubSplitters();
    for (Splitter splitter : subSplitters) {
      String pos = splitter.getProperties().get(BORDER_POS);
      if ((pos == null) || (!BORDER_POS_VALUES.contains(pos))) {
        throw new SplitsException(getBorderPosErrorMessage(splitter));
      }
      panel.add(splitter.getComponentStretch(context, false).getComponent(), SplitsUtils.capitalize(pos));
    }
    return createContainerStretch(panel, DoubleOperation.SUM, context);
  }

  private String getBorderPosErrorMessage(Splitter splitter) {
    return "Element '" + splitter.getName() + "' in " + getName() + " must have a property '" + BORDER_POS +
           "' set to [center|north|south|east|west]";
  }

  public String getName() {
    return "borderLayout";
  }
}