package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;
import java.util.List;

public class RepeatPanel implements RepeatHandler {
  private ComponentStretch stretch;
  private JPanel panel = new JPanel();
  private Repeat repeat;
  private Splitter templateSplitter;
  private SplitsContext context;

  public RepeatPanel(String name, Repeat repeat, Splitter templateSplitter, SplitsContext context) {
    this.repeat = repeat;
    this.templateSplitter = templateSplitter;
    this.context = context;
    repeat.register(this);
    set(repeat.getInitialItems());
    this.stretch = SwingStretches.get(panel);
    this.panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    this.panel.setName(name);
  }

  public void set(List items) {
    panel.removeAll();
    for (Object item : items) {
      add(createStretch(item));
    }
  }

  public void insert(Object item, int index) {
    panel.add(createStretch(item).getComponent(), index);
  }

  public void remove(int index) {
    panel.remove(index);
  }

  public void add(ComponentStretch componentStretch) {
    panel.add(componentStretch.getComponent());
  }

  private ComponentStretch createStretch(Object item) {
    RepeatContext repeatContext = new RepeatContext(context);
    repeat.getFactory().registerComponents(new ContextualRepeatCellBuilder(repeatContext), item);
    return templateSplitter.createComponentStretch(repeatContext, true);
  }

  public ComponentStretch getStretch() {
    return stretch;
  }
}
