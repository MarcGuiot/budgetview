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
  private GlobType type;
  private String name;

  protected AbstractGlobSelectionAction(GlobType type, Directory directory) {
    this.type = type;
    directory.get(SelectionService.class).addListener(this, type);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList globs = selection.getAll(type);
    if (globs.size() == 0) {
      name = null;
      setEnabled(false);
    }
    else {
      setEnabled(true);
      name = toString(globs);
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
