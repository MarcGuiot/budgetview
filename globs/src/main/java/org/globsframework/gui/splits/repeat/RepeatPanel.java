package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.SwingStretches;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;

public class RepeatPanel implements Repeat {
  private ComponentConstraints constraints;
  private JPanel panel = new JPanel();
  private RepeatHandler repeatHandler;
  private boolean autoHideIfEmpty;
  private Splitter[] splitterTemplates;
  private SplitsContext context;
  private List<RepeatContext> repeatContexts = new ArrayList<RepeatContext>();
  private RepeatLayout layout;
  private DefaultSplitsNode<Component> splitsNode;

  public RepeatPanel(String name, RepeatHandler repeatHandler, RepeatLayout layout,
                     boolean autoHideIfEmpty, Splitter[] splitterTemplates, SplitsContext context) {
    this.repeatHandler = repeatHandler;
    this.autoHideIfEmpty = autoHideIfEmpty;
    this.splitterTemplates = splitterTemplates;
    this.context = context;
    this.layout = layout;
    repeatHandler.register(this);
    this.layout.init(panel);
    this.panel.setName(name);
    this.panel.setOpaque(false);
    this.splitsNode = new DefaultSplitsNode<Component>(panel, context);
    this.context.addComponent(name, splitsNode);
    set(repeatHandler.getInitialItems());
    this.constraints = SwingStretches.get(panel);
    updateVisibility();
  }

  private void updateVisibility() {
    this.panel.setVisible(!autoHideIfEmpty || (panel.getComponentCount() > 0));
  }

  public void set(List items) {
    for (RepeatContext context : repeatContexts) {
      context.dispose();
    }
    repeatContexts.clear();

    List<ComponentConstraints[]> constraints = new ArrayList<ComponentConstraints[]>();
    int index = 0;
    for (Object item : items) {
      constraints.add(createStretches(item, index));
      index++;
    }

    layout.set(panel, constraints);
    panel.revalidate();
    updateVisibility();
  }

  public void insert(Object item, int index) {
    layout.insert(panel, createStretches(item, index), index);
    panel.revalidate();
    updateVisibility();
  }

  public void remove(int index) {
    layout.remove(panel, index);
    RepeatContext context = repeatContexts.remove(index);
    context.dispose();
    panel.revalidate();
    panel.repaint();
    updateVisibility();
  }

  public void move(int previousIndex, int newIndex) {
    layout.move(panel, previousIndex, newIndex);
    RepeatContext context = repeatContexts.remove(previousIndex);
    repeatContexts.add(newIndex, context);
    panel.revalidate();
  }

  private ComponentConstraints[] createStretches(Object item, int index) {
    RepeatContext repeatContext = new RepeatContext(context);
    repeatContexts.add(index, repeatContext);
    repeatHandler.getFactory().registerComponents(new ContextualRepeatCellBuilder(repeatContext), item);

    ComponentConstraints[] constraints = new ComponentConstraints[splitterTemplates.length];
    for (int i = 0; i < splitterTemplates.length; i++) {
      constraints[i] = splitterTemplates[i].createComponentStretch(repeatContext, !layout.managesInsets()).componentConstraints;
    }

    repeatContext.complete();

    return constraints;
  }

  public Splitter.SplitComponent getSplitComponent() {
    return new Splitter.SplitComponent(constraints, splitsNode);
  }

  public void dispose() {
    for (RepeatContext repeatContext : repeatContexts) {
      repeatContext.dispose();
    }
    panel.removeAll();
  }
}
