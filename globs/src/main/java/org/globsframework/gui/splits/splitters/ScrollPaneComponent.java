package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.utils.BackgroundColorUpdater;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;

public class ScrollPaneComponent extends AbstractSplitter {

  protected ScrollPaneComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    if (subSplitters.length != 1) {
      throw new SplitsException("scrollPane must have exactly one subcomponent");
    }
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    ComponentStretch subStretch = getSubSplitters()[0].createComponentStretch(context, true);
    JScrollPane scrollPane = new JScrollPane(subStretch.getComponent());

    ComponentStretch stretch = new ComponentStretch(scrollPane,
                                                    subStretch.getFill(),
                                                    subStretch.getAnchor(),
                                                    subStretch.getWeightX(),
                                                    subStretch.getWeightY());

    String id = properties.getString("id");
    if (id != null) {
      context.addComponent(id, scrollPane);
    }

    String bg = properties.getString("viewportBackground");
    if (Strings.isNotEmpty(bg)) {
      if (Colors.isHexaString(bg)) {
        scrollPane.getViewport().setBackground(Colors.toColor(bg));
      }
      else {
        ColorUpdater updater = new BackgroundColorUpdater(bg, scrollPane.getViewport());
        updater.install(context.getService(ColorService.class));
        context.addDisposable(updater);
      }
    }

    Boolean viewportOpaque = properties.getBoolean("viewportOpaque");
    if (viewportOpaque != null) {
      scrollPane.getViewport().setOpaque(viewportOpaque);
    }

    Integer verticalUnitIncrement = properties.getInt("verticalUnitIncrement");
    if (verticalUnitIncrement != null) {
      scrollPane.getVerticalScrollBar().setUnitIncrement(verticalUnitIncrement);
    }

    Integer horizontalUnitIncrement = properties.getInt("horizontalUnitIncrement");
    if (horizontalUnitIncrement != null) {
      scrollPane.getHorizontalScrollBar().setUnitIncrement(horizontalUnitIncrement);
    }

    return stretch;
  }

  public String getName() {
    return "scrollPane";
  }

  protected String[] getExcludedParameters() {
    return new String[]{"viewportBackground", "viewportOpaque", "verticalUnitIncrement", "horizontalUnitIncrement"};
  }
}
