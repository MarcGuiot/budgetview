package org.designup.picsou.gui.license;

import org.designup.picsou.model.User;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Set;

public abstract class AddOnStatusListener implements ChangeSetListener {

  public static void install(GlobRepository repository, AddOnStatusListener listener) {
    repository.addChangeListener(listener);
    listener.update(repository);
  }

  protected abstract void statusChanged(boolean addOnActivated);

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(User.KEY)) {
      update(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(User.TYPE)) {
      update(repository);
    }
  }

  private void update(GlobRepository repository) {
    Glob user = repository.find(User.KEY);
    if (user != null) {
      statusChanged(user.isTrue(User.IS_REGISTERED_USER));
    }
  }

}
