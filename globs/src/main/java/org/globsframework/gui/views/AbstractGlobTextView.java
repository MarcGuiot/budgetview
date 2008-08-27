package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.DefaultSelection;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public abstract class AbstractGlobTextView<T extends AbstractGlobTextView>
  implements GlobSelectionListener, ChangeSetListener, ComponentHolder {

  private GlobType type;
  private GlobRepository repository;
  private Directory directory;
  private GlobListStringifier stringifier;
  private GlobMatcher filter = GlobMatchers.ALL;
  private GlobList currentSelection = new GlobList();
  private boolean autoHideIfEmpty;
  private GlobListMatcher autoHideMatcher = GlobListMatchers.ALL;
  private ChangeSetMatcher updateMatcher = ChangeSetMatchers.NONE;
  protected boolean initCompleted = false;
  private GlobList forcedSelection;
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

  public T forceSelection(Glob glob) {
    this.forcedSelection = new GlobList(glob);
    return (T)this;
  }

  public void update() {
    JComponent component = getComponent();

    GlobList filteredSelection = currentSelection.filter(filter, repository);

    boolean matches = autoHideMatcher.matches(filteredSelection, repository);
    component.setVisible(matches);
    if (!component.isVisible()) {
      return;
    }

    String text = stringifier.toString(filteredSelection, repository);
    if (autoHideIfEmpty) {
      component.setVisible(Strings.isNotEmpty(text));
    }

    doUpdate(text);
  }

  public T setName(String name) {
    this.name = name;
    return (T)this;
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
    else if (updateMatcher.matches(changeSet, repository)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
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

  protected void complete() {
    if (forcedSelection == null) {
      directory.get(SelectionService.class).addListener(this, type);
    }
    else {
      selectionUpdated(new DefaultSelection(forcedSelection, Collections.singletonList(type)));
    }
    getComponent().setName(name);
  }
}
