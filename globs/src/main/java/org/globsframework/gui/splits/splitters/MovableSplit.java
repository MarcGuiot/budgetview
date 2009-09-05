package org.globsframework.gui.splits.splitters;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitHandler;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;

public class MovableSplit extends DefaultComponent<JideSplitPane> {
  private Direction direction;

  public enum Direction {
    HORIZONTAL("horizontalSplit", JideSplitPane.HORIZONTAL_SPLIT),
    VERTICAL("verticalSplit", JideSplitPane.VERTICAL_SPLIT);

    private String name;
    private int value;

    Direction(String name, int dir) {
      this.name = name;
      this.value = dir;
    }
  }

  public MovableSplit(Direction direction, SplitProperties properties, Splitter[] subSplitters) {
    super(JideSplitPane.class, direction.name, properties, subSplitters, true);
    this.direction = direction;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"dividerSize"};
  }

  public SplitComponent createRawStretch(SplitsContext context) {
    SplitHandler<JideSplitPane> splitHandler = findOrCreateComponent(context);
    JideSplitPane splitPane = splitHandler.getComponent();
    splitPane.setOrientation(direction.value);
    splitPane.setBorder(null);

    Splitter[] subSplitters = getSubSplitters();
    double totalWeightX = 0.0;
    double totalWeightY = 0.0;
    double[] weights = new double[subSplitters.length];
    int index = 0;
    for (Splitter splitter : subSplitters) {
      ComponentStretch stretch = splitter.createComponentStretch(context, true).componentStretch;
      splitPane.addPane(stretch.getComponent());
      totalWeightX += stretch.getWeightX();
      totalWeightY += stretch.getWeightY();
      switch (direction) {
        case HORIZONTAL:
          weights[index++] = stretch.getWeightX();
          break;
        case VERTICAL:
          weights[index++] = stretch.getWeightY();
          break;
      }
    }

    double[] proportions = new double[weights.length - 1];
    switch (direction) {
      case HORIZONTAL:
        if (totalWeightX != 0) {
          for (int i = 0; i < weights.length - 1; i++) {
            proportions[i] = weights[i] / totalWeightX;
          }
        }
        break;
      case VERTICAL:
        if (totalWeightY != 0) {
          for (int i = 0; i < weights.length - 1; i++) {
            proportions[i] = weights[i] / totalWeightY;
          }
        }
    }
    splitPane.setProportionalLayout(true);
    splitPane.setProportions(proportions);

    setDividerProperties(splitPane);
    return new SplitComponent(new ComponentStretch(splitPane, Fill.BOTH, Anchor.CENTER, totalWeightX, totalWeightY),
                              splitHandler);
  }

  private void setDividerProperties(JideSplitPane splitPane) {
    Integer size = getProperties().getInt("dividerSize");
    if (size != null) {
      splitPane.setDividerSize(size);
    }
  }

  public String getName() {
    return direction.name;
  }
}
