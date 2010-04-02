package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.SwingStretches;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RepeatPanel implements Repeat {
  private ComponentConstraints constraints;
  private JPanel panel = new JPanel();
  private RepeatHandler repeatHandler;
  private boolean autoHideIfEmpty;
  private Splitter[] headerSplitters;
  private Splitter[] splitterTemplates;
  private SplitsContext context;
  private List<RepeatContext> repeatContexts = new ArrayList<RepeatContext>();
  private RepeatLayout layout;
  private DefaultSplitsNode<Component> splitsNode;

  public RepeatPanel(String name, RepeatHandler repeatHandler, RepeatLayout layout,
                     boolean autoHideIfEmpty,
                     Splitter[] headerSplitters,
                     Splitter[] splitterTemplates,
                     SplitsContext context) {
    this.repeatHandler = repeatHandler;
    this.autoHideIfEmpty = autoHideIfEmpty;
    this.headerSplitters = headerSplitters;
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
    if (!items.isEmpty() && hasHeader()) {
      constraints.add(createHeaderStretchers());
    }

    int index = 0;
    for (Object item : items) {
      constraints.add(createStretches(item, index));
      index++;
    }

    layout.set(panel, constraints);
    GuiUtils.revalidate(panel);
    updateVisibility();
  }

  private boolean hasHeader() {
    return headerSplitters != null;
  }

  public void insert(Object item, int index) {
    if (repeatContexts.isEmpty() && hasHeader()) {
      layout.insert(panel, createHeaderStretchers(), 0);
    }

    layout.insert(panel, createStretches(item, index), adjustIndex(index));
    GuiUtils.revalidate(panel);
    updateVisibility();
  }

  public void remove(int index) {
    layout.remove(panel, adjustIndex(index));
    RepeatContext context = repeatContexts.remove(index);
    context.dispose();
    if (repeatContexts.isEmpty()) {
      if (hasHeader()){
        layout.remove(panel, 0);
      }
      panel.removeAll();
    }

    GuiUtils.revalidate(panel);
    panel.repaint();
    updateVisibility();
  }

  public void move(int previousIndex, int newIndex) {
    layout.move(panel, adjustIndex(previousIndex), adjustIndex(newIndex));
    RepeatContext context = repeatContexts.remove(previousIndex);
    repeatContexts.add(newIndex, context);
    GuiUtils.revalidate(panel);
  }

  private int adjustIndex(int index) {
    if (hasHeader()) {
      return index + 1;
    }
    return index;
  }

  private ComponentConstraints[] createHeaderStretchers() {
    ComponentConstraints[] constraints = new ComponentConstraints[headerSplitters.length];

    for (int i = 0; i < headerSplitters.length; i++) {

      Splitter.SplitComponent component =
        headerSplitters[i].createComponentStretch(context, !layout.managesInsets());
      constraints[i] = component.componentConstraints;
    }

    return constraints;
  }

  private ComponentConstraints[] createStretches(Object item, int index) {
    RepeatContext repeatContext = new RepeatContext(context);
    repeatContexts.add(index, repeatContext);
    repeatHandler.getFactory().registerComponents(new ContextualRepeatCellBuilder(repeatContext), item);

    ComponentConstraints[] constraints = new ComponentConstraints[splitterTemplates.length];
    for (int i = 0; i < splitterTemplates.length; i++) {
      Splitter.SplitComponent component =
        splitterTemplates[i].createComponentStretch(repeatContext, !layout.managesInsets());
      constraints[i] = component.componentConstraints;
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
