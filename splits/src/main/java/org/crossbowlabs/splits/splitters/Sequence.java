package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.layout.Anchor;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.Fill;
import org.crossbowlabs.splits.layout.GridBagBuilder;
import org.crossbowlabs.splits.utils.DoubleOperation;

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

  public Sequence(Splitter[] subSplitters, Direction direction, SplitProperties properties, SplitsContext context) {
    super(properties, subSplitters, context);
    this.direction = direction;
  }

  public ComponentStretch createRawStretch() {
    return createPanel(getSubSplitters(), direction);
  }

  public static ComponentStretch createPanel(Splitter[] subSplitters, Direction direction) {
    GridBagBuilder builder = GridBagBuilder.init().setOpaque(false);

    double weightX = 0.0;
    double weightY = 0.0;

    int position = 0;
    for (Splitter splitter : subSplitters) {
      ComponentStretch stretch = splitter.getComponentStretch(true);
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
