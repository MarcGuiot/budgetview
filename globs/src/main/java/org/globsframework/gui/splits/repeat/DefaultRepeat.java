package org.globsframework.gui.splits.repeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public class DefaultRepeat<T> implements Repeat<T>, RepeatHandler<T> {
  private RepeatPanel repeatPanel;
  private RepeatComponentFactory factory;
  private List<T> initialItems;

  public DefaultRepeat(RepeatComponentFactory factory, Collection<T> initialItems) {
    this.factory = factory;
    this.initialItems = new ArrayList<T>(initialItems);
  }

  public void register(RepeatPanel panel) {
    this.repeatPanel = panel;
  }

  public void set(List<T> items) {
    if (repeatPanel != null) {
      repeatPanel.set(items);
    }
    else {
      initialItems = new ArrayList<T>(items);
    }
  }

  public void insert(T item, int index) {
    if (repeatPanel != null) {
      repeatPanel.insert(item, index);
    }
    else if (initialItems != null){
      initialItems.add(index, item);
    }
  }

  public void remove(int index) {
    if (repeatPanel != null) {
      repeatPanel.remove(index);
    }
    else if (initialItems != null){
      initialItems.remove(index);
    }
  }

  public void move(int index1, int index2) {
    if (repeatPanel != null) {
      repeatPanel.move(index1, index2);
    }
    else if (initialItems != null) {
      T value = initialItems.remove(index1);
      initialItems.add(index2, value);
    }
  }

  public RepeatComponentFactory getFactory() {
    return factory;
  }

  public void dispose() {
    if (repeatPanel != null) {
      repeatPanel.dispose();
    }
    repeatPanel = null;
    initialItems = null;
  }

  public List<T> getInitialItems() {
    return initialItems;
  }
}
