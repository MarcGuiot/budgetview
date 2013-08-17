package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractGlobFieldEditor<T extends AbstractGlobFieldEditor>
  extends AbstractGlobComponentHolder<T>
  implements ChangeSetListener, GlobSelectionListener {

  protected GlobList currentGlobs = new GlobList();
  protected boolean isAdjusting;
  private List<Key> forcedSelection;

  protected AbstractGlobFieldEditor(GlobType globType, GlobRepository repository, Directory directory) {
    super(globType, repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, type);
  }

  protected abstract void updateFromGlobs();

  public T forceSelection(Key key) {
    selectionService.removeListener(this);
    forcedSelection = new ArrayList<Key>();
    forcedSelection.add(key);
    updateCurrentGlobsList();
    updateFromGlobs();
    return (T)this;
  }

  private void updateCurrentGlobsList() {
    if (forcedSelection == null) {
      currentGlobs.keepExistingGlobsOnly(repository);
    }
    else {
      currentGlobs = new GlobList();
      for (Key key : forcedSelection) {
        Glob glob = repository.find(key);
        if (glob != null) {
          currentGlobs.add(glob);
        }
      }
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
    if (forcedSelection == null) {
      selectionService.removeListener(this);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (forcedSelection == null) {
      this.currentGlobs = selection.getAll(type);
      updateFromGlobs();
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (isAdjusting || !changeSet.containsChanges(type)) {
      return;
    }

    updateCurrentGlobsList();
    updateFromGlobs();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      updateCurrentGlobsList();
      updateFromGlobs();
    }
  }

}
