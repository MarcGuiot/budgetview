package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
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
      }
    }, Month.TYPE);
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

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));
    addTotalLabel("totalObservedAmount", PeriodSeriesStat.AMOUNT, builder);
    addTotalLabel("totalPlannedAmount", PeriodSeriesStat.PLANNED_AMOUNT, builder);

    final GlobGaugeView gaugeView = new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea, PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT,
                                                      totalMatcher, repository, directory);
    builder.add("totalGauge", gaugeView.getComponent());

    seriesRepeat =
      builder.addRepeat("seriesRepeat",
                        new GlobList(),
                        new RepeatComponentFactory<Glob>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, final Glob periodSeriesStat) {
                            Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
                            final GlobButtonView globButtonView =
                              GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
                                .forceSelection(series);
                            cellBuilder.add("seriesName", globButtonView.getComponent());
                            addAmountButton("observedSeriesAmount", PeriodSeriesStat.AMOUNT, series, cellBuilder);
                            addAmountLabel("plannedSeriesAmount", PeriodSeriesStat.PLANNED_AMOUNT, series, cellBuilder);

                            final GlobGaugeView gaugeView =
                              new GlobGaugeView(PeriodSeriesStat.TYPE, budgetArea, PeriodSeriesStat.AMOUNT,
                                                PeriodSeriesStat.PLANNED_AMOUNT,
                                                GlobMatchers.fieldEquals(PeriodSeriesStat.SERIES, series.get(Series.ID)),
                                                repository, directory);
                            cellBuilder.add("gauge", gaugeView.getComponent());
                            cellBuilder.addDisposeListener(new Disposable() {
                              public void dispose() {
                                gaugeView.dispose();
                                globButtonView.dispose();
                              }
                            });
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
        return seriesDateFilter.matches(series, repository) &&
               (periodSeriesStat.get(PeriodSeriesStat.AMOUNT) != 0.0 ||
                periodSeriesStat.get(PeriodSeriesStat.PLANNED_AMOUNT) != 0.0);
      }
    };
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, PeriodSeriesStat.TYPE, getStringifier(field))
      .setFilter(totalMatcher);
  }

  private void addAmountLabel(String name, DoubleField field, Glob series, RepeatCellBuilder cellBuilder) {
    final GlobLabelView globLabelView = GlobLabelView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field))
      .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
    cellBuilder.add(name, globLabelView.getComponent());
    cellBuilder.addDisposeListener(new Disposable() {
      public void dispose() {
        globLabelView.dispose();
      }
    });
  }

  private void addAmountButton(String name, DoubleField field, final Glob series, RepeatCellBuilder cellBuilder) {
    final GlobButtonView globButtonView = GlobButtonView.init(PeriodSeriesStat.TYPE, repository, directory, getStringifier(field), new GlobListFunctor() {
      public void run(GlobList list, GlobRepository repository) {
        directory.get(NavigationService.class).gotoDataForSeries(series);
      }
    })
      .setFilter(GlobMatchers.linkedTo(series, PeriodSeriesStat.SERIES));
    cellBuilder.add(name, globButtonView.getComponent());
    cellBuilder.addDisposeListener(new Disposable() {
      public void dispose() {
        globButtonView.dispose();
      }
    });
  }

  private GlobListStringifier getStringifier(final DoubleField field) {
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
