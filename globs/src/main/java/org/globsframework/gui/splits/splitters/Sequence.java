package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.DoubleOperation;

public class Sequence extends AbstractSplitter {
  private Direction direction;

  public enum Direction {
    HORIZONTAL("row", DoubleOperation.SUM, DoubleOperation.MAX),
    VERTICAL("column", DoubleOperation.MAX, DoubleOperation.SUM);

    private String name;
    private DoubleOperation weightXOperation;
    private DoubleOperation weightYOperation;

    Direction(String name, DoubleOperation weightXOperation, DoubleOperation weightYOperation) {
      this.name = name;
      this.weightXOperation = weightXOperation;
      this.weightYOperation = weightYOperation;
    }
  }

  public Sequence(Splitter[] subSplitters, Direction direction, SplitProperties properties) {
    super(properties, subSplitters);
    this.direction = direction;
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    return createPanel(getSubSplitters(), direction, context);
  }

  public static ComponentStretch createPanel(Splitter[] subSplitters, Direction direction, SplitsContext context) {
    GridBagBuilder builder = GridBagBuilder.init().setOpaque(false);

    double weightX = 0.0;
    double weightY = 0.0;

    int position = 0;
    for (Splitter splitter : subSplitters) {
      ComponentStretch stretch = splitter.createComponentStretch(context, true);
      builder.add(stretch.getComponent(),
                  direction == Direction.HORIZONTAL ? position++ : 0,
                  direction == Direction.VERTICAL ? position++ : 0,
                  1, 1,
                  stretch.getWeightX(), stretch.getWeightY(),
                  stretch.getFill(), stretch.getAnchor(),
                  null);
      weightX = direction.weightXOperation.get(weightX, stretch.getWeightX());
      weightY = direction.weightYOperation.get(weightY, stretch.getWeightY());
    }
    return new ComponentStretch(builder.getPanel(), Fill.BOTH, Anchor.CENTER, weightX, weightY);
  }

  protected String[] getExcludedParameters() {
    return Grid.DEFAULT_GRIDBAG_PROPERTIES;
  }

  public String getName() {
    return direction.name;
  }

}
