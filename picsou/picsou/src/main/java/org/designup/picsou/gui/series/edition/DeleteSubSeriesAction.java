package org.designup.picsou.gui.series.edition;

import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class DeleteSubSeriesAction extends MultiSelectionAction {
  private JDialog owner;

  public DeleteSubSeriesAction(GlobRepository repository, Directory directory, JDialog owner) {
    super(SubSeries.TYPE, repository, directory);
    this.owner = owner;
  }

  protected String getLabel(GlobList selection) {
    return Lang.get("delete");
  }

  protected void processClick(GlobList subSeriesList, GlobRepository repository, Directory directory) {
    GlobList transactions =
      repository.getAll(Transaction.TYPE,
                        fieldIn(Transaction.SUB_SERIES,
                                subSeriesList.getValueSet(SubSeries.ID)));
    if (!transactions.isEmpty()) {
      DeleteSubSeriesDialog dialog = new DeleteSubSeriesDialog(subSeriesList,
                                                               repository, directory,
                                                               owner);
      dialog.show();
    }
    else {
      repository.delete(subSeriesList);
      setEnabled(false);
    }
  }
}