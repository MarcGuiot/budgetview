package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.budget.SeriesRepeatComponentFactory;
import org.designup.picsou.gui.model.PeriodOccasionalSeriesStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SavingsSeriesView implements Disposable {
  private Glob account;
  private GlobRepository repository;
  private Directory directory;
  private SeriesEditionDialog seriesEditionDialog;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private PicsouMatchers.SeriesFirstEndDateFilter seriesDateFilter;
  private GlobMatcher seriesFilter;
  private Repeat<Glob> seriesRepeat;
  private List<Key> currentSeries = Collections.emptyList();
  private GlobStringifier accountStringifier;
  private SelectionService selectionService;
  private Component panel;
  private GlobsPanelBuilder builder;
  private GlobSelectionListener selectionListener;

  public SavingsSeriesView(Glob account,
                           final GlobRepository repository,
                           Directory directory,
                           final SeriesEditionDialog seriesEditionDialog) {
    this.account = account;
    this.repository = repository;
    this.directory = directory;
    this.seriesEditionDialog = seriesEditionDialog;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionListener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat(repository);
      }
    };
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(PeriodSeriesStat.TYPE)
            || changeSet.containsChanges(PeriodOccasionalSeriesStat.TYPE)
            || changeSet.containsChanges(Series.TYPE)) {
          updateRepeat(repository);  // on passe the repository et non l'autre a cause du
//          updateRepeat(SavingsSeriesView.this.repository);  // on passe the repository et non l'autre a cause du
          // ReplicationGlobRepository : les listener sont enregistre sur les deux repository (le Replication et
          // l'original
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateRepeat(repository);
      }
    });
    selectionService.addListener(selectionListener, Month.TYPE);
    accountStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);
    registerComponents();
    selectionListener
      .selectionUpdated(GlobSelectionBuilder.create(selectionService.getSelection(Month.TYPE), Month.TYPE));
  }

  public Component getPanel() {
    return panel;
  }

  public void registerComponents() {
    builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaAccountSeriesView.splits",
                                    repository, directory);

    String accountName = Lang.get("budgetView.savings.accountName.from",
                                  accountStringifier.toString(account, repository));
    seriesRepeat =
      builder.addRepeat("seriesRepeat",
                        new GlobList(),
                        new SeriesRepeatComponentFactory(account, accountName, repository, directory, seriesEditionDialog));

    seriesDateFilter = PicsouMatchers.seriesDateSavingsAndAccountFilter(account.get(Account.ID));
    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
        Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
        ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgetIndex =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
        int notActive = 0;
        for (Integer monthId : selectedMonthIds) {
          GlobList seriesBudget =
            seriesBudgetIndex.findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
          if (seriesBudget.size() == 0 || !seriesBudget.get(0).get(SeriesBudget.ACTIVE)) {
            notActive++;
          }
        }
        return !(selectedMonthIds.size() == notActive) && seriesDateFilter.matches(series, repository);
      }
    };
    panel = builder.load();
  }

  private void updateRepeat(final GlobRepository repository) {
    Comparator<Glob> comparator = new GlobFieldComparator(PeriodSeriesStat.ABS_SUM_AMOUNT);
    List<Key> newSeries = repository.getAll(PeriodSeriesStat.TYPE, seriesFilter)
      .sort(comparator)
      .toKeyList();
    GlobUtils.diff(currentSeries, newSeries, new GlobUtils.DiffFunctor<Key>() {
      public void add(Key key, int index) {
        seriesRepeat.insert(repository.get(key), index);
      }

      public void remove(int index) {
        seriesRepeat.remove(index);
      }

      public void move(int previousIndex, int newIndex) {
        seriesRepeat.move(previousIndex, newIndex);
      }
    });
    currentSeries = newSeries;
  }

  public void dispose() {
    builder.dispose();
    selectionService.removeListener(selectionListener);
  }
}