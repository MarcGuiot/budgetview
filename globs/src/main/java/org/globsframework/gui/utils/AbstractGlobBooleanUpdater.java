package org.globsframework.gui.utils;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;

import java.util.Set;

public abstract class AbstractGlobBooleanUpdater implements ChangeSetListener, Disposable {
  private BooleanField field;
  private GlobRepository repository;

  private Key currentKey;

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
    Glob project = repository.find(currentKey);
    if (project != null) {
      boolean value = project.isTrue(field);
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

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
