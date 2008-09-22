package org.designup.picsou.gui.graphics;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.gui.components.StackChart;
import org.designup.picsou.gui.components.StackChartElement;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class CategoriesChart extends View implements GlobSelectionListener, ChangeSetListener {
  private TransactionSelection transactionSelection;
  protected GlobStringifier categoryStringifier;
  private SortedSet<StackChartElement> chartElements = new TreeSet<StackChartElement>();
  private StackChart chart = new StackChart();

  public CategoriesChart(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    this.transactionSelection = transactionSelection;
    this.transactionSelection.addListener(this);
    this.categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    repository.addChangeListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    chart.setOpaque(false);
    builder.add("categoriesChart", chart);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(MonthStat.TYPE)) {
      update();
    }
    if (changeSet.containsChanges(Category.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  private void update() {
    Map<Integer, Double> categoryIdToExpenses = new HashMap<Integer, Double>();
    GlobList monthStats = transactionSelection.getMonthStatsForAllMasterCategories();
    for (Glob monthStat : monthStats) {
      Integer categoryId = monthStat.get(MonthStat.CATEGORY);
      if (MasterCategory.ALL.getId().equals(categoryId)) {
        continue;
      }
      Double expenses = monthStat.get(MonthStat.TOTAL_SPENT);
      Double expensesForCategory = categoryIdToExpenses.get(categoryId);
      if (expensesForCategory == null) {
        expensesForCategory = 0.0;
      }
      categoryIdToExpenses.put(categoryId, expensesForCategory + expenses);
    }

    chartElements.clear();
    for (Glob master : repository.getAll(Category.TYPE, PicsouMatchers.masterCategories())) {
      Integer categoryId = master.get(Category.ID);
      if (categoryId.equals(MasterCategory.ALL.getId())) {
        continue;
      }

      Double value = categoryIdToExpenses.get(categoryId);
      String label =
        categoryStringifier.toString(repository.get(Key.create(Category.TYPE, categoryId)), repository);
      boolean isSelected = transactionSelection.isCategorySelected(categoryId);
      chartElements.add(new StackChartElement(label, value, isSelected));
    }

    chart.setValues(chartElements);
  }
}
