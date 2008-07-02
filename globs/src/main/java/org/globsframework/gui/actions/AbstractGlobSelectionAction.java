package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractGlobSelectionAction extends AbstractAction implements GlobSelectionListener {
  protected final GlobType type;
  protected final Directory directory;
  private String name;
  protected GlobList lastSelection = GlobList.EMPTY;

  protected AbstractGlobSelectionAction(GlobType type, Directory directory) {
    this.type = type;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, type);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    lastSelection = selection.getAll(type);
    if (lastSelection.size() == 0) {
      name = null;
      setEnabled(false);
    }
    else {
      setEnabled(true);
      name = toString(lastSelection);
      if (Strings.isNullOrEmpty(name)) {
        setEnabled(false);
      }
      else {
        putValue(NAME, name);
      }
    }
  }

  public String getName() {
    return name;
  }

  public abstract String toString(GlobList globs);
}
