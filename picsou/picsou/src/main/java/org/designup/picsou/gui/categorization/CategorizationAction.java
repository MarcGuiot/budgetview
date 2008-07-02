package org.designup.picsou.gui.categorization;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.model.Transaction;

import java.awt.event.ActionEvent;
import java.awt.*;

public class CategorizationAction extends AbstractGlobSelectionAction {
  private GlobRepository repository;
  private Window parent;

  public CategorizationAction(GlobRepository repository, Directory directory, Window parent) {
    super(Transaction.TYPE, directory);
    this.repository = repository;
    this.parent = parent;
  }

  public void actionPerformed(ActionEvent e) {
    CategorizationDialog dialog = new CategorizationDialog(parent, repository, directory);
    dialog.show(lastSelection);
  }

  public String toString(GlobList globs) {
    return "categorize";
  }
}
