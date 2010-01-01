package org.globsframework.model.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Set;

public abstract class TypeChangeSetListener implements ChangeSetListener {

  private GlobType[] types;

  protected TypeChangeSetListener(GlobType... types) {
    if (types.length == 0) {
      throw new InvalidParameter("You must supply at least one GlobType");
    }
    this.types = types;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    for (GlobType type : types) {
      if (changeSet.containsChanges(type)) {
        update(repository);
        return;
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    for (GlobType type : types) {
      if (changedTypes.contains(type)) {
        update(repository);
        return;
      }
    }
  }

  protected abstract void update(GlobRepository repository);
}
