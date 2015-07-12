package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.utils.Disposable;

import java.util.List;

public interface Repeat<T> extends Disposable {
  void set(List<T> items);

  void insert(T item, int index);

  void remove(int index);

  void move(int index1, int index2);

  void startUpdate();

  void updateComplete();
}
