package com.budgetview.desktop.budget;

import com.budgetview.desktop.View;
import com.budgetview.desktop.accounts.utils.AccountFilter;
import com.budgetview.desktop.components.filtering.FilterManager;
import com.budgetview.desktop.components.filtering.Filterable;
import com.budgetview.desktop.components.filtering.components.FilterMessagePanel;
import com.budgetview.desktop.components.layoutconfig.SplitPaneConfig;
import com.budgetview.desktop.series.UncategorizedSummaryView;
import com.budgetview.model.BudgetArea;
import com.budgetview.model.LayoutConfig;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetView extends View {

  private FilterManager filterManager;

  public BudgetView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetView.splits",
                                                      repository, directory);

    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder);
    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder);
    addBudgetAreaView("variableBudgetView", BudgetArea.VARIABLE, builder);
    addBudgetAreaView("extrasBudgetView", BudgetArea.EXTRAS, builder);
    addBudgetAreaView("savingsBudgetView", BudgetArea.TRANSFER, builder);

    builder.add("horizontalSplit", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_HORIZONTAL_1));
    builder.add("verticalSplit1", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_LEFT_1, LayoutConfig.BUDGET_VERTICAL_LEFT_2));
    builder.add("verticalSplit2", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_RIGHT_1));

    UncategorizedSummaryView uncategorized = new UncategorizedSummaryView(repository, directory);
    uncategorized.registerComponents(builder);

    filterManager = new FilterManager(Filterable.NO_OP);
    AccountFilter.initForPeriodStat(filterManager, repository, directory);

    FilterMessagePanel accountFilterMessage = new FilterMessagePanel(filterManager, repository, directory);
    builder.add("accountFilterMessage", accountFilterMessage.getPanel());

    parentBuilder.add("budgetView", builder);
  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 GlobsPanelBuilder builder) {
    BudgetAreaSeriesView view = new BudgetAreaSeriesView(name, budgetArea, repository, directory);
    view.registerComponents(builder);
  }

  public void reset() {
    filterManager.reset();
  }
}
