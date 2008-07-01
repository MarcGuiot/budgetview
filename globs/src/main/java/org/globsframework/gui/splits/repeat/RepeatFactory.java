package org.globsframework.gui.splits.repeat;

public interface RepeatFactory<T> {

  void register(RepeatCellBuilder cellBuilder, T item);
}
