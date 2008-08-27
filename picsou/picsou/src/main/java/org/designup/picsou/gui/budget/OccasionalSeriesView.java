package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.OccasionalSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class OccasionalSeriesView extends View {
  private String name;
  private GlobMatcher totalMatcher;

  protected OccasionalSeriesView(String name, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.totalMatcher =
      GlobMatchers.linkTargetFieldEquals(SeriesStat.SERIES, Series.ID, Series.OCCASIONAL_SERIES_ID);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/occasionalSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(stringify(BudgetArea.OCCASIONAL_EXPENSES)));
    addTotalLabel("totalObservedAmount", SeriesStat.AMOUNT, builder);
    addTotalLabel("totalPlannedAmount", SeriesStat.PLANNED_AMOUNT, builder);

    final GlobGaugeView gaugeView = new GlobGaugeView(SeriesStat.TYPE, BudgetArea.OCCASIONAL_EXPENSES,
                                                      SeriesStat.AMOUNT, SeriesStat.PLANNED_AMOUNT,
                                                      totalMatcher, repository, directory);
    builder.add("totalGauge", gaugeView.getComponent());

    final GlobRepeat repeat =
      builder.addRepeat("seriesRepeat",
                        Category.TYPE,
                        GlobMatchers.NONE,
                        new RepeatComponentFactory<Glob>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, Glob master) {
                            cellBuilder.add("categoryName",
                                            GlobLabelView.init(Category.TYPE, repository, directory)
                                              .forceSelection(master)
                                              .getComponent());
                            addAmountLabel("observedCategoryAmount", master, cellBuilder);
                          }
                        });

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList months = selection.getAll(Month.TYPE);
        Set<Integer> monthIds = months.getValueSet(Month.ID);
        GlobList stats =
          repository.getAll(OccasionalSeriesStat.TYPE, GlobMatchers.contained(OccasionalSeriesStat.MONTH, monthIds));
        Set<Integer> masterIds = stats.getValueSet(OccasionalSeriesStat.CATEGORY);
        repeat.setFilter(GlobMatchers.contained(Category.ID, masterIds));

        selectionService.select(stats, OccasionalSeriesStat.TYPE);
      }
    }, Month.TYPE);

    parentBuilder.add(name, builder);
  }

  private String stringify(BudgetArea budgetArea) {
    return descriptionService.getStringifier(BudgetArea.TYPE).toString(budgetArea.getGlob(), repository);
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, SeriesStat.TYPE,
                     GlobListStringifiers.sum(decimalFormat, field))
      .setFilter(totalMatcher);
  }

  private void addAmountLabel(String name, Glob master, RepeatCellBuilder cellBuilder) {
    GlobListStringifier stringifier = GlobListStringifiers.sum(decimalFormat, OccasionalSeriesStat.AMOUNT);
    cellBuilder.add(name,
                    GlobLabelView.init(OccasionalSeriesStat.TYPE, repository, directory, stringifier)
                      .setFilter(GlobMatchers.linkedTo(master, OccasionalSeriesStat.CATEGORY))
                      .getComponent());
  }
}