package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.PanelBuilder;

public interface RepeatComponentFactory<T> {

  void registerComponents(PanelBuilder cellBuilder, T item);
}
