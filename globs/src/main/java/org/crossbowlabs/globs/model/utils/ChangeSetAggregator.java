package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.utils.exceptions.NotSupported;

import java.util.List;

public class ChangeSetAggregator {

  private GlobRepository repository;
  private MutableChangeSet changeSet = new DefaultChangeSet();
  private Listener listener;

  public ChangeSetAggregator(GlobRepository repository) {
    this.repository = repository;
    this.listener = new Listener();
    repository.addChangeListener(listener);
  }

  public ChangeSet getCurrentChanges() {
    return changeSet;
  }

  public ChangeSet dispose() {
    repository.removeChangeListener(listener);
    repository = null;
    try {
      return changeSet;
    }
    finally {
      changeSet = null;
    }
  }

  public void reset() {
    changeSet = new DefaultChangeSet();
  }

  private class Listener implements ChangeSetListener {
    public void globsChanged(ChangeSet newChanges, GlobRepository globRepository) {
      changeSet.merge(newChanges);
    }

    public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
      throw new NotSupported("");
    }
  }
}
