package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;
import java.awt.*;

public class Filler extends AbstractSplitter {

  private Fill fill = Fill.BOTH;

  public Filler(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters, context);
    String direction = properties.getString("fill");
    if ((direction != null) && (direction.length() > 0)) {
      fill = getFill(direction);
    }
  }

  private Fill getFill(String direction) {
    Fill fill = null;
    try {
      fill = Fill.valueOf(direction.trim().toUpperCase());
    }
    catch (IllegalArgumentException e) {
      throw new SplitsException("Unknown direction for filler: " + direction);
    }
    return fill;
  }

  public ComponentStretch createRawStretch() {
    Component component;
    double weightx;
    double weighty;

    switch (fill) {
      case BOTH:
        component = Box.createGlue();
        weightx = SwingStretches.LARGE_WEIGHT;
        weighty = SwingStretches.LARGE_WEIGHT;
        break;
      case HORIZONTAL:
        component = Box.createHorizontalGlue();
        weightx = SwingStretches.LARGE_WEIGHT;
        weighty = SwingStretches.NULL_WEIGHT;
        break;
      case VERTICAL:
        component = Box.createVerticalGlue();
        weightx = SwingStretches.NULL_WEIGHT;
        weighty = SwingStretches.LARGE_WEIGHT;
        break;
      case NONE:
        component = Box.createGlue();
        weightx = SwingStretches.NULL_WEIGHT;
        weighty = SwingStretches.NULL_WEIGHT;
        break;
      default:
        throw new SplitsException("Unknown fill type: " + fill);
    }

    return new ComponentStretch(component, fill, Anchor.CENTER, weightx, weighty);
  }

  public String getName() {
    return "filler";
  }
}
