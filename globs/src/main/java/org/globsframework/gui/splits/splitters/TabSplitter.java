package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;

public class TabSplitter extends AbstractSplitter {
  private String title;

  public TabSplitter(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(properties, subSplitters);
    this.title = properties.getString("title", context);
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

  protected SplitComponent createRawStretch(SplitsContext context) {
    return getSubSplitters()[0].createComponentStretch(context, true);
  }

}