package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SavingsSeriesView {
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
  private GlobStringifier seriesStringifier;
  private JLabel accountName;

  protected SavingsSeriesView(Glob account, final GlobRepository repository,
                              Directory directory, final SeriesEditionDialog seriesEditionDialog) {
    this.account = account;
    this.repository = repository;
    this.directory = directory;
    this.seriesEditionDialog = seriesEditionDialog;
    selectionService = directory.get(SelectionService.class);
    selectionListener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat(repository);
      }
    };
    selectionService.addListener(selectionListener, Month.TYPE);
    accountStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);
    seriesStringifier = directory.get(DescriptionService.class).getStringifier(Series.TYPE);
    registerComponents();
    selectionListener
      .selectionUpdated(GlobSelectionBuilder.create(selectionService.getSelection(Month.TYPE), Month.TYPE));
  }

  public Component getPanel() {
    return panel;
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
    accountName.setVisible(!currentSeries.isEmpty());
  }

  public void registerComponents() {
    builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaAccountSeriesView.splits",
                                    repository, directory);

    String accountName = Lang.get("budgetView.savings.accountName.from",
                                  accountStringifier.toString(account, repository));
    this.accountName = new JLabel(accountName);
    builder.add("accountName", this.accountName);
    seriesRepeat =
      builder.addRepeat("seriesRepeat",
                        new GlobList(),
                        new SeriesRepeatComponentFactory(accountName));

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

  public void dispose() {
    builder.dispose();
    selectionService.removeListener(selectionListener);
  }

  private class SeriesRepeatComponentFactory implements RepeatComponentFactory<Glob> {
    private String accountName;

    public SeriesRepeatComponentFactory(String accountName) {
      this.accountName = accountName;
    }


    private class EditSeriesFunctor implements GlobListFunctor {
      public void run(GlobList list, GlobRepository repository) {
        showSeriesEdition(list.getFirst());
      }
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {

      final Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);

      final GlobButtonView seriesNameButton =
        GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
          .setName(accountName + "." + seriesStringifier.toString(series, repository))
          .forceSelection(series);
      cellBuilder.add("seriesName", seriesNameButton.getComponent());

      addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });

      addAmountButton("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          showSeriesEdition(series);
        }
      });

      final GlobGaugeView gaugeView =
        new GlobGaugeView(PeriodSeriesStat.TYPE, BudgetArea.SAVINGS, PeriodSeriesStat.AMOUNT,
                          PeriodSeriesStat.PLANNED_AMOUNT,
                          GlobMatchers.fieldEquals(PeriodSeriesStat.SERIES, series.get(Series.ID)),
                          repository, directory);
      cellBuilder.add("gauge", gaugeView.getComponent());

      cellBuilder.addDisposeListener(gaugeView);
      cellBuilder.addDisposeListener(seriesNameButton);
    }

    private void addAmountButton(String name,
                                 DoubleField field,
                                 final Glob series,
                                 RepeatCellBuilder cellBuilder,
                                 final GlobListFunctor callback) {
      final GlobButtonView globButtonView =
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(series, field), callback)
          .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
      cellBuilder.add(name, globButtonView.getComponent());
      cellBuilder.addDisposeListener(globButtonView);
    }

    private GlobListStringifier getStringifier(Glob series, final DoubleField field) {
      final double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                                                               repository.findLinkTarget(series, Series.TO_ACCOUNT),
                                                                               account);
      return new GlobListStringifier() {
        public String toString(GlobList list, GlobRepository repository) {
          Glob first = list.getFirst();
          if (first == null) {
            return "";
          }
          Double amount = Math.abs(first.get(field));
          return Formatting.DECIMAL_FORMAT.format(Amounts.isNearZero(amount) ? 0 : multiplier * amount);
        }
      };
    }

    private void showSeriesEdition(Glob series) {
      seriesEditionDialog.show(series, selectedMonthIds);
    }
  }
}