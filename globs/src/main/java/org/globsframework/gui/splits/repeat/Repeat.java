package org.globsframework.gui.splits.repeat;

import java.util.List;

public interface Repeat<T> {
  void set(List<T> items);

  void insert(T item, int index);

  void remove(int index);

}
