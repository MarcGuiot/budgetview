package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.Strings;

import java.util.List;
import java.util.Set;

public abstract class AbstractGlobTextView<T extends AbstractGlobTextView> implements GlobSelectionListener, ChangeSetListener, ComponentHolder {
  private GlobType type;
  private GlobRepository repository;
  private GlobListStringifier stringifier;
  private GlobList currentSelection = new GlobList();
  private boolean autoHide;

  public AbstractGlobTextView(GlobType type, GlobRepository repository, Directory directory, GlobListStringifier stringifier) {
    this.type = type;
    this.repository = repository;
    this.stringifier = stringifier;
    directory.get(SelectionService.class).addListener(this, type);
    repository.addChangeListener(this);
  }

  public T setAutoHide(boolean autoHide) {
    this.autoHide = autoHide;
    getComponent().setVisible(!autoHide || Strings.isNotEmpty(getText()));
    return (T)this;
  }

  public void update() {
    String text = stringifier.toString(currentSelection);
    if (autoHide) {
      getComponent().setVisible(Strings.isNotEmpty(text));
    }
    doUpdate(text);
  }

  protected abstract void doUpdate(String text);

  protected abstract String getText();

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(type)) {
      Set<Key> deleted = changeSet.getDeleted(type);
      if (!deleted.isEmpty()) {
        currentSelection.removeAll(deleted);
      }
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
