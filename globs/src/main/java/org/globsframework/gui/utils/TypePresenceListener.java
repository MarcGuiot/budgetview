package org.globsframework.gui.utils;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.util.Set;

public class TypePresenceListener implements Disposable {
  private GlobType type;
  private final GlobRepository repository;
  private BooleanListener functor;
  private ChangeSetListener listener;

  public static TypePresenceListener installShowHide(final JComponent component, GlobType type, GlobRepository repository) {
    return install(type, repository, new BooleanListener() {
      public void apply(boolean value) {
        component.setVisible(value);
      }
    });
  }

  public static TypePresenceListener install(GlobType type, GlobRepository repository, BooleanListener functor) {
    return new TypePresenceListener(type, repository, functor);
  }

  public TypePresenceListener(final GlobType type, GlobRepository repository, BooleanListener functor) {
    this.type = type;
    this.repository = repository;
    this.functor = functor;
    this.listener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!changeSet.containsCreationsOrDeletions(type)) {
          return;
        }
        doUpdate();
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (!changedTypes.contains(type)) {
          return;
        }
        doUpdate();
      }

    };
    repository.addChangeListener(listener);
    doUpdate();
  }

  private void doUpdate() {
    functor.apply(repository.contains(type));
  }

  public void dispose() {
    repository.removeChangeListener(listener);
  }
}
