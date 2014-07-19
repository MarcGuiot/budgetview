package org.globsframework.gui.splits;

import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.OnLoadListener;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public interface PanelBuilder {
  <T extends Component> SplitsNode<T> add(String name, T component);

  <T extends Action> T add(String name, T action);

  <T> Repeat<T> addRepeat(String name, Collection<T> items, RepeatComponentFactory<T> repeatFactory);

  CardHandler addCardHandler(String handlerName);

  PanelBuilder addDisposable(Disposable disposable);

  void addOnLoadListener(OnLoadListener listener);

  void removeOnLoadListener(OnLoadListener listener);
}
