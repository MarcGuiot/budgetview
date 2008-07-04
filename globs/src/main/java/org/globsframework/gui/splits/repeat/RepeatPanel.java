package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RepeatPanel implements Repeat {
  private ComponentStretch stretch;
  private JPanel panel = new JPanel();
  private RepeatHandler repeatHandler;
  private Splitter templateSplitter;
  private SplitsContext context;
  private List<RepeatContext> repeatContexts = new ArrayList<RepeatContext>();

  public RepeatPanel(String name, RepeatHandler repeatHandler, Splitter templateSplitter, SplitsContext context) {
    this.repeatHandler = repeatHandler;
    this.templateSplitter = templateSplitter;
    this.context = context;
    repeatHandler.register(this);
    set(repeatHandler.getInitialItems());
    this.stretch = SwingStretches.get(panel);
    this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    this.panel.setName(name);
    this.panel.setOpaque(false);
  }

  public void set(List items) {
    panel.removeAll();
    int index = 0;
    for (Object item : items) {
      add(createStretch(item, index));
      index++;
    }
  }

  public void insert(Object item, int index) {
    panel.add(createStretch(item, index).getComponent(), index);
  }

  public void remove(int index) {
    panel.remove(index);
    RepeatContext context = repeatContexts.remove(index);
    context.dispose();
  }

  public void add(ComponentStretch componentStretch) {
    panel.add(componentStretch.getComponent());
  }

  private ComponentStretch createStretch(Object item, int index) {
    RepeatContext repeatContext = new RepeatContext(context);
    repeatContexts.add(index, repeatContext);
    repeatHandler.getFactory().registerComponents(new ContextualRepeatCellBuilder(repeatContext), item);
    return templateSplitter.createComponentStretch(repeatContext, true);
  }

  public ComponentStretch getStretch() {
    return stretch;
  }

  public void dispose() {
    for (RepeatContext repeatContext : repeatContexts) {
      repeatContext.dispose();
    }
    panel.removeAll();
  }
}
