package org.globsframework.gui.splits.repeat;

import java.util.List;

public interface RepeatHandler<T> {
  List<T> getInitialItems();

  void register(RepeatPanel panell);

  RepeatComponentFactory getFactory();

  void dispose();
}
