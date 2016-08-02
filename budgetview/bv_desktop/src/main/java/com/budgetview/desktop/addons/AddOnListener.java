package com.budgetview.desktop.addons;

import com.budgetview.model.AddOns;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.KeyChangeListener;

public abstract class AddOnListener {

  public abstract void processAddOn(boolean enabled);

  public static Disposable install(final GlobRepository repository, final BooleanField field, final AddOnListener listener) {
    final KeyChangeListener keyListener = new KeyChangeListener(AddOns.KEY) {
      public void update() {
        Glob addOns = repository.find(AddOns.KEY);
        if (addOns != null) {
          listener.processAddOn(addOns.isTrue(field));
        }
      }
    };
    repository.addChangeListener(keyListener);
    keyListener.update();
    return new Disposable() {
      public void dispose() {
        repository.removeChangeListener(keyListener);
      }
    };
  }
}
