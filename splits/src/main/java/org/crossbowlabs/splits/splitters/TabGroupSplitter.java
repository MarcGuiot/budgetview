package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.SwingStretches;

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
