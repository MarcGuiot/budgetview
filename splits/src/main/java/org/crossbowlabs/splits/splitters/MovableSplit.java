package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.Anchor;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.Fill;

import javax.swing.*;

public class MovableSplit extends DefaultComponent<JSplitPane> {
  private Direction direction;

  public enum Direction {
    HORIZONTAL("horizontalSplit", JSplitPane.HORIZONTAL_SPLIT),
    VERTICAL("verticalSplit", JSplitPane.VERTICAL_SPLIT);

    private String name;
    private int value;

    Direction(String name, int dir) {
      this.name = name;
      this.value = dir;
    }
  }

  public MovableSplit(Direction direction, SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(JSplitPane.class, direction.name, context, properties, subSplitters, true);
    this.direction = direction;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"dividerLocation"};
  }

  public ComponentStretch createRawStretch() {
    if (getSubSplitters().length != 2) {
      throw new SplitsException("verticalSplit requires two subcomponents");
    }
    ComponentStretch stretch1 = getSubSplitters()[0].getComponentStretch(true);
    ComponentStretch stretch2 = getSubSplitters()[1].getComponentStretch(true);
    component.setOrientation(direction.value);
    component.setLeftComponent(stretch1.getComponent());
    component.setRightComponent(stretch2.getComponent());
    component.setBorder(null);

    switch (direction) {
      case HORIZONTAL:
        component.setResizeWeight(getRatio(stretch1.getWeightX(), stretch2.getWeightX()));
      case VERTICAL:
        component.setResizeWeight(getRatio(stretch1.getWeightX(), stretch2.getWeightX()));
    }

    setDividerProperties(component);
    return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER,
                                stretch1.getWeightX() + stretch2.getWeightX(),
                                stretch1.getWeightY() + stretch2.getWeightY());
  }

  private double getRatio(double x, double y) {
    if (x + y == 0) {
      return 100;
    }
    return x / (x + y);
  }

  private void setDividerProperties(JSplitPane splitPane) {
    Integer size = getProperties().getInt("dividerSize");
    if (size != null) {
      splitPane.setDividerSize(size);
    }

    Integer location = getProperties().getInt("dividerLocation");
    if (location != null) {
      splitPane.setDividerLocation(location);
    }
  }

  public String getName() {
    return direction.name;
  }
}
