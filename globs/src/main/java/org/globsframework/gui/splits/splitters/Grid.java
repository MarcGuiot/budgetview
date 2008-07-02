package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.*;
import org.globsframework.gui.splits.utils.DoubleOperation;

public class Grid extends AbstractSplitter {
  static final String[] DEFAULT_GRIDBAG_PROPERTIES = {"defaultFill", "defaultAnchor", "defaultMargin",
                                                      "defaultMarginTop", "defaultMarginBottom",
                                                      "defaultMarginLeft", "defaultMarginRight"};

  protected Grid(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    GridBagBuilder builder = GridBagBuilder.init().setOpaque(false);
    double weightX = 0;
    double weightY = 0;
    for (Splitter splitter : getSubSplitters()) {
      ComponentStretch stretch = splitter.createComponentStretch(context, false);
      GridPos gridPos = stretch.getGridPos();
      if (gridPos == null) {
        throw new SplitsException("Grid element '" + splitter.getName() + "' must have a GridPos attribute");
      }
      builder.add(stretch.getComponent(),
                  gridPos.getX(), gridPos.getY(),
                  gridPos.getW(), gridPos.getH(),
                  stretch.getWeightX(), stretch.getWeightY(),
                  stretch.getFill(), stretch.getAnchor(),
                  splitter.getMarginInsets());
      weightX = DoubleOperation.SUM.get(stretch.getWeightX(), weightX);
      weightY = DoubleOperation.SUM.get(stretch.getWeightY(), weightY);
    }
    return new ComponentStretch(builder.getPanel(), Fill.BOTH, Anchor.CENTER, weightX, weightY);
  }

  protected String[] getExcludedParameters() {
    return DEFAULT_GRIDBAG_PROPERTIES;
  }

  public String getName() {
    return "grid";
  }
}
