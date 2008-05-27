package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;

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