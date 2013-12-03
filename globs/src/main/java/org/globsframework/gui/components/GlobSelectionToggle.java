package org.globsframework.gui.components;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GlobSelectionToggle implements GlobSelectionListener, ChangeListener, Disposable {
  private JToggleButton toggleButton;
  private GlobRepository repository;
  private SelectionService selectionService;
  private Key selectionKey;
  private GlobType type;
  private boolean selectionInProgress;

  public GlobSelectionToggle(Key key, GlobRepository repository, Directory directory) {
    this.toggleButton = new JToggleButton();
    this.repository = repository;
    this.selectionKey = key;
    this.type = key.getGlobType();

    this.toggleButton.addChangeListener(this);

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, type);
    updateToggle(selectionService.getSelection(type));
  }

  public void selectionUpdated(GlobSelection selection) {
    updateToggle(selection.getAll(type));
  }

  private void updateToggle(GlobList selection) {
    selectionInProgress = true;
    try {
      toggleButton.setSelected(selection.getKeyList().contains(selectionKey));
    }
    finally {
      selectionInProgress = false;
    }
  }

  public void stateChanged(ChangeEvent changeEvent) {
    if (selectionInProgress) {
      return;
    }

    selectionInProgress = true;
    try {
      Glob glob = repository.get(selectionKey);
      GlobList newSelection = new GlobList();
      if (toggleButton.getModel().isSelected()) {
        newSelection.add(glob);
      }
      else {
        newSelection.addAll(selectionService.getSelection(type));
        newSelection.remove(glob);
      }
      selectionService.select(newSelection, type);
    }
    finally {
      selectionInProgress = false;
    }
  }

  public JToggleButton getComponent() {
    return toggleButton;
  }

  public void dispose() {
    this.selectionService.removeListener(this);
  }
}
