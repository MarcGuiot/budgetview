package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.DefaultCardHandler;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.utils.Functor;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Collection;

public interface RepeatCellBuilder {
  <T extends Component> SplitsNode<T> add(String name, T component);

  <T extends Action> T add(String name, T action);

  <T> Repeat<T> addRepeat(String name, Collection<T> items, RepeatComponentFactory<T> repeatFactory);

  CardHandler addCardHandler(String handlerName);

  void addDisposeListener(Disposable disposable);

  void addOnLoadListener(OnLoadListener listener);
}
