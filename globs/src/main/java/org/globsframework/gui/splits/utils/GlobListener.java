package org.globsframework.gui.splits.utils;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.KeyChangeListener;

public class GlobListener {
  public static Disposable install(final Key key, final GlobRepository repository, final Functor functor) {
    final KeyChangeListener listener = new KeyChangeListener(key) {
      public void update() {
        functor.update(repository.find(key), repository);
      }
    };
    repository.addChangeListener(listener);
    listener.update();
    return new Disposable() {
      public void dispose() {
        repository.removeChangeListener(listener);
      }
    };
  }

  public interface Functor {
    void update(Glob glob, GlobRepository repository);
  }
}
