package org.designup.picsou.gui.budget;

import com.jidesoft.swing.JideSplitPane;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.utils.ApplicationColors;
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
  private JEditorPane helpMessage;
  private SeriesEditionDialog seriesEditionDialog;
  private SeriesAmountEditionDialog seriesAmountEditionDialog;

  public BudgetView(GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);
    this.repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateHelpMessage();
        }
      }
    });

    seriesEditionDialog = new SeriesEditionDialog(repository, directory);
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
    addBudgetAreaView("projectsBudgetView", BudgetArea.SPECIAL, builder);
    addBudgetAreaView("savingsBudgetView", BudgetArea.SAVINGS, builder);

    builder.add("horizontalSplitPane", new JideSplitPane());
    builder.add("firstVerticalSplitPane", new JideSplitPane());
    builder.add("secondVerticalSplitPane", new JideSplitPane());
    builder.add("thirdVerticalSplitPane", new JideSplitPane());

    createHelpMessage();
    builder.add("helpMessage", helpMessage);
    builder.add("hideHelpMessage",
                new SetFieldValueAction(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE,
                                        false, repository));

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    parentBuilder.add("budgetView", builder);

  }

  private void addBudgetAreaView(String name,
                                 BudgetArea budgetArea,
                                 GlobsPanelBuilder builder) {
    View view = new BudgetAreaSeriesView(name, budgetArea, repository, directory,
                                         seriesEditionDialog, seriesAmountEditionDialog);
    view.registerComponents(builder);
  }

  private JEditorPane createHelpMessage() {
    helpMessage = new JEditorPane();
    helpMessage.setContentType("text/html");
    ApplicationColors.installLinkColor(helpMessage, "mainpanel", "mainpanel.message.link", directory);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE)) {
          updateHelpMessage();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateHelpMessage();
      }
    });
    updateHelpMessage();
    return helpMessage;
  }

  private void updateHelpMessage() {
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs != null) {
      helpMessage.setVisible(prefs.isTrue(UserPreferences.SHOW_BUDGET_VIEW_HELP_MESSAGE) &&
                             repository.contains(Series.TYPE,
                                                 not(fieldEquals(Series.ID,
                                                                 Series.UNCATEGORIZED_SERIES_ID))));
    }
  }
}
