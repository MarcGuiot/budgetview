package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;

public class TabSplitter extends AbstractSplitter {
  private String title;

  public TabSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
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

  protected ComponentStretch createRawStretch(SplitsContext context) {
    return getSubSplitters()[0].createComponentStretch(context, true);
  }

}