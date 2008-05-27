package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.GlobRepository;

import java.util.List;

public abstract class DefaultChangeSetListener implements ChangeSetListener {
  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
