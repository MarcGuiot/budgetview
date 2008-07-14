package org.designup.picsou.gui.categorization;

import org.designup.picsou.model.Transaction;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

import java.awt.event.ActionEvent;

public class CategorizationAction extends AbstractGlobSelectionAction {

  public CategorizationAction(Directory directory) {
    super(Transaction.TYPE, directory);
  }

  public void actionPerformed(ActionEvent e) {
    CategorizationDialog dialog = directory.get(CategorizationDialog.class);
    dialog.show(lastSelection, true);
  }

  public String toString(GlobList globs) {
    return "categorize";
  }
}
