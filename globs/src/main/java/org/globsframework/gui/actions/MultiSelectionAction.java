package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class MultiSelectionAction extends AbstractAction implements GlobSelectionListener, Disposable {

  private GlobType type;
  protected SelectionService selectionService;
  private Key forcedKey;

  protected final GlobRepository repository;
  protected final Directory directory;

  public MultiSelectionAction(GlobType type,
                              GlobRepository repository,
                              Directory directory) {
    this(null, type, repository, directory);
  }

  public MultiSelectionAction(Key forcedKey,
                              GlobType type,
                              GlobRepository repository,
                              Directory directory) {
    this.forcedKey = forcedKey;
    this.type = type;
    this.repository = repository;
    this.directory = directory;
    selectionService = directory.get(SelectionService.class);
    if (forcedKey == null) {
      selectionService.addListener(this, type);
    }
    update();
  }

  public void dispose() {
    if (forcedKey == null) {
      selectionService.removeListener(this);
    }
  }

  public final void selectionUpdated(GlobSelection selection) {
    update();
  }

  private void update() {
    GlobList selection = getCurrentSelection();

    putValue(NAME, getLabel(selection));
    setEnabled(!selection.isEmpty());
    processSelection(selection);
  }

  protected abstract String getLabel(GlobList selection);

  public void actionPerformed(ActionEvent actionEvent) {
    processClick(getCurrentSelection(), repository, directory);
  }

  private GlobList getCurrentSelection() {
    GlobList selection;
    if (forcedKey != null) {
      selection = new GlobList();
      Glob glob = repository.find(forcedKey);
      if (glob != null) {
        selection.add(glob);
      }
    }
    else {
      selection = selectionService.getSelection(type);
    }
    return selection;
  }

  protected void processSelection(GlobList selection) {
  }

  protected abstract void processClick(GlobList selection, GlobRepository repository, Directory directory);
}
