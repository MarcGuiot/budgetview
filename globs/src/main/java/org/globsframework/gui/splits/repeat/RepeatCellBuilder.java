package org.globsframework.gui.splits.repeat;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface RepeatCellBuilder {
  <T extends Component> T add(String name, T component);

  <T extends Action> T add(String name, T action);

  <T> Repeat<T> addRepeat(String name, List<T> items, RepeatComponentFactory<T> repeatFactory);

  void addDisposeListener(DisposeListener dispose);

  interface DisposeListener {

    void dispose();
  }
}
