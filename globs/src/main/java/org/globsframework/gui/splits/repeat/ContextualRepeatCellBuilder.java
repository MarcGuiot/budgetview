package org.globsframework.gui.splits.repeat;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContextualRepeatCellBuilder implements RepeatCellBuilder {
  private RepeatContext repeatContext;

  public ContextualRepeatCellBuilder(RepeatContext repeatContext) {
    this.repeatContext = repeatContext;
  }

  public <T extends Component> T add(String name, T component)  {
    repeatContext.addComponent(name, component);
    return component;
  }

  public <T extends Action> T add(String name, T action) {
    repeatContext.add(name, action);
    return action;
  }

  public <T> DefaultRepeat<T> addRepeat(String name, List<T> items, RepeatComponentFactory<T> repeatFactory) {
    DefaultRepeat<T> repeat = new DefaultRepeat<T>(repeatFactory, items);
    repeatContext.addRepeat(name, repeat);
    return repeat;
  }

  public void addDisposeListener(DisposeListener listener) {
    repeatContext.addDisposeListener(listener);
  }
}
