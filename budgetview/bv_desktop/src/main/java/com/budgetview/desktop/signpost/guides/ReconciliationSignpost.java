package com.budgetview.desktop.signpost.guides;

import com.budgetview.model.SignpostStatus;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ReconciliationSignpost extends AbstractTableSignpost {

  public ReconciliationSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.FIRST_RECONCILIATION_DONE, repository, directory);
  }

  protected void update(JTable table) {
    if (!SignpostStatus.isCompleted(SignpostStatus.FIRST_RECONCILIATION_SHOWN, repository)) {
      return;
    }

    int rowCount = table.getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }

    boolean selection = table.getSelectedRows().length > 0;
    if (!selection && canShow()) {
      show(Lang.get("reconciliation.annotation.signpost"));
    }
    else if (selection && isShowing()) {
      dispose();
    }
  }

  protected int getColumn() {
    return 0;
  }
}
