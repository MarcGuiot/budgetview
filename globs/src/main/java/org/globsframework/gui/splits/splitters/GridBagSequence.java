package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;

public class GridBagSequence extends Sequence {

  public GridBagSequence(Splitter[] subSplitters, Direction direction, SplitProperties properties) {
    super(properties, subSplitters, direction);
  }

  protected SequenceBuilder getSequenceBuilder() {
    return new GridBagSequenceBuilder();
  }

  public String getName() {
    switch (direction) {
      case HORIZONTAL:
        return "row";
      case VERTICAL:
        return "column";
    }
    throw new InvalidParameter("Invalid direction " +  direction);
  }

  public static SplitComponent createPanel(Splitter[] subSplitters, Direction direction, SplitsContext context, String ref) {
    return createPanel(new GridBagSequenceBuilder(), subSplitters, direction, context, ref);
  }

  public static class GridBagSequenceBuilder implements SequenceBuilder {
    private GridBagBuilder builder;

    public void init(JPanel panel, Direction direction) {
      builder = GridBagBuilder.init(panel).setOpaque(false);
    }

    public void add(ComponentConstraints constraints, Direction direction, int position) {
      builder.add(constraints.getComponent(),
                  direction == Direction.HORIZONTAL ? position : 0,
                  direction == Direction.VERTICAL ? position : 0,
                  1, 1,
                  constraints.getWeightX(), constraints.getWeightY(),
                  constraints.getFill(), constraints.getAnchor(),
                  null);
    }
  }
}
