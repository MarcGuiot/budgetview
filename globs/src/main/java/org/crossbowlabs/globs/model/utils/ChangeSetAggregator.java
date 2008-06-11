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
  private MutableChangeSet changeSet;
  private Listener listener;

  public ChangeSetAggregator(GlobRepository repository) {
    this(repository, new DefaultChangeSet());
  }

  public ChangeSetAggregator(GlobRepository repository, MutableChangeSet importChangeSet) {
    this.repository = repository;
    changeSet = importChangeSet;
    this.listener = new Listener();
    repository.addChangeListener(listener);
  }

  public MutableChangeSet getCurrentChanges() {
    return changeSet;
  }

  public MutableChangeSet dispose() {
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
