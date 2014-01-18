package org.globsframework.gui.actions;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public abstract class SingleGlobAction extends AbstractAction implements ChangeSetListener, Disposable {

  private Key key;
  private GlobRepository repository;

  protected SingleGlobAction(String label, Key key, GlobRepository repository) {
    super(label);
    this.key = key;
    this.repository = repository;
    repository.addChangeListener(this);
    processUpdate(repository.find(key), repository);
  }

  protected abstract void processClick(Glob glob, GlobRepository repository);

  protected abstract void processUpdate(Glob glob, GlobRepository repository);

  public void actionPerformed(ActionEvent e) {
    processClick(repository.find(key), repository);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(key)) {
      processUpdate(repository.find(key), repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(key.getGlobType())) {
      processUpdate(repository.find(key), repository);
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
