package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.Anchor;
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
    new HashSet<String>(Arrays.asList("center", "west", "east", "north", "south"));

  public BorderLayoutComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
  }

  public SplitComponent createRawStretch(SplitsContext context) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BorderLayout());


    double weightX = 0;
    double weightY = 0;
    for (Splitter splitter : getSubSplitters()) {
      String pos = splitter.getProperties().get(BORDER_POS);
      if ((pos == null) || (!BORDER_POS_VALUES.contains(pos))) {
        throw new SplitsException(getBorderPosErrorMessage(splitter));
      }
      SplitComponent splitComponent = splitter.createComponentStretch(context, false);
      weightX = DoubleOperation.SUM.get(splitComponent.componentConstraints.getWeightX(), weightX);
      weightY = DoubleOperation.SUM.get(splitComponent.componentConstraints.getWeightY(), weightY);
      panel.add(splitComponent.componentConstraints.getComponent(), SplitsUtils.capitalize(pos));
    }
    return new SplitComponent(new ComponentConstraints(panel, Fill.BOTH, Anchor.CENTER, weightX, weightY),
                                 new DefaultSplitsNode(panel, context));
  }

  private String getBorderPosErrorMessage(Splitter splitter) {
    return "Element '" + splitter.getName() + "' in " + getName() + " must have a property '" + BORDER_POS +
           "' set to [center|north|south|east|west]";
  }

  public String getName() {
    return "borderLayout";
  }
}