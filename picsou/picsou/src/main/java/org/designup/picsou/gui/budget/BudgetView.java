package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.series.UncategorizedSummaryView;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.LayoutConfig;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetView extends View {

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
    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder);

    builder.add("horizontalSplit", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_HORIZONTAL_1));
    builder.add("verticalSplit1", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_LEFT_1, LayoutConfig.BUDGET_VERTICAL_LEFT_2));
    builder.add("verticalSplit2", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_RIGHT_1));

    UncategorizedSummaryView uncategorized = new UncategorizedSummaryView(repository, directory);
    uncategorized.registerComponents(builder);

    parentBuilder.add("budgetView", builder);
  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 GlobsPanelBuilder builder) {
    BudgetAreaSeriesView view = new BudgetAreaSeriesView(name, budgetArea, repository, directory);
    view.registerComponents(builder);
  }
}
