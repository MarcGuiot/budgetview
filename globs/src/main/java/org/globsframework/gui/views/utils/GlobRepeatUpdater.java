package org.globsframework.gui.views.utils;

import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobRepeatListener;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.views.GlobViewModel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GlobRepeatUpdater implements GlobViewModel.Listener, GlobRepeat, Disposable {
  private GlobViewModel model;
  private Repeat<Glob> repeat;
  private List<GlobRepeatListener> listeners;
  boolean inUpdate = false;

  public void globInserted(int index) {
    repeat.insert(model.get(index), index);
    if (!inUpdate){
      notifyListeners();
    }
  }

  public void globUpdated(int index) {
  }

  public void globRemoved(int index) {
    repeat.remove(index);
    if (!inUpdate) {
      notifyListeners();
    }
  }

  public void globMoved(int previousIndex, int newIndex) {
    repeat.move(previousIndex, newIndex);
    if (!inUpdate) {
      notifyListeners();
    }
  }

  public void globListPreReset() {
  }

  public void globListReset() {
    if ((repeat != null) && (model != null)) {
      repeat.set(model.getAll());
      if (!inUpdate){
        notifyListeners();
      }
    }
  }

  public void startUpdate() {
    inUpdate = true;
    repeat.startUpdate();
  }

  public void updateComplete() {
    inUpdate = false;
    repeat.updateComplete();
    notifyListeners();
  }

  public void set(GlobViewModel model, Repeat<Glob> repeat) {
    this.model = model;
    this.repeat = repeat;
    notifyListeners();
  }

  public GlobList getCurrentGlobs() {
    return model.getAll();
  }

  public void setFilter(GlobMatcher matcher) {
    model.setFilter(matcher, false);
  }

  public boolean isEmpty() {
    return model.size() == 0;
  }

  public int size() {
    return model.size();
  }

  public void addListener(GlobRepeatListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<GlobRepeatListener>();
    }
    listeners.add(listener);
  }

  public void removeListener(GlobRepeatListener listener) {
    if (listeners != null) {
      listeners.remove(listener);
      if (listeners.isEmpty()) {
        listeners = null;
      }
    }
  }

  public void clear() {
    repeat.set(GlobList.EMPTY);
    notifyListeners();
  }

  public void refresh() {
    model.refresh();
  }

  private void notifyListeners() {
    if (listeners != null) {
      GlobList currentList = model.getAll();
      for (GlobRepeatListener listener : listeners) {
        listener.listChanged(currentList);
      }
    }
  }

  public void dispose() {
    if (listeners != null) {
      listeners.clear();
      listeners = null;
    }
    if (model != null) {
      // disposing the updater should not dispose the model
      model = null;
    }
  }
}
