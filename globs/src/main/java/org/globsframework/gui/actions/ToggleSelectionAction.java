package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleSelectionAction extends AbstractAction implements GlobSelectionListener, Disposable {

  private SelectionService selectionService;

  private Key key;
  private final String textForSelect;
  private final String textForUnselect;
  protected final GlobRepository repository;
  protected final Directory directory;

  public ToggleSelectionAction(Key key,
                               String textForSelect,
                               String textForUnselect,
                               GlobRepository repository,
                               Directory directory) {
    this.key = key;
    this.textForSelect = textForSelect;
    this.textForUnselect = textForUnselect;
    this.repository = repository;
    this.directory = directory;
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, key.getGlobType());
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  private void update() {
    GlobList selection = selectionService.getSelection(key.getGlobType());
    this.putValue(Action.NAME, selection.contains(key) ? textForUnselect : textForSelect);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobList selection = selectionService.getSelection(key.getGlobType());
    Glob glob = repository.find(key);
    if (glob == null) {
      return;
    }
    GlobList newSelection = new GlobList(selection);
    if (selection.contains(glob)) {
      selectionService.clear(key.getGlobType());
    }
    else {
      selectionService.select(glob);
    }
  }

  public void dispose() {
    selectionService.removeListener(this);
  }
}
