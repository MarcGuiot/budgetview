package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AutoHideOnSelectionPanel extends JPanel implements GlobSelectionListener {

  public enum Mode {
    SHOW_IF_AT_LEAST_ONE,
    SHOW_IF_AT_LEAST_TWO
  }

  private GlobType type;
  private Mode mode;

  public AutoHideOnSelectionPanel(GlobType type, Mode mode, Directory directory) {
    this.type = type;
    this.mode = mode;
    directory.get(SelectionService.class).addListener(this, type);
    setVisible(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    setVisible(doShow(selection.getAll(type).size()));
  }

  public boolean doShow(int count) {
    switch (mode) {
      case SHOW_IF_AT_LEAST_ONE:
        return count >= 1;
      case SHOW_IF_AT_LEAST_TWO:
        return count >= 2;
    }
    return false;
  }
}
