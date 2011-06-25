package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CategorizationSelectionSignpost extends AbstractTableSignpost {

  public CategorizationSelectionSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.CATEGORIZATION_SELECTION_DONE, repository, directory);
  }

  protected void update(JTable table) {
    if (!SignpostStatus.isCompleted(SignpostStatus.GOTO_CATEGORIZATION_DONE, repository)) {
      return;
    }

    int rowCount = table.getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }

    boolean selection = table.getSelectedRows().length > 0;
    if (!selection && canShow()) {
      show(Lang.get("signpost.categorizationSelection"));
    }
    else if (selection && isShowing()) {
      dispose();
    }
  }
}
