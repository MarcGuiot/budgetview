package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BudgetAreaSeriesView extends View {
  private String name;
  private BudgetArea budgetArea;
  private GlobMatcher totalMatcher;

  protected BudgetAreaSeriesView(String name, BudgetArea budgetArea, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.budgetArea = budgetArea;
    this.totalMatcher = GlobMatchers.linkTargetFieldEquals(SeriesStat.SERIES, Series.BUDGET_AREA, budgetArea.getId());
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
                                          GlobLabelView.init(Series.TYPE, repository, directory)
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
}
