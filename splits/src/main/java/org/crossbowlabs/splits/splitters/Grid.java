package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.GridBagBuilder;
import org.crossbowlabs.splits.layout.GridPos;
import org.crossbowlabs.splits.utils.DoubleOperation;

public class Grid extends AbstractSplitter {
  static final String[] DEFAULT_GRIDBAG_PROPERTIES = {"defaultFill", "defaultAnchor", "defaultMargin",
                                                      "defaultMarginTop", "defaultMarginBottom",
                                                      "defaultMarginLeft", "defaultMarginRight"};

  protected Grid(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters, context);
  }

  protected ComponentStretch createRawStretch() {
    GridBagBuilder builder = GridBagBuilder.init().setOpaque(false);
    for (Splitter splitter : getSubSplitters()) {
      ComponentStretch stretch = splitter.getComponentStretch(false);
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
    }
    return createContainerStretch(builder.getPanel(), getSubSplitters(), DoubleOperation.SUM);
  }

  protected String[] getExcludedParameters() {
    return DEFAULT_GRIDBAG_PROPERTIES;
  }

  public String getName() {
    return "grid";
  }
}
