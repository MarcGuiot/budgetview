package org.globsframework.gui.splits.repeat;

import java.awt.*;
import java.util.List;

public interface RepeatCellBuilder {
  void add(String name, Component component);

  <T> RepeatHandler<T> addRepeat(String name, RepeatFactory<T> repeatFactory, List<T> items);
}
