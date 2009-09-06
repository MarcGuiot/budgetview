package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitHandler;
import org.globsframework.gui.splits.impl.DefaultSplitHandler;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.utils.DoubleOperation;

import javax.swing.*;
import java.awt.*;

public abstract class Sequence extends AbstractSplitter {
  protected Direction direction;

  public Sequence(SplitProperties properties, Splitter[] subSplitters, Direction direction) {
    super(properties, subSplitters);
    this.direction = direction;
  }

  public interface SequenceBuilder {
    void init(JPanel panel, Direction direction);

    void add(ComponentStretch stretch, Direction direction, int position);
  }

  protected abstract SequenceBuilder getSequenceBuilder();

  public SplitComponent createRawStretch(SplitsContext context) {
    return createPanel(getSequenceBuilder(), getSubSplitters(), direction, context, properties.get("ref"));
  }

  protected static SplitComponent createPanel(SequenceBuilder builder, Splitter[] subSplitters, Direction direction, SplitsContext context, String ref) {
    SplitHandler splitHandler = getPanel(ref, context);
    JPanel panel = (JPanel)splitHandler.getComponent();
    builder.init(panel, direction);

    double weightX = 0.0;
    double weightY = 0.0;

    int position = 0;
    for (Splitter splitter : subSplitters) {
      SplitComponent splitComponent = splitter.createComponentStretch(context, true);
      ComponentStretch stretch = splitComponent.componentStretch;
      builder.add(stretch, direction, position++);
      weightX = direction.weightXOperation.get(weightX, stretch.getWeightX());
      weightY = direction.weightYOperation.get(weightY, stretch.getWeightY());
    }
    return new SplitComponent(new ComponentStretch(panel, Fill.BOTH, Anchor.CENTER, weightX, weightY),
                              splitHandler);
  }

  private static SplitHandler getPanel(String ref, SplitsContext context) {
    if (ref == null) {
      return new DefaultSplitHandler(new JPanel(), context);
    }
    SplitHandler component = context.findComponent(ref);
    if (component == null) {
      throw new SplitsException("Referenced component '" + ref + "' not found");
    }
    if (!(component.getComponent() instanceof JPanel)) {
      throw new SplitsException("Referenced component '" + ref + "' must be a JPanel");
    }
    return component;
  }

  protected String[] getExcludedParameters() {
    return Grid.DEFAULT_GRIDBAG_PROPERTIES;
  }

  public enum Direction {
    HORIZONTAL(DoubleOperation.SUM, DoubleOperation.MAX),
    VERTICAL(DoubleOperation.MAX, DoubleOperation.SUM);

    private DoubleOperation weightXOperation;
    private DoubleOperation weightYOperation;

    Direction(DoubleOperation weightXOperation, DoubleOperation weightYOperation) {
      this.weightXOperation = weightXOperation;
      this.weightYOperation = weightYOperation;
    }
  }
}
