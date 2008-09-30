package org.globsframework.gui.splits.splitters;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
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
    return new String[]{"dividerLocation"};
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    JideSplitPane component = findOrCreateComponent(context);
    double weightX = 0.;
    double weightY = 0.;
    for (Splitter splitter : getSubSplitters()) {
      ComponentStretch stretch = splitter.createComponentStretch(context, true);
      component.addPane(stretch.getComponent());
      weightX += stretch.getWeightX();
      weightY += stretch.getWeightY();
    }
    component.setOrientation(direction.value);
    component.setBorder(null);

    setDividerProperties(component);
    return new ComponentStretch(component, Fill.BOTH, Anchor.CENTER, weightX, weightY);
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
