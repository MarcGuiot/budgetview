package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;

public class MonthSummaryView extends View implements GlobSelectionListener {
  private Directory localDirectory;

  public MonthSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/monthSummaryView.splits", repository, localDirectory);
    builder.addLabel("totalReceivedAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_RECEIVED));
    builder.addLabel("totalSpentAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_SPENT));
    builder.add("totalBalance",
                new BalanceGraph(MonthStat.TYPE, MonthStat.TOTAL_RECEIVED, MonthStat.TOTAL_SPENT, localDirectory));
    builder.addRepeat("budgetAreaRepeat", Arrays.asList(BudgetArea.values()),
                      new RepeatComponentFactory<BudgetArea>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {
                          final DoubleField expenseField = MonthStat.getReceived(budgetArea);
                          final DoubleField spentField = MonthStat.getSpent(budgetArea);
                          final DoubleField plannedExpenseField = MonthStat.getPlannedReceived(budgetArea);
                          final DoubleField plannedSpentField = MonthStat.getPlannedReceived(budgetArea);
                          JLabel label = new JLabel(descriptionService.getStringifier(BudgetArea.TYPE)
                            .toString(budgetArea.getGlob(), repository));
                          cellBuilder.add("budgetAreaName", label);
                          label.setName(budgetArea.getGlob().get(BudgetArea.NAME));
                          cellBuilder.add("budgetAreaAmount",
                                          GlobLabelView.init(MonthStat.TYPE, repository, localDirectory,
                                                             GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT,
                                                                                      expenseField, spentField)).getComponent());
                          cellBuilder.add("budgetAreaPlannedAmount",
                                          GlobLabelView.init(MonthStat.TYPE, repository, localDirectory,
                                                             GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT,
                                                                                      plannedExpenseField, plannedSpentField)).getComponent());
                        }
                      });
    parentBuilder.add("monthSummaryView", builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    GlobList selectedMonthStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(
        repository.getAll(MonthStat.TYPE,
                          GlobMatchers.and(GlobMatchers.fieldEquals(MonthStat.MONTH, monthId),
                                           GlobMatchers.fieldEquals(MonthStat.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                                           GlobMatchers.fieldEquals(MonthStat.CATEGORY, Category.ALL))));
    }
    SelectionService localSelectionService = localDirectory.get(SelectionService.class);
    localSelectionService.select(selectedMonthStats, MonthStat.TYPE);
    localSelectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
  }
}
