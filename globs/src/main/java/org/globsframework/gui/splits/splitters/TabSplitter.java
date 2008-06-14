package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;

import java.awt.*;

public class TabSplitter extends AbstractSplitter {
  private String title;

  public TabSplitter(SplitsContext context, SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters, context);
    this.title = properties.get("title");
    if (subSplitters.length != 1) {
      throw new SplitsException("Tab component '" + title + "' must have exactly one subcomponent");
    }
  }

  public String getName() {
    return "tab";
  }

  public String getTitle() {
    return title;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"title"};
  }

  protected ComponentStretch createRawStretch() {
    return getSubSplitters()[0].getComponentStretch(true);
  }

  public Component getComponent() {
    return getComponentStretch(true).getComponent();
  }
}