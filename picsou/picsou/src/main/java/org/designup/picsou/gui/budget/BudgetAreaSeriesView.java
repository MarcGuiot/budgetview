package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.description.ForcedPlusGlobListStringifier;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.EditSeriesAction;
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
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private GlobMatcher totalMatcher;
  private SeriesEditionDialog seriesEditionDialog;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private PicsouMatchers.SeriesFirstEndDateFilter seriesDateFilter;
  private GlobMatcher seriesFilter;
  private Repeat<Glob> seriesRepeat;
  private List<Key> currentSeries = Collections.emptyList();
  private Gauge gauge;
  private JLabel plannedLabel;
  private JLabel amountLabel;

  protected BudgetAreaSeriesView(String name, final BudgetArea budgetArea, final GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.totalMatcher = GlobMatchers.linkTargetFieldEquals(PeriodSeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId());
    seriesEditionDialog = new SeriesEditionDialog(directory.get(JFrame.class), repository, directory);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        seriesDateFilter.filterDates(selectedMonthIds);
        updateRepeat(repository);
        update();
      }
    }, Month.TYPE);
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceStat.TYPE)) {
          update();
        }
      }
    });
  }

  private void updateRepeat(final GlobRepository repository) {
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
    Double remaining = 0.0;
    Double observed = 0.0;
    Double planned = 0.0;
    int multiplier = budgetArea.isIncome() ? 1 : -1;

    Glob currentMonth = repository.get(CurrentMonth.KEY);

    double overrunPart = 0;
    for (Glob balanceStat : repository.getAll(BalanceStat.TYPE,
                                              GlobMatchers.fieldIn(BalanceStat.MONTH, selectedMonthIds))) {
      observed += multiplier * balanceStat.get(BalanceStat.getObserved(budgetArea));
      planned += multiplier * balanceStat.get(BalanceStat.getPlanned(budgetArea));
      remaining += multiplier * balanceStat.get(BalanceStat.getRemaining(budgetArea));
      if (balanceStat.get(BalanceStat.MONTH) >= currentMonth.get(CurrentMonth.MONTH_ID)) {
        overrunPart += multiplier * (planned - (remaining + observed));
      }
    }
    amountLabel.setText(ForcedPlusGlobListStringifier.toString(observed, budgetArea));
    amountLabel.setVisible(true);
    amountLabel.setBackground(Color.RED);

    double adjustedPlanned = overrunPart > 10E-6 ? planned + overrunPart : planned;
    plannedLabel.setText(ForcedPlusGlobListStringifier.toString(adjustedPlanned, budgetArea));
    gauge.setValues(observed, planned, overrunPart);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    amountLabel = builder.add("totalObservedAmount", new JLabel());
    plannedLabel = builder.add("totalPlannedAmount", new JLabel());

    gauge = BudgetAreaGaugeFactory.createGauge(budgetArea);
    builder.add("totalGauge", gauge);

    seriesRepeat =
      builder.addRepeat("seriesRepeat",
                        new GlobList(),
                        new RepeatComponentFactory<Glob>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {

                            final Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);

                            final GlobButtonView seriesNameButton =
                              GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
                                .forceSelection(series);
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
                        });

    builder.add("createSeries", new CreateSeriesAction());

    builder.add("editAllSeries",
                new EditSeriesAction(repository, directory, seriesEditionDialog, budgetArea));

    parentBuilder.add(name, builder);
    seriesDateFilter = PicsouMatchers.seriesDateFilter(budgetArea.getId(), false);
    seriesFilter = new GlobMatcher() {
      public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
        Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
        ReadOnlyGlobRepository.MultiFieldIndexed seruesBudgetIndex = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                                                            series.get(Series.ID));
        int notActive = 0;
        for (Integer monthId : selectedMonthIds) {
          GlobList seriesBudget =
            seruesBudgetIndex.findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
          if (seriesBudget.size() == 0 || !seriesBudget.get(0).get(SeriesBudget.ACTIVE)) {
            notActive++;
          }
        }
        return !(selectedMonthIds.size() == notActive) && seriesDateFilter.matches(series, repository);
      }
    };
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, PeriodSeriesStat.TYPE, getStringifier(field))
      .setFilter(totalMatcher);
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

  private GlobListStringifier getPlannedAmountStringifier(final DoubleField field) {
    return new ForcedPlusGlobListStringifier(budgetArea,
                                             GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome()));
  }

  private class EditSeriesFunctor implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditionDialog.show(list.getFirst(), selectedMonthIds);
    }
  }

  private class CreateSeriesAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.showNewSeries(GlobList.EMPTY, budgetArea);
    }
  }
}
