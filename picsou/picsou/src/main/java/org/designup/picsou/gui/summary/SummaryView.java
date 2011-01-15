package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsBalanceChartView;
import org.designup.picsou.gui.accounts.chart.SavingsAccountsChartView;
import org.designup.picsou.gui.budget.AccountManagementMessage;
import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SummaryView extends View {
  private SelectionService selectionService;
  private JEditorPane messageField;

  public SummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(new TypeChangeSetListener(BudgetStat.TYPE) {
      protected void update(GlobRepository repository) {
        updateMessage();
      }
    });
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateMessage();
      }
    }, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/summaryView.splits",
                                                      repository, directory);
    MainDailyPositionsChartView mainDailyPositionsView = new MainDailyPositionsChartView(repository, directory);
    mainDailyPositionsView.registerComponents(builder);

    SavingsAccountsChartView savingsAccountsView = new SavingsAccountsChartView(repository, directory);
    savingsAccountsView.registerComponents(builder);

    SavingsAccountsBalanceChartView savingsAccountsBalanceView = new SavingsAccountsBalanceChartView(repository, directory);
    savingsAccountsBalanceView.registerComponents(builder);

    builder.add("gotoData", new GotoCardAction(Card.DATA, directory));
    builder.add("gotoBudget", new GotoCardAction(Card.BUDGET, directory));
    builder.add("gotoSavings", new GotoCardAction(Card.SAVINGS, directory));

    messageField = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("message", messageField);

    parentBuilder.add("summaryView", builder);
  }

  private void updateMessage() {
    GlobList months = selectionService.getSelection(Month.TYPE);
    if (months.isEmpty()) {
      messageField.setText("");
      return;
    }

    int lastMonthId = months.getSortedSet(Month.ID).last();
    Glob lastMonthStat = repository.find(Key.create(BudgetStat.TYPE, lastMonthId));
    Double minPosition = lastMonthStat.get(BudgetStat.MIN_POSITION);
    String text = AccountManagementMessage.getMessage(minPosition, repository);
    messageField.setText(text);
    GuiUtils.revalidate(messageField);
  }
}
