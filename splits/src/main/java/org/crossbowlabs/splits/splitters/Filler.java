package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.Anchor;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.Fill;
import org.crossbowlabs.splits.layout.SwingStretches;

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
