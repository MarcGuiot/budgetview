package org.globsframework.gui.splits.repeat;

import java.util.List;

public class DefaultRepeat<T> implements RepeatHandler<T>, Repeat<T> {
  private RepeatHandler<T> repeatHandler;
  private RepeatComponentFactory factory;
  private List<T> initialItems;

  public DefaultRepeat(RepeatComponentFactory factory, List<T> initialItems) {
    this.factory = factory;
    this.initialItems = initialItems;
  }

  public void set(List<T> items) {
    repeatHandler.set(items);
  }

  public void insert(T item, int index) {
    repeatHandler.insert(item, index);
  }

  public void remove(int index) {
    repeatHandler.remove(index);
  }

  public RepeatComponentFactory getFactory() {
    return factory;
  }

  public List<T> getInitialItems() {
    return initialItems;
  }

  public void register(RepeatHandler<T> repeatHandler) {
    this.repeatHandler = repeatHandler;
  }
}
