package org.designup.picsou.gui.series.edition;

import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteSubSeriesAction extends AbstractAction implements GlobSelectionListener {
  private GlobRepository repository;
  private Directory directory;
  private JDialog owner;
  private GlobList subSeriesList = GlobList.EMPTY;

  public DeleteSubSeriesAction(GlobRepository repository, Directory directory, JDialog owner) {
    super(Lang.get("delete"));
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
    directory.get(SelectionService.class).addListener(this, SubSeries.TYPE);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    subSeriesList = selection.getAll(SubSeries.TYPE);
    setEnabled(!subSeriesList.isEmpty());
  }

  public void actionPerformed(ActionEvent e) {
    GlobList transactions =
      repository.getAll(Transaction.TYPE,
                        GlobMatchers.fieldIn(Transaction.SUB_SERIES,
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
      subSeriesList = GlobList.EMPTY;
    }
  }
}