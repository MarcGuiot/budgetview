package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.series.SeriesDeletionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class SeriesDeletionHandler {

  private GlobList seriesToDelete = GlobList.EMPTY;

  private Window dialog;
  private LocalGlobRepository localRepository;
  private GlobRepository repository;
  private Directory directory;
  private Directory localDirectory;
  private SelectionService selectionService;

  public SeriesDeletionHandler(Window dialog,
                               LocalGlobRepository localRepository, GlobRepository repository,
                               Directory directory, Directory localDirectory, SelectionService selectionService) {
    this.dialog = dialog;
    this.localRepository = localRepository;
    this.repository = repository;
    this.directory = directory;
    this.localDirectory = localDirectory;
    this.selectionService = selectionService;
  }

  public void delete(Glob currentSeries, boolean closeWindowOnConfirm, boolean commitOnSetDate) {
    seriesToDelete = new GlobList();
    seriesToDelete.add(currentSeries);
    Glob mirrorSeries = repository.findLinkTarget(currentSeries, Series.MIRROR_SERIES);
    if (mirrorSeries != null) {
      seriesToDelete.add(mirrorSeries);
    }

    Set<Integer> seriesIds = seriesToDelete.getValueSet(Series.ID);
    GlobList transactionsForSeries = localRepository.getAll(Transaction.TYPE, fieldIn(Transaction.SERIES, seriesIds));
    final Ref<SeriesDeletionDialog.Action> action = new Ref<SeriesDeletionDialog.Action>(SeriesDeletionDialog.Action.CANCEL);
    if (transactionsForSeries.isEmpty()) {
      doDelete(action);
    }
    else if (BudgetArea.TRANSFER.getId().equals(currentSeries.get(Series.BUDGET_AREA))) {
      ConfirmationDialog confirmationDialog = new ConfirmationDialog("seriesDeletion.title",
                                                                     Lang.get("seriesDeletion.savings.message"),
                                                                     dialog, directory,
                                                                     ConfirmationDialog.Mode.STANDARD) {
        protected void processOk() {
          doDelete(action);
        }
      };
      confirmationDialog.show();
    }
    else {
      SeriesDeletionDialog seriesDeletionDialog =
        new SeriesDeletionDialog(currentSeries, transactionsForSeries, localRepository, localDirectory, dialog);
      action.set(seriesDeletionDialog.show());
      if (action.get() == SeriesDeletionDialog.Action.DELETE) {
        doDelete(action);
      }
    }

    if (action.get() == SeriesDeletionDialog.Action.DELETE
        || (commitOnSetDate && action.get() == SeriesDeletionDialog.Action.SET_DATE)) {
      localRepository.commitChanges(false);
      localRepository.rollback();
      if (closeWindowOnConfirm) {
        dialog.setVisible(false);
      }
    }
  }

  private void doDelete(Ref<SeriesDeletionDialog.Action> action) {
    GlobList tmp = new GlobList(seriesToDelete);
    selectionService.clear(Series.TYPE);
    localRepository.delete(tmp);
    action.set(SeriesDeletionDialog.Action.DELETE);
  }
}
