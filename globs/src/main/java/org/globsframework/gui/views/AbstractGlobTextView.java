package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import java.util.List;

public abstract class AbstractGlobTextView implements GlobSelectionListener, ChangeSetListener, ComponentHolder {
  private GlobType type;
  private GlobRepository repository;
  private GlobListStringifier stringifier;
  private GlobList currentSelection = new GlobList();

  public AbstractGlobTextView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    this.type = type;
    this.repository = repository;
    this.stringifier = stringifier;
    directory.get(SelectionService.class).addListener(this, type);
    repository.addChangeListener(this);
  }

  public void update() {
    doUpdate(stringifier.toString(currentSelection));
  }

  protected abstract void doUpdate(String text);

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(type)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    currentSelection = new GlobList();
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    currentSelection = selection.getAll(type);
    update();
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
