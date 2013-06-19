package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class MultiSelectionAction extends AbstractAction {

  private GlobType type;
  private GlobMatcher matcher;
  protected SelectionService selectionService;

  protected final GlobRepository repository;
  protected final Directory directory;

  public MultiSelectionAction(String name,
                              GlobType type,
                              GlobRepository repository,
                              Directory directory) {
    super(name);
    this.type = type;
    this.repository = repository;
    this.directory = directory;
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update();
      }
    }, type);
    update();
  }

  private void update() {
    GlobList selection = selectionService.getSelection(type);
    setEnabled(!selection.isEmpty());
    processSelection(selection);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    processClick(selectionService.getSelection(type), repository, directory);
  }

  protected void processSelection(GlobList selection) {
  }

  protected abstract void processClick(GlobList selection, GlobRepository repository, Directory directory);
}
