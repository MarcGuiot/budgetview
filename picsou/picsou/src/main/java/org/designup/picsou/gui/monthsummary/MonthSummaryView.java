package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
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
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    parentBuilder.add("monthSummaryView", builder);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    GlobList selectedMonthStats = new GlobList();
    GlobList selectedSeriesStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(
        repository.getAll(MonthStat.TYPE,
                          GlobMatchers.and(GlobMatchers.fieldEquals(MonthStat.MONTH, monthId),
                                           GlobMatchers.fieldEquals(MonthStat.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                                           GlobMatchers.fieldEquals(MonthStat.CATEGORY, Category.ALL))));
      selectedSeriesStats.addAll(
        repository.getAll(SeriesStat.TYPE, GlobMatchers.fieldEquals(SeriesStat.MONTH, monthId)));
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
}
