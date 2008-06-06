package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.ComponentHolder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.format.GlobListStringifier;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.utils.directory.Directory;

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
