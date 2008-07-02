package org.globsframework.gui.splits.repeat;

import java.util.List;

public interface Repeat<T> {
  List<T> getInitialItems();

  void register(RepeatHandler<T> repeatHandler);

  RepeatComponentFactory getFactory();
}
