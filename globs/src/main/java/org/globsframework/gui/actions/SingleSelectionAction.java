package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class SingleSelectionAction extends AbstractAction {

  private GlobType type;
  private GlobMatcher matcher;
  private SelectionService selectionService;

  protected final GlobRepository repository;
  protected final Directory directory;

  public SingleSelectionAction(String name,
                               GlobType type,
                               GlobRepository repository,
                               Directory directory) {
    this(name, type, GlobMatchers.ALL, repository, directory);
  }

  public SingleSelectionAction(String name,
                               GlobType type,
                               GlobMatcher matcher,
                               GlobRepository repository,
                               Directory directory) {
    super(name);
    this.type = type;
    this.matcher = matcher;
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
    if (selection.size() != 1) {
      setEnabled(false);
      processSelection(null);
    }
    else {
      Glob glob = selection.get(0);
      setEnabled(matcher.matches(glob, repository));
      processSelection(glob);
    }
  }
  
  protected void processSelection(Glob glob) {

  }

  public void actionPerformed(ActionEvent actionEvent) {
    process(selectionService.getSelection(type).get(0), repository, directory);
  }

  protected abstract void process(Glob selected, GlobRepository repository, Directory directory);
}
