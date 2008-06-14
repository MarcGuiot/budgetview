package org.globsframework.model;

import org.globsframework.metamodel.GlobType;

import java.util.List;

public interface ChangeSetListener {
  void globsChanged(ChangeSet changeSet, GlobRepository repository);

  void globsReset(GlobRepository repository, List<GlobType> changedTypes);
}
