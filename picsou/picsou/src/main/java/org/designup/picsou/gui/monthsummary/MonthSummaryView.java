package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.BudgetStat;
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
  public MonthSummaryView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    parentDirectory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/monthSummaryView.splits", repository, directory);
    builder.addLabel("totalReceivedAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_RECEIVED));
    builder.addLabel("totalSpentAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_SPENT));
    builder.add("totalBalance",
                new BalanceGraph(MonthStat.TYPE, MonthStat.TOTAL_RECEIVED, MonthStat.TOTAL_SPENT, directory));
    builder.addRepeat("budgetAreaRepeat", Arrays.asList(BudgetArea.values()),
                      new RepeatComponentFactory<BudgetArea>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {
                          final DoubleField expenseField = MonthStat.getReceived(budgetArea);
                          final DoubleField spentField = MonthStat.getSpent(budgetArea);
                          JLabel label = new JLabel(descriptionService.getStringifier(BudgetArea.TYPE)
                            .toString(budgetArea.getGlob(), repository));
                          cellBuilder.add("budgetAreaName", label);
                          label.setName(budgetArea.getGlob().get(BudgetArea.NAME));
                          cellBuilder.add("budgetAreaAmount",
                                          GlobLabelView.init(MonthStat.TYPE, repository, directory,
                                                             GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT,
                                                                                      expenseField, spentField)).getComponent());
                          cellBuilder.add("budgetAreaPlannedAmount",
                                          GlobLabelView.init(BudgetStat.TYPE, repository, directory,
                                                             GlobListStringifiers.conditionnalSum(
                                                               GlobMatchers.fieldEquals(BudgetStat.BUDGET_AREA,
                                                                                        budgetArea.getId()),
                                                               PicsouDescriptionService.DECIMAL_FORMAT,
                                                               BudgetStat.AMOUNT)).getComponent());
                        }
                      });
    parentBuilder.add("monthSummaryView", builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    GlobList selectedMonthStats = new GlobList();
    GlobList selectedBudgetStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(
        repository.getAll(MonthStat.TYPE,
                          GlobMatchers.and(GlobMatchers.fieldEquals(MonthStat.MONTH, monthId),
                                           GlobMatchers.fieldEquals(MonthStat.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                                           GlobMatchers.fieldEquals(MonthStat.CATEGORY, Category.ALL))));
      selectedBudgetStats.addAll(
        repository.getAll(BudgetStat.TYPE, GlobMatchers.fieldEquals(BudgetStat.MONTH, monthId)));
    }
    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(selectedMonthStats, MonthStat.TYPE);
    localSelectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
    localSelectionService.select(selectedBudgetStats, BudgetStat.TYPE);
  }
}
