package org.globsframework.gui.splits.repeat;

import java.awt.*;
import java.util.List;

public interface RepeatCellBuilder {
  void add(String name, Component component);

  <T> Repeat<T> addRepeat(String name, RepeatComponentFactory<T> repeatFactory, List<T> items);

  void addDisposeListener(DisposeListener dispose);

  interface DisposeListener {

    void dispose();
  }
}
