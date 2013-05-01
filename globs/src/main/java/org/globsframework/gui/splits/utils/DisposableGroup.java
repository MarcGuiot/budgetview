package org.globsframework.gui.splits.utils;

import java.util.ArrayList;
import java.util.List;

public class DisposableGroup implements Disposable {

  private List<Disposable> disposables = new ArrayList<Disposable>();

  public <T extends Disposable> T add(T disposable) {
    disposables.add(disposable);
    return disposable;
  }

  public void dispose() {
    for (Disposable disposable : disposables) {
      disposable.dispose();
    }
    disposables.clear();
  }
}
