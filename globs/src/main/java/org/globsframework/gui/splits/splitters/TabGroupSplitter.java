package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;

public class TabGroupSplitter extends AbstractSplitter {

  private JTabbedPane tabbedPane = new JTabbedPane();

  public TabGroupSplitter(SplitsContext context, SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters, context);
    for (Splitter splitter : subSplitters) {
      if (!(splitter instanceof TabSplitter)) {
        throw new SplitsException("Invalid component '" + splitter.getName() + "' found in 'tabs' component - " +
                                  " only 'tab' subcomponents are accepted");
      }
      TabSplitter tab = (TabSplitter)splitter;
      tabbedPane.add(tab.getTitle(), tab.getComponent());
    }
  }

  public String getName() {
    return "tabs";
  }

  protected ComponentStretch createRawStretch() {
    return SwingStretches.get(tabbedPane);
  }
}
