package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Project;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import java.util.Set;

public class HideProjectDetailsTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsDeletions(Project.TYPE)) {
      update(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE)) {
      update(repository);
    }
  }

  private void update(GlobRepository repository) {
    if (!repository.contains(Project.TYPE) && repository.contains(UserPreferences.KEY)) {
      repository.update(UserPreferences.KEY, UserPreferences.SHOW_PROJECT_DETAILS, false);
    }
  }
}
