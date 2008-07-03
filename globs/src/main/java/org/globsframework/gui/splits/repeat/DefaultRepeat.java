package org.globsframework.gui.splits.repeat;

import java.util.List;

public class DefaultRepeat<T> implements Repeat<T>, RepeatHandler<T> {
  private RepeatPanel repeatPanel;
  private RepeatComponentFactory factory;
  private List<T> initialItems;

  public DefaultRepeat(RepeatComponentFactory factory, List<T> initialItems) {
    this.factory = factory;
    this.initialItems = initialItems;
  }

  public void set(List<T> items) {
    repeatPanel.set(items);
  }

  public void insert(T item, int index) {
    repeatPanel.insert(item, index);
  }

  public void remove(int index) {
    repeatPanel.remove(index);
  }

  public RepeatComponentFactory getFactory() {
    return factory;
  }

  public void dispose() {
    repeatPanel.dispose();
  }

  public List<T> getInitialItems() {
    return initialItems;
  }

  public void register(RepeatPanel panel) {
    this.repeatPanel = panel;
  }
}
