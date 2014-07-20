package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.impl.DefaultSplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.OnLoadListener;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ContextualRepeatCellBuilder implements PanelBuilder {
  private RepeatContext repeatContext;

  public ContextualRepeatCellBuilder(RepeatContext repeatContext) {
    this.repeatContext = repeatContext;
  }

  public <T extends Component> SplitsNode<T> add(String id, T component) {
    SplitsNode<T> splitsNode = new DefaultSplitsNode<T>(component, repeatContext);
    repeatContext.addComponent(id, (SplitsNode<Component>)splitsNode);
    return splitsNode;
  }

  public <T extends Action> T add(String id, T action) {
    repeatContext.add(id, action);
    return action;
  }

  public <T> DefaultRepeat<T> addRepeat(String id, Collection<T> items, RepeatComponentFactory<T> repeatFactory) {
    DefaultRepeat<T> repeat = new DefaultRepeat<T>(repeatFactory, items);
    repeatContext.addRepeat(id, repeat);
    return repeat;
  }

  public CardHandler addCardHandler(String handlerId) {
    JPanel panel = new JPanel();
    add(handlerId, panel);
    return DefaultCardHandler.init(panel);
  }

  public ContextualRepeatCellBuilder addDisposable(Disposable disposable) {
    repeatContext.addDisposable(disposable);
    return this;
  }

  public void addOnLoadListener(OnLoadListener listener) {
    repeatContext.addOnLoadListener(listener);
  }

  public void removeOnLoadListener(OnLoadListener listener) {
    repeatContext.removeOnLoadListener(listener);
  }
}
