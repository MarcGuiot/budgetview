package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.GlobGaugeView;
import org.designup.picsou.gui.model.OccasionalSeriesStat;
import org.designup.picsou.gui.model.PeriodOccasionalSeriesStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
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
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class OccasionalSeriesView extends View {
  private String name;
  private GlobMatcher totalMatcher;
  private Set<Integer> currentMonths = Collections.emptySet();
  private GlobList currentStat = GlobList.EMPTY;

  protected OccasionalSeriesView(String name, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.name = name;
    this.totalMatcher =
      GlobMatchers.linkTargetFieldEquals(PeriodSeriesStat.SERIES, Series.ID, Series.OCCASIONAL_SERIES_ID);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/occasionalSeriesView.splits",
                                                      repository, directory);

    builder.add("budgetAreaTitle", new JLabel(stringify(BudgetArea.OCCASIONAL)));
    addObservedTotalLabel("totalObservedAmount", PeriodSeriesStat.AMOUNT, builder);
    addPlannedTotalLabel("totalPlannedAmount", PeriodSeriesStat.PLANNED_AMOUNT, builder);

    final GlobGaugeView gaugeView = new GlobGaugeView(PeriodSeriesStat.TYPE, BudgetArea.OCCASIONAL,
                                                      PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT,
                                                      totalMatcher, repository, directory);
    builder.add("totalGauge", gaugeView.getComponent());

    final Repeat<Glob> repeat =
      builder.addRepeat("seriesRepeat",
                        new EmptyGlobList(),
                        new RepeatComponentFactory<Glob>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, Glob stat) {
                            Glob master = repository.findLinkTarget(stat, PeriodOccasionalSeriesStat.CATEGORY);
                            final GlobLabelView category = GlobLabelView.init(Category.TYPE, repository, directory)
                              .forceSelection(master);
                            JLabel label = category.getComponent();
                            cellBuilder.add("categoryName", label);
                            String categoryName = descriptionService.getStringifier(Category.TYPE).toString(master, repository);
                            label.setName("categoryName." + categoryName);

                            final GlobButtonView amountLabel = addAmountButton("observedCategoryAmount", master, cellBuilder, "amount." + categoryName);

                            cellBuilder.addDisposeListener(new Disposable() {
                              public void dispose() {
                                category.dispose();
                                amountLabel.dispose();
                              }
                            });
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
        if (changeSet.containsChanges(OccasionalSeriesStat.TYPE)) {
          updateFilterAndSelection(repeat);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });

    parentBuilder.add(name, builder);
  }

  private void updateFilterAndSelection(final Repeat<Glob> repeat) {
    GlobList stats =
      repository.getAll(OccasionalSeriesStat.TYPE, GlobMatchers.contained(OccasionalSeriesStat.MONTH, currentMonths));

    Set<Glob> newStats = new HashSet<Glob>();
    repository.enterBulkDispatchingMode();
    try {
      repository.deleteAll(PeriodOccasionalSeriesStat.TYPE);
      for (Glob stat : stats) {
        Glob periodeStat = repository.findOrCreate(Key.create(PeriodOccasionalSeriesStat.TYPE,
                                                              stat.get(OccasionalSeriesStat.CATEGORY)));
        newStats.add(periodeStat);
        repository.update(periodeStat.getKey(), PeriodOccasionalSeriesStat.AMOUNT,
                          periodeStat.get(PeriodOccasionalSeriesStat.AMOUNT) +
                          stat.get(OccasionalSeriesStat.AMOUNT));
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
    GlobList orderedStat = new GlobList(newStats);
    orderedStat.sort(new Comparator<Glob>() {
      public int compare(Glob o1, Glob o2) {
        return new Double(Math.abs(o2.get(PeriodOccasionalSeriesStat.AMOUNT)))
          .compareTo(Math.abs(o1.get(PeriodOccasionalSeriesStat.AMOUNT)));
      }
    });

    GlobUtils.diff(currentStat, orderedStat, new GlobUtils.DiffFunctor<Glob>() {
      public void add(Glob glob, int index) {
        repeat.insert(glob, index);
      }

      public void remove(int index) {
        repeat.remove(index);
      }

      public void move(int previousIndex, int newIndex) {
        repeat.move(previousIndex, newIndex);
      }
    });
    currentStat = orderedStat;
    selectionService.select(newStats, PeriodOccasionalSeriesStat.TYPE);
  }

  private String stringify(BudgetArea budgetArea) {
    return descriptionService.getStringifier(BudgetArea.TYPE).toString(budgetArea.getGlob(), repository);
  }

  private void addObservedTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    builder.addLabel(name, PeriodSeriesStat.TYPE,
                     new ForcedPlusGlobListStringifier(BudgetArea.OCCASIONAL,
                                                       GlobListStringifiers.sum(field, decimalFormat, true)))
      .setFilter(totalMatcher);
  }

  private void addPlannedTotalLabel(String name, DoubleField field, GlobsPanelBuilder builder) {
    final GlobListStringifier globListStringifier = GlobListStringifiers.sum(field, decimalFormat, true);

    builder.addLabel(name, PeriodSeriesStat.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        String amount = globListStringifier.toString(list, repository);
        if (amount.startsWith("-")) {
          amount = "0.0";
        }
        return amount;
      }
    })
      .setFilter(totalMatcher);
  }

  private GlobButtonView addAmountButton(String name, Glob master, RepeatCellBuilder cellBuilder, String amountName) {
    GlobListStringifier stringifier = GlobListStringifiers.sum(decimalFormat, PeriodOccasionalSeriesStat.AMOUNT);
    GlobButtonView view = GlobButtonView.init(PeriodOccasionalSeriesStat.TYPE, repository, directory, stringifier,
                                              new NavigateToTransactions(master));
    JButton label = view.setFilter(GlobMatchers.linkedTo(master, PeriodOccasionalSeriesStat.CATEGORY)).getComponent();
    cellBuilder.add(name, label);
    label.setName(amountName);
    return view;
  }

  private class NavigateToTransactions implements GlobListFunctor {
    private Glob masterCategory;

    public NavigateToTransactions(Glob masterCategory) {
      this.masterCategory = masterCategory;
    }

    public void run(GlobList periodOccasionalSeriesStatList, GlobRepository repository) {
      NavigationService navigationService = directory.get(NavigationService.class);
      navigationService.gotoData(BudgetArea.OCCASIONAL, masterCategory);
    }
  }
}