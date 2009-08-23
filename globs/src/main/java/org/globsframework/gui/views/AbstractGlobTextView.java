package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractGlobTextView<T extends AbstractGlobTextView>
  implements GlobSelectionListener, ChangeSetListener, ComponentHolder {

  private GlobType type;
  private GlobRepository repository;
  private Directory directory;
  private GlobListStringifier stringifier;
  private GlobMatcher filter = GlobMatchers.ALL;
  protected GlobList currentSelection = new GlobList();
  private boolean autoHideIfEmpty;
  private GlobListMatcher autoHideMatcher = null;
  private ChangeSetMatcher updateMatcher = ChangeSetMatchers.NONE;
  protected boolean initCompleted = false;
  private List<Key> forcedSelection;
  protected String name;

  public AbstractGlobTextView(GlobType type, GlobRepository repository, Directory directory,
                              GlobListStringifier stringifier) {
    this.type = type;
    this.repository = repository;
    this.directory = directory;
    this.stringifier = stringifier;
    repository.addChangeListener(this);
  }

  public T setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    if (initCompleted) {
      update();
    }
    return (T)this;
  }

  public T setAutoHideIfEmpty(boolean autoHide) {
    this.autoHideIfEmpty = autoHide;
    if (initCompleted) {
      update();
    }
    return (T)this;
  }

  public T setAutoHideMatcher(GlobListMatcher matcher) {
    this.autoHideMatcher = matcher;
    if (initCompleted) {
      update();
    }
    return (T)this;
  }

  public T setUpdateMatcher(ChangeSetMatcher updateMatcher) {
    this.updateMatcher = updateMatcher;
    return (T)this;
  }

  public T forceSelection(Key key) {
    this.forcedSelection = new ArrayList<Key>();
    this.forcedSelection.add(key);
    if (initCompleted) {
      forceSelection();
    }
    return (T)this;
  }

  public void update() {
    JComponent component = getComponent();

    GlobList filteredSelection;
    try {
      filteredSelection = getFilteredSelection();
    }
    catch (Exception e) {
      throw new RuntimeException("Exception for " + component.getName() +
                                 " - type=" + type +
                                 " - currentSelection=" + currentSelection, e);
    }

    if (autoHideMatcher != null) {
      boolean matches = autoHideMatcher.matches(filteredSelection, repository);
      component.setVisible(matches);
      if (!component.isVisible()) {
        return;
      }
    }

    String text = stringifier.toString(filteredSelection, repository);
    if (autoHideIfEmpty) {
      component.setVisible(Strings.isNotEmpty(text));
    }

    doUpdate(text);
  }

  protected GlobList getFilteredSelection() {
    return currentSelection.filter(filter, repository);
  }

  public T setName(String name) {
    this.name = name;
    return (T)this;
  }

  protected abstract void doUpdate(String text);

  protected abstract String getText();

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(type)) {
      if (forcedSelection != null) {
        currentSelection = new GlobList();
        for (Key key : forcedSelection) {
          Glob glob = globRepository.find(key);
          if (glob != null) {
            currentSelection.add(glob);
          }
        }
      }
      else {
        Set<Key> deleted = changeSet.getDeleted(type);
        if (!deleted.isEmpty()) {
          currentSelection.removeAll(deleted);
        }
      }
      update();
    }
    else if (updateMatcher.matches(changeSet, repository)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
    currentSelection = new GlobList();
    if (forcedSelection != null) {
      for (Key key : forcedSelection) {
        Glob glob = globRepository.find(key);
        if (glob != null) {
          currentSelection.add(glob);
        }
      }
    }
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    currentSelection = selection.getAll(type);
    update();
  }

  public void dispose() {
    repository.removeChangeListener(this);
    if (forcedSelection == null) {
      directory.get(SelectionService.class).removeListener(this);
    }
  }

  protected void complete() {
    if (forcedSelection == null) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.addListener(this, type);
      currentSelection = selectionService.getSelection(type);
      update();
    }
    else {
      forceSelection();
    }
    getComponent().setName(name);
  }

  private void forceSelection() {
    GlobList globList = new GlobList();
    for (Key key : forcedSelection) {
      Glob glob = repository.find(key);
      if (glob != null) {
        globList.add(glob);
      }
    }
    selectionUpdated(GlobSelectionBuilder.init().add(globList, type).get());
  }
}
