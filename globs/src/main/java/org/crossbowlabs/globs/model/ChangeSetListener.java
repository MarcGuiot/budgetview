package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.GlobType;

import java.util.List;

public interface ChangeSetListener {
  void globsChanged(ChangeSet changeSet, GlobRepository repository);

  void globsReset(GlobRepository repository, List<GlobType> changedTypes);
}
