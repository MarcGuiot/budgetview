package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.SplitHandler;
import org.globsframework.gui.splits.impl.DefaultSplitHandler;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ContextualRepeatCellBuilder implements RepeatCellBuilder {
  private RepeatContext repeatContext;

  public ContextualRepeatCellBuilder(RepeatContext repeatContext) {
    this.repeatContext = repeatContext;
  }

  public <T extends Component> SplitHandler<T> add(String name, T component)  {
    SplitHandler<T> splitHandler = new DefaultSplitHandler<T>(component, repeatContext);
    repeatContext.addComponent(name, (SplitHandler<Component>)splitHandler);
    return splitHandler;
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

  public void addDisposeListener(Disposable listener) {
    repeatContext.addDisposable(listener);
  }
}
