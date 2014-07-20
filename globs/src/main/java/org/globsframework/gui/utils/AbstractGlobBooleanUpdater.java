package org.globsframework.gui.utils;

import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import java.util.Set;

public abstract class AbstractGlobBooleanUpdater implements ChangeSetListener, Disposable {
  private BooleanField field;
  private GlobRepository repository;
  private Key currentKey;
  private DisposableGroup disposables;

  public AbstractGlobBooleanUpdater(BooleanField field, GlobRepository repository) {
    this.field = field;
    this.repository = repository;
    repository.addChangeListener(this);
  }

  protected abstract void doUpdate(boolean value);

  public void update() {
    if (currentKey == null) {
      return;
    }
    Glob glob = repository.find(currentKey);
    if (glob != null) {
      boolean value = glob.isTrue(field);
      doUpdate(value);
    }
  }

  public void setKey(Key key) {
    this.currentKey = key;
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if ((currentKey != null) && changeSet.containsChanges(currentKey, field)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(field.getGlobType())) {
      update();
    }
  }

  public void setKeyOnLoad(final Key projectKey, final PanelBuilder builder) {
    if (disposables == null) {
      disposables = new DisposableGroup();
    }

    final OnLoadListener listener = new OnLoadListener() {
      public void processLoad() {
        setKey(projectKey);
      }
    };

    builder.addOnLoadListener(listener);
    disposables.add(new Disposable() {
      public void dispose() {
        builder.removeOnLoadListener(listener);
      }
    });
  }


  public void dispose() {
    if (disposables != null) {
      disposables.dispose();
    }
    repository.removeChangeListener(this);
    currentKey = null;
  }
}
