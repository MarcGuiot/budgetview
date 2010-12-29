package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.summary.BudgetSummaryView;
import org.designup.picsou.gui.budget.footers.BudgetAreaSeriesFooter;
import org.designup.picsou.gui.budget.footers.EmptyBudgetAreaSeriesFooter;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetView extends View {
  private SeriesEditor seriesEditor;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);

    seriesEditor = directory.get(SeriesEditor.class);
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

    builder.add("horizontalSplitPane", new JideSplitPane());
    builder.add("firstVerticalSplitPane", new JideSplitPane());
    builder.add("secondVerticalSplitPane", new JideSplitPane());
    builder.add("thirdVerticalSplitPane", new JideSplitPane());

    parentBuilder.add("budgetView", builder);

  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 GlobsPanelBuilder builder) {
    addBudgetAreaView(name, budgetArea, new EmptyBudgetAreaSeriesFooter(), builder);
  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 BudgetAreaSeriesFooter footer,
                                 GlobsPanelBuilder builder) {
    View view = new BudgetAreaSeriesView(name, budgetArea, repository, directory, footer, seriesEditor);
    view.registerComponents(builder);

  }
}
