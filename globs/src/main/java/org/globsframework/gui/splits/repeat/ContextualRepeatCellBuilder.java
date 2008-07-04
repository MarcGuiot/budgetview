package org.globsframework.gui.splits.repeat;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContextualRepeatCellBuilder implements RepeatCellBuilder {
  private RepeatContext repeatContext;

  public ContextualRepeatCellBuilder(RepeatContext repeatContext) {
    this.repeatContext = repeatContext;
  }

  public void add(String name, Component component) {
    repeatContext.addComponent(name, component);
  }

  public void add(String name, Action action) {
    repeatContext.add(name, action);
  }

  public <T> DefaultRepeat<T> addRepeat(String name, RepeatComponentFactory<T> repeatFactory, List<T> items) {
    DefaultRepeat<T> repeat = new DefaultRepeat<T>(repeatFactory, items);
    repeatContext.addRepeat(name, repeat);
    return repeat;
  }

  public void addDisposeListener(DisposeListener listener) {
    repeatContext.addDisposeListener(listener);
  }
}
