package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.directory.Directory;
import org.globsframework.model.utils.GlobListMatcher;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public class AutoHideOnSelectionPanel extends JPanel implements GlobSelectionListener {

  private GlobType type;
  private GlobListMatcher matcher;
  private GlobRepository repository;

  public AutoHideOnSelectionPanel(GlobType type, GlobListMatcher matcher,
                                  GlobRepository repository, Directory directory) {
    this.type = type;
    this.matcher = matcher;
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, type);
    setVisible(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(type)) {
      setVisible(matcher.matches(selection.getAll(type),repository));
    }
  }
}
