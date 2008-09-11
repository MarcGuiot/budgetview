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
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class OccasionalSeriesView extends View {
  private String name;
  private GlobMatcher totalMatcher;
  private Set<Integer> currentMonths = Collections.emptySet();

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
                            JLabel label = GlobLabelView.init(Category.TYPE, repository, directory)
                              .forceSelection(master)
                              .getComponent();
                            cellBuilder.add("categoryName", label);
                            String categoryName = descriptionService.getStringifier(Category.TYPE).toString(master, repository);
                            label.setName("categoryName." + categoryName);
                            addAmountLabel("observedCategoryAmount", master, cellBuilder, "amount." + categoryName);
                          }
                        });

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList months = selection.getAll(Month.TYPE);
        currentMonths = months.getValueSet(Month.ID);
        updateFilterAndSelection(repeat);
      }
    }, Month.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(OccasionalSeriesStat.TYPE)) {
          updateFilterAndSelection(repeat);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });

    parentBuilder.add(name, builder);
  }

  private void updateFilterAndSelection(GlobRepeat repeat) {
    GlobList stats =
      repository.getAll(OccasionalSeriesStat.TYPE, GlobMatchers.contained(OccasionalSeriesStat.MONTH, currentMonths));
    Set<Integer> masterIds = stats.getValueSet(OccasionalSeriesStat.CATEGORY);
    repeat.setFilter(GlobMatchers.contained(Category.ID, masterIds));

    selectionService.select(stats, OccasionalSeriesStat.TYPE);
  }

  private String stringify(BudgetArea budgetArea) {
    return descriptionService.getStringifier(BudgetArea.TYPE).toString(budgetArea.getGlob(), repository);
  }

  private void addTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, SeriesStat.TYPE,
                     GlobListStringifiers.sum(field, decimalFormat, true))
      .setFilter(totalMatcher);
  }

  private void addAmountLabel(String name, Glob master, RepeatCellBuilder cellBuilder, String amountName) {
    GlobListStringifier stringifier = GlobListStringifiers.sum(decimalFormat, OccasionalSeriesStat.AMOUNT);
    JLabel label = GlobLabelView.init(OccasionalSeriesStat.TYPE, repository, directory, stringifier)
      .setFilter(GlobMatchers.linkedTo(master, OccasionalSeriesStat.CATEGORY))
      .getComponent();
    cellBuilder.add(name, label);
    label.setName(amountName);
  }
}