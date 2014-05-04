package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.summary.BudgetSummaryView;
import org.designup.picsou.gui.components.highlighting.HighlightingService;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.LayoutConfig;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class BudgetView extends View {

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
  }

  private static Directory createDirectory(Directory parentDirectory) {
    DefaultDirectory directory = new DefaultDirectory(parentDirectory);
    directory.add(new HighlightingService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetView.splits",
                                                      repository, directory);

    BudgetSummaryView budgetSummaryView = new BudgetSummaryView(repository, directory);
    budgetSummaryView.registerComponents(builder);

    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder);
    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder);
    addBudgetAreaView("variableBudgetView", BudgetArea.VARIABLE, builder);
    addBudgetAreaView("extrasBudgetView", BudgetArea.EXTRAS, builder);
    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder);

    builder.add("horizontalSplit", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_HORIZONTAL_1, LayoutConfig.BUDGET_HORIZONTAL_2));
    builder.add("verticalSplit1", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_LEFT));
    builder.add("verticalSplit2", SplitPaneConfig.create(directory, LayoutConfig.BUDGET_VERTICAL_CENTER));
    builder.add("verticalSplit3", new JideSplitPane());

    parentBuilder.add("budgetView", builder);

  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 GlobsPanelBuilder builder) {
    View view = new BudgetAreaSeriesView(name, budgetArea, repository, directory);
    view.registerComponents(builder);
  }
}
