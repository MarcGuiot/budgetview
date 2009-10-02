package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collection;

public class ContextualRepeatCellBuilder implements RepeatCellBuilder {
  private RepeatContext repeatContext;

  public ContextualRepeatCellBuilder(RepeatContext repeatContext) {
    this.repeatContext = repeatContext;
  }

  public <T extends Component> SplitsNode<T> add(String name, T component)  {
    SplitsNode<T> splitsNode = new DefaultSplitsNode<T>(component, repeatContext);
    repeatContext.addComponent(name, (SplitsNode<Component>)splitsNode);
    return splitsNode;
  }

  public <T extends Action> T add(String name, T action) {
    repeatContext.add(name, action);
    return action;
  }

  public <T> DefaultRepeat<T> addRepeat(String name, Collection<T> items, RepeatComponentFactory<T> repeatFactory) {
    DefaultRepeat<T> repeat = new DefaultRepeat<T>(repeatFactory, items);
    repeatContext.addRepeat(name, repeat);
    return repeat;
  }

  public void addDisposeListener(Disposable listener) {
    repeatContext.addDisposable(listener);
  }
}
