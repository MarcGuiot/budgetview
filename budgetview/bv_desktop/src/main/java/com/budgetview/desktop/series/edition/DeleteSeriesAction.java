package com.budgetview.desktop.series.edition;

import com.budgetview.desktop.series.SeriesEditionDialog;
import com.budgetview.desktop.series.utils.SeriesDeletionHandler;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.*;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.isNotTrue;

public class DeleteSeriesAction extends AbstractAction {
  private Key seriesKey;
  private final Window owner;
  private final GlobRepository parentRepository;
  private final Directory directory;

  public DeleteSeriesAction(Key seriesKey, Window owner, GlobRepository repository, Directory directory) {
    super(Lang.get("seriesEdition.deleteCurrent"));
    this.seriesKey = seriesKey;
    this.owner = owner;
    this.parentRepository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {

    LocalGlobRepository localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(BudgetArea.TYPE, Month.TYPE, CurrentMonth.TYPE,
              Account.TYPE, SubSeries.TYPE,
              Bank.TYPE, BankEntity.TYPE)
        .get();

    SeriesEditionDialog.loadSeries(localRepository, parentRepository);

    Glob series = localRepository.get(seriesKey);
    Set<Integer> seriesIds = new HashSet<Integer>();
    seriesIds.add(series.get(Series.ID));
    if (series.get(Series.MIRROR_SERIES) != null) {
      seriesIds.add(series.get(Series.MIRROR_SERIES));
    }
    localRepository.reset(parentRepository.getAll(Transaction.TYPE,
                                                  and(TransactionMatchers.transactionsForSeries(seriesIds),
                                                      isNotTrue(Transaction.PLANNED))),
                          Transaction.TYPE);

    SeriesDeletionHandler handler = new SeriesDeletionHandler(owner,
                                                              localRepository, parentRepository,
                                                              directory, directory, directory.get(SelectionService.class));
    handler.delete(series, false, true);
  }
}
