package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;

public class BoxSequence extends Sequence {

  public BoxSequence(Splitter[] subSplitters, Direction direction, SplitProperties properties) {
    super(properties, subSplitters, direction);
  }

  protected SequenceBuilder getSequenceBuilder() {
    return new BoxSequenceBuilder();
  }

  public String getName() {
    switch (direction) {
      case HORIZONTAL:
        return "horizontalBoxes";
      case VERTICAL:
        return "verticalBoxes";
    }
    throw new InvalidParameter("Invalid direction " +  direction);
  }

  private static class BoxSequenceBuilder implements SequenceBuilder {
    private JPanel panel;

    public void init(JPanel panel, Direction direction) {
      this.panel = panel;
      panel.setOpaque(false);
      panel.setLayout(new BoxLayout(panel, getBoxDirection(direction)));
    }

    public void add(ComponentConstraints constraints, Direction direction, int position) {
      panel.add(constraints.getComponent(), position);
    }

    public int getBoxDirection(Direction direction) {
      switch (direction) {
        case HORIZONTAL:
          return BoxLayout.X_AXIS;
        case VERTICAL:
          return BoxLayout.Y_AXIS;
      }
      throw new RuntimeException("Unexpected direction " + direction);
    }
  }
}