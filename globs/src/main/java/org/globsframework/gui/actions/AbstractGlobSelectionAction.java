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

  protected AbstractGlobSelectionAction(String name, GlobType type, Directory directory) {
    super(name);
    this.name = name;
    this.type = type;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, type);
    setEnabled(false);
  }

  protected AbstractGlobSelectionAction(GlobType type, Directory directory) {
    this(null, type, directory);
  }

  public void selectionUpdated(GlobSelection selection) {
    lastSelection = selection.getAll(type);
    name = toString(lastSelection);
    if (lastSelection.size() == 0) {
      setEnabled(false);
    }
    else {
      if (Strings.isNullOrEmpty(name)) {
        setEnabled(false);
      }
      else {
        setEnabled(true);
        putValue(NAME, name);
      }
    }
  }

  public String getName() {
    return name;
  }

  public String toString(GlobList globs) {
    return getName();
  }
}
