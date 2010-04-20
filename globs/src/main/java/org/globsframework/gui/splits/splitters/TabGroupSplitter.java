package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
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
    SplitsNode<JTabbedPane> component = findOrCreateComponent(context);

    for (Splitter splitter : getSubSplitters()) {
      TabSplitter tab = (TabSplitter)splitter;
      component.getComponent().add(tab.getTitle(), tab.createComponentStretch(context, true).componentConstraints.getComponent());
    }
    return new SplitComponent(SwingStretches.get(component.getComponent()), component);
  }

  protected SplitsNode<JTabbedPane> findOrCreateComponent(SplitsContext context) {
    String ref = properties.getString("ref");
    String componentName = properties.getString("name");
    return context.findOrCreateComponent(ref, componentName, JTabbedPane.class, getName());
  }

}
