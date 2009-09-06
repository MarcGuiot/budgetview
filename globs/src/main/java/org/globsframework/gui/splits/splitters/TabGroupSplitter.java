package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.impl.DefaultSplitHandler;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;

public class TabGroupSplitter extends AbstractSplitter {

  public TabGroupSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    for (Splitter splitter : subSplitters) {
      if (!(splitter instanceof TabSplitter)) {
        throw new SplitsException("Invalid component '" + splitter.getName() + "' found in 'tabs' component - " +
                                  " only 'tab' subcomponents are accepted");
      }
    }
  }

  public String getName() {
    return "tabs";
  }

  protected SplitComponent createRawStretch(SplitsContext context) {
    JTabbedPane tabbedPane = new JTabbedPane();
    for (Splitter splitter : getSubSplitters()) {
      TabSplitter tab = (TabSplitter)splitter;
      tabbedPane.add(tab.getTitle(), tab.createComponentStretch(context, true).componentStretch.getComponent());
    }
    return new SplitComponent(SwingStretches.get(tabbedPane), new DefaultSplitHandler(tabbedPane, context));
  }
}
