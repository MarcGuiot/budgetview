package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.budget.summary.BudgetSummaryView;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.SetFieldValueAction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class BudgetView extends View {
  private SeriesEditionDialog seriesEditionDialog;
  private SeriesAmountEditionDialog seriesAmountEditionDialog;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);

    seriesEditionDialog = directory.get(SeriesEditionDialog.class);
    seriesAmountEditionDialog = new SeriesAmountEditionDialog(repository, directory, seriesEditionDialog);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budgetView.splits",
                                                      repository, directory);

    BudgetSummaryView budgetSummaryView = new BudgetSummaryView(repository, directory);
    budgetSummaryView.registerComponents(builder);

    addBudgetAreaView("incomeBudgetView", BudgetArea.INCOME, builder);
    addBudgetAreaView("recurringBudgetView", BudgetArea.RECURRING, builder);
    addBudgetAreaView("envelopeBudgetView", BudgetArea.ENVELOPES, builder);
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
    View view = new BudgetAreaSeriesView(name, budgetArea, repository, directory,
                                         seriesEditionDialog, seriesAmountEditionDialog);
    view.registerComponents(builder);
  }
}
