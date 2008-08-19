package org.globsframework.gui.actions;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class AbstractGlobSelectionAction extends AbstractAction implements GlobSelectionListener {
  protected final GlobType type;
  private GlobRepository repository;
  protected final Directory directory;
  private String name;
  protected GlobList lastSelection = GlobList.EMPTY;
  private GlobMatcher matcher = GlobMatchers.ALL;

  protected AbstractGlobSelectionAction(String name, GlobType type, GlobRepository repository, Directory directory) {
    super(name);
    this.name = name;
    this.type = type;
    this.repository = repository;
    this.directory = directory;
    directory.get(SelectionService.class).addListener(this, type);
    setEnabled(false);
  }

  protected AbstractGlobSelectionAction(GlobType type, GlobRepository repository, Directory directory) {
    this(null, type, repository, directory);
  }

  public void setMatcher(GlobMatcher matcher) {
    this.matcher = matcher;
  }

  public void selectionUpdated(GlobSelection selection) {
    lastSelection = selection.getAll(type).filter(matcher, repository);
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
