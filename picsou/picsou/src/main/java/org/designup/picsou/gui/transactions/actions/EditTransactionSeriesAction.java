package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class EditTransactionSeriesAction extends MultiSelectionAction {

  public EditTransactionSeriesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("transaction.editSeries"), Transaction.TYPE, repository, directory);
  }

  protected void processSelection(GlobList selection) {
    Set<Integer> seriesIds = selection.getValueSet(Transaction.SERIES);
    setEnabled((seriesIds.size() == 1) && !Series.isUncategorized(seriesIds.iterator().next()));
  }

  protected void processClick(GlobList selection, GlobRepository repository, Directory directory) {
    Integer seriesId = selection.getValueSet(Transaction.SERIES).iterator().next();
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    SeriesEditor.get(directory).showSeries(series, selection.getValueSet(Transaction.BUDGET_MONTH));
  }
}
