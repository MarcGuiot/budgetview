package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;
import java.util.Collections;
import java.awt.event.ActionEvent;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private GlobMatcher totalMatcher;
  private SeriesEditionDialog seriesEditionDialog;
  private Set<Integer> selectedMonthIds = Collections.emptySet();

  protected BudgetAreaSeriesView(String name, BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.totalMatcher = GlobMatchers.linkTargetFieldEquals(SeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId());
    seriesEditionDialog = new SeriesEditionDialog(directory.get(JFrame.class), repository, directory);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
      }
    }, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetAreaSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(stringify(budgetArea)));
    addTotalLabel("totalObservedAmount", SeriesStat.AMOUNT, builder);
    addTotalLabel("totalPlannedAmount", SeriesStat.PLANNED_AMOUNT, builder);

    final GlobGaugeView gaugeView = new GlobGaugeView(SeriesStat.TYPE, budgetArea, SeriesStat.AMOUNT, SeriesStat.PLANNED_AMOUNT,
                                                      totalMatcher, repository, directory);
    builder.add("totalGauge", gaugeView.getComponent());

    builder.addRepeat("seriesRepeat",
                      Series.TYPE,
                      GlobMatchers.fieldEquals(Series.BUDGET_AREA, budgetArea.getId()),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Glob series) {
                          cellBuilder.add("seriesName",
                                          GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
                                            .forceSelection(series)
                                            .getComponent());
                          addAmountLabel("observedSeriesAmount", SeriesStat.AMOUNT, series, cellBuilder);
                          addAmountLabel("plannedSeriesAmount", SeriesStat.PLANNED_AMOUNT, series, cellBuilder);

                          final GlobGaugeView gaugeView =
                            new GlobGaugeView(SeriesStat.TYPE, budgetArea, SeriesStat.AMOUNT, SeriesStat.PLANNED_AMOUNT,
                                              GlobMatchers.fieldEquals(SeriesStat.SERIES, series.get(Series.ID)),
                                              repository, directory);
                          cellBuilder.add("gauge", gaugeView.getComponent());

                          cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
                            public void dispose() {
                              gaugeView.dispose();
                            }
                          });
                        }
                      });

    builder.add("editAllSeries", new AbstractAction(Lang.get("budgetview.editAll")) {
      public void actionPerformed(ActionEvent e) {
        seriesEditionDialog.show(budgetArea, selectedMonthIds);
      }
    });

    parentBuilder.add(name, builder);
  }

  private String stringify(BudgetArea budgetArea) {
    return descriptionService.getStringifier(BudgetArea.TYPE).toString(budgetArea.getGlob(), repository);
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, SeriesStat.TYPE, getStringifier(field))
      .setFilter(totalMatcher);
  }

  private void addAmountLabel(String name, DoubleField field, Glob series, RepeatCellBuilder cellBuilder) {
    cellBuilder.add(name,
                    GlobLabelView.init(SeriesStat.TYPE, repository, directory, getStringifier(field))
                      .setFilter(GlobMatchers.linkedTo(series, SeriesStat.SERIES))
                      .getComponent());
  }

  private GlobListStringifier getStringifier(DoubleField field) {
    return GlobListStringifiers.sum(field, decimalFormat, !budgetArea.isIncome());
  }

  private class EditSeriesFunctor implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditionDialog.show(list.getFirst(), selectedMonthIds);
    }
  }
}
