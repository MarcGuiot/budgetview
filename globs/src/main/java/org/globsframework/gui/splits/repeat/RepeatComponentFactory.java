package org.globsframework.gui.splits.repeat;

public interface RepeatComponentFactory<T> {

  void registerComponents(RepeatCellBuilder cellBuilder, T item);
}
