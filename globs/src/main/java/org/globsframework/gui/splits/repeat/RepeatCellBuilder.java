package org.globsframework.gui.splits.repeat;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface RepeatCellBuilder {
  void add(String name, Component component);

  void add(String name, Action action);

  <T> Repeat<T> addRepeat(String name, RepeatComponentFactory<T> repeatFactory, List<T> items);

  void addDisposeListener(DisposeListener dispose);

  interface DisposeListener {

    void dispose();
  }
}
