package org.designup.picsou.gui.signpost.guides;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class CategorizationSelectionSignpost extends AbstractTableSignpost {
  private int index;

  public CategorizationSelectionSignpost(GlobRepository repository, Directory directory) {
    super(SignpostStatus.CATEGORIZATION_SELECTION_DONE, repository, directory);
  }

  protected void update(JTable table) {
    index = 0;
    if (!SignpostStatus.isCompleted(SignpostStatus.GOTO_CATEGORIZATION_DONE, repository)) {
      return;
    }

    int rowCount = table.getModel().getRowCount();
    if (rowCount == 0) {
      return;
    }

    boolean selection = table.getSelectedRows().length > 0;
    if (!selection && canShow()) {
      String text = Lang.get("signpost.categorizationSelection");
      if (table.getRowCount() != 0) {
        for (; index < table.getRowCount(); ++index) {
          Glob transaction = (Glob)table.getModel().getValueAt(index, 0);
          if (Transaction.isUncategorized(transaction)) {
            Rectangle rect = table.getCellRect(index, 0, true);
            table.scrollRectToVisible(rect);
            break;
          }
        }
        if (index == table.getRowCount()){
          index = 0;
        }
        Glob transaction = (Glob)table.getModel().getValueAt(index, 0);
        Integer seriesId = transaction.get(Transaction.SERIES);
        if (!seriesId.equals(Series.UNCATEGORIZED_SERIES_ID)) {
          text = Lang.get("signpost.categorizationSelection.alreadySelected");
        }
      }
      show(text);
    }
    else if (selection && isShowing()) {
      dispose();
    }
  }

  protected int getRow() {
    return index;
  }
}
