package org.globsframework.model.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import java.util.List;

public abstract class DefaultChangeSetListener implements ChangeSetListener {
  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
