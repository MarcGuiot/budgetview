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
  private Splitter[] splitterTemplates;
  private SplitsContext context;
  private List<RepeatContext> repeatContexts = new ArrayList<RepeatContext>();
  private RepeatLayout layout;

  public RepeatPanel(String name, RepeatHandler repeatHandler, RepeatLayout layout,
                     Splitter[] splitterTemplates, SplitsContext context) {
    this.repeatHandler = repeatHandler;
    this.splitterTemplates = splitterTemplates;
    this.context = context;
    this.layout = layout;
    repeatHandler.register(this);
    this.layout.init(panel);
    this.panel.setName(name);
    this.panel.setOpaque(false);
    this.context.addComponent(name, panel);
    set(repeatHandler.getInitialItems());
    this.stretch = SwingStretches.get(panel);
  }

  public void set(List items) {
    for (RepeatContext context : repeatContexts) {
      context.dispose();
    }
    repeatContexts.clear();

    List<ComponentStretch[]> stretches = new ArrayList<ComponentStretch[]>();
    int index = 0;
    for (Object item : items) {
      stretches.add(createStretches(item, index));
      index++;
    }

    layout.set(panel, stretches);
    panel.revalidate();
  }

  public void insert(Object item, int index) {
    layout.insert(panel, createStretches(item, index), index);
    panel.revalidate();
  }

  public void remove(int index) {
    layout.remove(panel, index);
    RepeatContext context = repeatContexts.remove(index);
    context.dispose();
    panel.revalidate();
  }

  public void move(int previousIndex, int newIndex) {
    layout.move(panel, previousIndex, newIndex);
    RepeatContext context = repeatContexts.remove(previousIndex);
    repeatContexts.add(newIndex, context);
    panel.revalidate();
  }

  private ComponentStretch[] createStretches(Object item, int index) {
    RepeatContext repeatContext = new RepeatContext(context);
    repeatContexts.add(index, repeatContext);
    repeatHandler.getFactory().registerComponents(new ContextualRepeatCellBuilder(repeatContext), item);

    ComponentStretch[] stretches = new ComponentStretch[splitterTemplates.length];
    for (int i = 0; i < splitterTemplates.length; i++) {
      stretches[i] = splitterTemplates[i].createComponentStretch(repeatContext, !layout.managesInsets());
    }

    repeatContext.complete();

    return stretches;
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
