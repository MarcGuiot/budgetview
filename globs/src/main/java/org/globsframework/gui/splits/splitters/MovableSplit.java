package org.globsframework.gui.splits.splitters;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.Fill;

import java.awt.*;

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
    return new String[]{"dividerSize", "handleColor"};
  }

  public SplitComponent createRawStretch(SplitsContext context) {
    SplitsNode<JideSplitPane> splitsNode = findOrCreateComponent(context);
    JideSplitPane splitPane = splitsNode.getComponent();
    splitPane.setOrientation(direction.value);
    splitPane.setBorder(null);

    Splitter[] subSplitters = getSubSplitters();
    double totalWeightX = 0.0;
    double totalWeightY = 0.0;
    double[] weights = new double[subSplitters.length];
    int index = 0;
    for (Splitter splitter : subSplitters) {
      ComponentConstraints constraints = splitter.createComponentStretch(context, true).componentConstraints;
      splitPane.addPane(constraints.getComponent());
      totalWeightX += constraints.getWeightX();
      totalWeightY += constraints.getWeightY();
      switch (direction) {
        case HORIZONTAL:
          weights[index++] = constraints.getWeightX();
          break;
        case VERTICAL:
          weights[index++] = constraints.getWeightY();
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
    setHandleProperties(splitPane, context);
    return new SplitComponent(new ComponentConstraints(splitPane, Fill.BOTH, Anchor.CENTER, totalWeightX, totalWeightY),
                              splitsNode);
  }

  private void setDividerProperties(JideSplitPane splitPane) {
    Integer size = getProperties().getInt("dividerSize");
    if (size != null) {
      splitPane.setDividerSize(size);
    }
  }


  private void setHandleProperties(final JideSplitPane splitPane, SplitsContext context) {
    final String handleColor = getProperties().getString("handleColor");

    if (handleColor != null) {
      if (Colors.isHexaString(handleColor)) {
        splitPane.putClientProperty("handleColor", Colors.toColor(handleColor));
      }
      else if (handleColor.length() == 0) {
        splitPane.putClientProperty("handleColor", null);
      }
      else {
        ColorUpdater updater = new ColorUpdater(handleColor) {
          public void updateColor(Color color) {
            splitPane.putClientProperty("handleColor", color);
          }
        };
        updater.install(context.getService(ColorService.class));
        context.addDisposable(updater);
      }
    }  }

  public String getName() {
    return direction.name;
  }
}
