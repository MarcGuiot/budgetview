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
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class SingleSelectionAction extends AbstractAction implements GlobSelectionListener, Disposable {

  private GlobType type;
  private GlobMatcher matcher;
  private SelectionService selectionService;
  private Key forcedKey;

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
    selectionService.addListener(this, type);
    update();
  }

  public void setKey(Key key) {
    if (forcedKey == null) {
      selectionService.removeListener(this);
    }
    this.forcedKey = key;
    update();
  }

  private void update() {
    Glob currentGlob = getCurrentGlob();
    setEnabled((currentGlob != null) && matcher.matches(currentGlob, repository));
    processSelection(currentGlob);
  }

  private Glob getCurrentGlob() {
    if (forcedKey != null) {
      return repository.find(forcedKey);
    }

    GlobList selection = selectionService.getSelection(type);
    if (selection.size() != 1) {
      return null;
    }
    return selection.get(0);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  protected void processSelection(Glob glob) {
  }

  public void actionPerformed(ActionEvent actionEvent) {
    process(getCurrentGlob(), repository, directory);
  }

  protected abstract void process(Glob selected, GlobRepository repository, Directory directory);

  public void dispose() {
    selectionService.removeListener(this);
  }
}
