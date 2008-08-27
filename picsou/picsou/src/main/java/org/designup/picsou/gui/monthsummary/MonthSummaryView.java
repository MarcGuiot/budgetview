package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
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
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.ChangeSetMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;

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

    builder.addRepeat("budgetAreaRepeat",
                      getBudgetAreas(),
                      new RepeatComponentFactory<BudgetArea>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {
                          JLabel label = new JLabel(descriptionService.getStringifier(BudgetArea.TYPE)
                            .toString(budgetArea.getGlob(), repository));
                          cellBuilder.add("budgetAreaName", label);
                          label.setName(budgetArea.getGlob().get(BudgetArea.NAME));
                          cellBuilder.add("budgetAreaAmount",
                                          GlobLabelView.init(SeriesStat.TYPE, repository, directory,
                                                             new TotalGlobListStringifier(budgetArea.getId(),
                                                                                          SeriesStat.AMOUNT,
                                                                                          budgetArea.isIncome())).getComponent());
                          cellBuilder.add("budgetAreaPlannedAmount",
                                          GlobLabelView.init(SeriesStat.TYPE, repository, directory,
                                                             new TotalGlobListStringifier(budgetArea.getId(),
                                                                                          SeriesStat.PLANNED_AMOUNT,
                                                                                          budgetArea.isIncome())).getComponent());
                        }
                      });

    builder.addLabel("uncategorizedAmountLabel", Month.TYPE, new UncategorizedStringifier())
      .setAutoHideIfEmpty(true)
      .setUpdateMatcher(ChangeSetMatchers.changesForType(Transaction.TYPE));

    builder.add("categorize", new CategorizationAction());

    parentBuilder.add("monthSummaryView", builder);
  }

  private List<BudgetArea> getBudgetAreas() {
    List<BudgetArea> result = new ArrayList<BudgetArea>(Arrays.asList(BudgetArea.values()));
    result.remove(BudgetArea.UNCATEGORIZED);
    return result;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    GlobList selectedMonthStats = new GlobList();
    GlobList selectedSeriesStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(
        repository.getAll(MonthStat.TYPE,
                          and(fieldEquals(MonthStat.MONTH, monthId),
                              fieldEquals(MonthStat.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                              fieldEquals(MonthStat.CATEGORY, Category.ALL))));
      selectedSeriesStats.addAll(
        repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, monthId)));
    }
    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(selectedMonthStats, MonthStat.TYPE);
    localSelectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
    localSelectionService.select(selectedSeriesStats, SeriesStat.TYPE);
  }

  private static class TotalGlobListStringifier implements GlobListStringifier {
    private Integer budgetArea;
    private DoubleField amountField;
    private int multiplier;

    public TotalGlobListStringifier(Integer budgetArea, DoubleField amountField, boolean isIncome) {
      this.budgetArea = budgetArea;
      this.amountField = amountField;
      this.multiplier = isIncome ? 1 : -1;
    }

    public String toString(GlobList selected, GlobRepository repository) {
      Double value = 0.;
      Map<Integer, Integer> cache = new HashMap<Integer, Integer>();
      for (Glob seriesStat : selected) {
        Integer budgetArea = cache.get(seriesStat.get(SeriesStat.SERIES));
        if (budgetArea == null) {
          Glob series = repository.get(Key.create(Series.TYPE, seriesStat.get(SeriesStat.SERIES)));
          budgetArea = series.get(Series.BUDGET_AREA);
          cache.put(seriesStat.get(SeriesStat.SERIES), budgetArea);
        }
        if (budgetArea.equals(this.budgetArea)) {
          value += multiplier * seriesStat.get(amountField);
        }
      }
      return PicsouDescriptionService.DECIMAL_FORMAT.format(value);
    }
  }

  private class UncategorizedStringifier implements GlobListStringifier {
    public String toString(GlobList monthList, GlobRepository repository) {
      double spent = 0;
      double received = 0;
      for (Glob month : monthList) {
        for (Glob transaction : getUncategorizedTransactions(month, repository)) {
          double amount = transaction.get(Transaction.AMOUNT);
          if (amount < 0) {
            spent -= amount;
          }
          else {
            received += amount;
          }
        }
      }

      DecimalFormat format = PicsouDescriptionService.DECIMAL_FORMAT;
      StringBuilder builder = new StringBuilder();
      if (received > 0) {
        builder.append(format.format(received));
      }
      if ((received > 0) && (spent > 0)) {
        builder.append(" / ");
      }
      if (spent > 0) {
        builder.append("-").append(format.format(spent));
      }
      return builder.toString();
    }
  }

  private class CategorizationAction extends AbstractAction implements GlobSelectionListener {
    private GlobList selectedMonthList;

    private CategorizationAction() {
      super(Lang.get("budgetArea.uncategorized"));
      directory.get(SelectionService.class).addListener(this, Month.TYPE);
    }

    public void selectionUpdated(GlobSelection selection) {
      this.selectedMonthList = selection.getAll(Month.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      GlobList transactions = new GlobList();
      for (Glob month : selectedMonthList) {
        transactions.addAll(getUncategorizedTransactions(month, repository));
      }

      CategorizationDialog dialog = new CategorizationDialog(directory.get(JFrame.class), repository, directory);
      dialog.show(transactions, false, true);
    }
  }

  private GlobList getUncategorizedTransactions(Glob month, GlobRepository repository) {
    GlobMatcher matcher = and(
      fieldEquals(Transaction.SERIES, Series.UNKNOWN_SERIES_ID),
      not(fieldEquals(Transaction.PLANNED, true))
    );
    return repository.findByIndex(Transaction.MONTH_INDEX, month.get(Month.ID))
      .filterSelf(matcher, repository);
  }
}
