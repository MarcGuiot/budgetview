package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.SplitsNode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface RepeatCellBuilder {
  <T extends Component> SplitsNode<T> add(String name, T component);

  <T extends Action> T add(String name, T action);

  <T> Repeat<T> addRepeat(String name, List<T> items, RepeatComponentFactory<T> repeatFactory);

  void addDisposeListener(Disposable dispose);

}
