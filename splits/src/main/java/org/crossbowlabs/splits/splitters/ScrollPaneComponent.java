package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.color.BackgroundColorUpdater;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;

import javax.swing.*;

public class ScrollPaneComponent extends AbstractSplitter {
  private ComponentStretch stretch;

  protected ScrollPaneComponent(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters, context);
    if (subSplitters.length != 1) {
      throw new SplitsException("scrollPane must have exactly one subcomponent");
    }
    ComponentStretch subStretch = subSplitters[0].getComponentStretch(true);
    JScrollPane scrollPane = new JScrollPane(subStretch.getComponent());
    stretch = new ComponentStretch(scrollPane,
                                   subStretch.getFill(),
                                   subStretch.getAnchor(),
                                   subStretch.getWeightX(),
                                   subStretch.getWeightY());

    String id = properties.getString("id");
    if (id != null) {
      context.addComponent(id, scrollPane);
    }

    String bg = properties.getString("viewportBackground");
    if (bg != null) {
      context.getColorService().install(bg, new BackgroundColorUpdater(scrollPane.getViewport()));
    }

    Boolean viewportOpaque = properties.getBoolean("viewportOpaque");
    if (viewportOpaque != null) {
      scrollPane.getViewport().setOpaque(viewportOpaque);
    }
  }

  protected ComponentStretch createRawStretch() {
    return stretch;
  }

  public String getName() {
    return "scrollPane";
  }

  protected String[] getExcludedParameters() {
    return new String[]{"viewportBackground", "viewportOpaque"};
  }
}
