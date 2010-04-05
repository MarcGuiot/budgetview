package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.footers.BudgetAreaSeriesFooter;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.charts.GlobGaugeView;
import org.designup.picsou.gui.description.ForcedPlusGlobListStringifier;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
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
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private Matchers.SeriesFirstEndDateFilter seriesDateFilter;
  private List<Key> currentSeries = Collections.emptyList();

  private BudgetAreaHeader header;
  private BudgetAreaSeriesFooter footerGenerator;

  private Repeat<Glob> seriesRepeat;
  private GlobMatcher seriesFilter;
  private SeriesEditionButtons seriesButtons;
  private JEditorPane footerArea = GuiUtils.createReadOnlyHtmlComponent();

  private SeriesAmountEditionDialog seriesAmountEditionDialog;

  public BudgetAreaSeriesView(String name,
                              final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory,
                              BudgetAreaSeriesFooter footerGenerator,
                              final SeriesEditionDialog seriesEditionDialog,
                              final SeriesAmountEditionDialog seriesAmountEditionDialog) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.footerGenerator = footerGenerator;
    this.seriesAmountEditionDialog = seriesAmountEditionDialog;

    seriesButtons = new SeriesEditionButtons(budgetArea, repository, directory, seriesEditionDialog);

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat();
      }
    }, Month.TYPE);

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
    comparator = Collections.reverseOrder(comparator);
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
    footerGenerator.update(currentSeries);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    JLabel amountLabel = builder.add("totalObservedAmount", new JLabel()).getComponent();
    JLabel plannedLabel = builder.add("totalPlannedAmount", new JLabel()).getComponent();

    Gauge gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    builder.add("totalGauge", gauge);

    BudgetAreaHeaderUpdater headerUpdater =
      new BudgetAreaHeaderUpdater(TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                  repository, directory);
    headerUpdater.setColors("block.total",
                            "block.total.overrun.error",
                            "block.total.overrun.positive");

    this.header = new BudgetAreaHeader(budgetArea, headerUpdater, repository, directory);

    seriesRepeat =
      builder.addRepeat("seriesRepeat", new GlobList(), new SeriesRepeatComponentFactory());

    seriesButtons.registerButtons(builder);

    parentBuilder.add(name, builder);
    if (budgetArea == BudgetArea.SAVINGS) {
      seriesDateFilter =
        Matchers.seriesDateSavingsAndAccountFilter(Account.MAIN_SUMMARY_ACCOUNT_ID);
    }
    else {
      seriesDateFilter = Matchers.seriesDateFilter(budgetArea.getId(), false);
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
          if (seriesBudget.size() == 0 || !seriesBudget.getFirst().isTrue(SeriesBudget.ACTIVE)) {
            notActive++;
          }
        }
        return !(selectedMonthIds.size() == notActive) && seriesDateFilter.matches(series, repository);
      }
    };

    footerGenerator.init(footerArea);
    builder.add("footerArea", footerArea);
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
          repository.update(UserPreferences.KEY, UserPreferences.SHOW_ENVELOPES_EDITION_MESSAGE, false);
          seriesAmountEditionDialog.show(series, selectedMonthIds);
        }
      });

      final GlobGaugeView gaugeView =
        new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea, PeriodSeriesStat.AMOUNT,
                          PeriodSeriesStat.PLANNED_AMOUNT,
                          PeriodSeriesStat.PAST_REMAINING, PeriodSeriesStat.FUTURE_REMAINING,
                          PeriodSeriesStat.PAST_OVERRUN, PeriodSeriesStat.FUTURE_OVERRUN,
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
      final GlobButtonView amountButtonView =
        GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), callback)
          .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
      cellBuilder.add(name, amountButtonView.getComponent());
      cellBuilder.addDisposeListener(amountButtonView);
    }

    private GlobListStringifier getStringifier(final DoubleField field) {
      return new ForcedPlusGlobListStringifier(budgetArea,
                                               GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome()));
    }

  }
}
