package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.description.ForcedPlusGlobListStringifier;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private SeriesEditionDialog seriesEditionDialog;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private PicsouMatchers.SeriesFirstEndDateFilter seriesDateFilter;
  private GlobMatcher seriesFilter;
  private Repeat<Glob> seriesRepeat;
  private List<Key> currentSeries = Collections.emptyList();
  private BudgetAreaHeaderUpdater headerUpdater;
  private SeriesEditionButtons seriesButtons;

  public BudgetAreaSeriesView(String name,
                              final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory,
                              final SeriesEditionDialog seriesEditionDialog) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.seriesEditionDialog = seriesEditionDialog;

    seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory, seriesEditionDialog);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat();
        update();
      }
    }, Month.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceStat.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(BalanceStat.TYPE)) {
          update();
        }
      }
    });

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(PeriodSeriesStat.TYPE) ||
            changeSet.containsChanges(SeriesBudget.TYPE)
            || changeSet.containsChanges(Series.TYPE)) {
          updateRepeat();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(PeriodSeriesStat.TYPE) || changedTypes.contains(Series.TYPE)
            || changedTypes.contains(SeriesBudget.TYPE)) {
          updateRepeat();
        }
      }
    });
  }

  private void updateRepeat() {
    Comparator<Glob> comparator = new GlobFieldComparator(PeriodSeriesStat.ABS_SUM_AMOUNT);
    if (!budgetArea.isIncome()) {
      comparator = Collections.reverseOrder(comparator);
    }
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

  void update() {
    GlobList balanceStat = new GlobList();
    balanceStat.addAll(repository.getAll(BalanceStat.TYPE,
                                         GlobMatchers.fieldIn(BalanceStat.MONTH, selectedMonthIds)));
    headerUpdater.update(balanceStat, budgetArea);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    JLabel amountLabel = builder.add("totalObservedAmount", new JLabel());
    JLabel plannedLabel = builder.add("totalPlannedAmount", new JLabel());

    Gauge gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    builder.add("totalGauge", gauge);

    this.headerUpdater =
      new BudgetAreaHeaderUpdater(TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                  repository, directory);
    this.headerUpdater.setColors("block.total",
                                 "block.total.overrun.error",
                                 "block.total.overrun.positive");

    seriesRepeat =
      builder.addRepeat("seriesRepeat", new GlobList(), new SeriesRepeatComponentFactory());

    seriesButtons.registerButtons(builder);

    parentBuilder.add(name, builder);
    if (budgetArea == BudgetArea.SAVINGS) {
      seriesDateFilter =
        PicsouMatchers.seriesDateSavingsAndAccountFilter(Account.MAIN_SUMMARY_ACCOUNT_ID);
    }
    else {
      seriesDateFilter = PicsouMatchers.seriesDateFilter(budgetArea.getId(), false);
    }
    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
        Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
        ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgetIndex =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
        int notActive = 0;
        for (Integer monthId : selectedMonthIds) {
          GlobList seriesBudget =
            seriesBudgetIndex.findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
          if (seriesBudget.size() == 0 || !seriesBudget.getFirst().get(SeriesBudget.ACTIVE)) {
            notActive++;
          }
        }
        return !(selectedMonthIds.size() == notActive) && seriesDateFilter.matches(series, repository);
      }
    };
  }

  private class SeriesRepeatComponentFactory implements RepeatComponentFactory<Glob> {

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {

      final Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);

      GlobButtonView seriesNameButton = seriesButtons.createSeriesButton(series);
      cellBuilder.add("seriesName", seriesNameButton.getComponent());

      addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          directory.get(NavigationService.class).gotoDataForSeries(series);
        }
      });

      addAmountButton("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          seriesEditionDialog.show(series, selectedMonthIds);
        }
      });

      final GlobGaugeView gaugeView =
        new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea, PeriodSeriesStat.AMOUNT,
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
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), callback)
          .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
      cellBuilder.add(name, globButtonView.getComponent());
      cellBuilder.addDisposeListener(globButtonView);
    }

    private GlobListStringifier getStringifier(final DoubleField field) {
      return new ForcedPlusGlobListStringifier(budgetArea,
                                               GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome()));
    }

  }
}
