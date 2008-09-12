package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
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
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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
                          JLabel nameLabel = new JLabel(descriptionService.getStringifier(BudgetArea.TYPE)
                            .toString(budgetArea.getGlob(), repository));
                          cellBuilder.add("budgetAreaName", nameLabel);
                          nameLabel.setName(budgetArea.getGlob().get(BudgetArea.NAME));

                          JLabel amountLabel = cellBuilder.add("budgetAreaAmount", new JLabel());
                          JLabel plannedLabel = cellBuilder.add("budgetAreaPlannedAmount", new JLabel());
                          Gauge gauge = cellBuilder.add("budgetAreaGauge",
                                                        BudgetAreaGaugeFactory.createGauge(budgetArea));
                          new BudgetAreaUpdater(budgetArea, amountLabel, plannedLabel, gauge);
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

  private class BudgetAreaUpdater implements ChangeSetListener, GlobSelectionListener {
    private BudgetArea budgetArea;
    private JLabel amountLabel;
    private JLabel plannedLabel;
    private Gauge gauge;
    private int multiplier;
    private Set<Integer> selectedMonths = Collections.emptySet();

    public BudgetAreaUpdater(BudgetArea budgetArea, JLabel amountLabel, JLabel plannedLabel, Gauge gauge) {
      this.budgetArea = budgetArea;
      this.amountLabel = amountLabel;
      this.plannedLabel = plannedLabel;
      this.gauge = gauge;
      this.multiplier = budgetArea.isIncome() ? 1 : -1;
      directory.get(SelectionService.class).addListener(this, Month.TYPE);
      repository.addChangeListener(this);
      update();
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(SeriesStat.TYPE)) {
        update();
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(SeriesStat.TYPE)) {
        update();
      }
    }

    public void selectionUpdated(GlobSelection selection) {
      if (selection.isRelevantForType(Month.TYPE)) {
        selectedMonths = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        update();
      }
    }

    public void update() {

      Double observed = 0.0;
      Double planned = 0.0;
      Map<Integer, Integer> cache = new HashMap<Integer, Integer>();
      for (Glob seriesStat : repository.getAll(SeriesStat.TYPE, GlobMatchers.fieldIn(SeriesStat.MONTH, selectedMonths))) {
        Integer budgetAreaId = cache.get(seriesStat.get(SeriesStat.SERIES));
        if (budgetAreaId == null) {
          Glob series = repository.get(Key.create(Series.TYPE, seriesStat.get(SeriesStat.SERIES)));
          budgetAreaId = series.get(Series.BUDGET_AREA);
          cache.put(seriesStat.get(SeriesStat.SERIES), budgetAreaId);
        }
        if (budgetArea.getId().equals(budgetAreaId)) {
          observed += multiplier * seriesStat.get(SeriesStat.AMOUNT);
          planned += multiplier * seriesStat.get(SeriesStat.PLANNED_AMOUNT);
        }
      }

      amountLabel.setText(PicsouDescriptionService.DECIMAL_FORMAT.format(observed));
      amountLabel.setVisible(true);
      amountLabel.setBackground(Color.RED);
      plannedLabel.setText(PicsouDescriptionService.DECIMAL_FORMAT.format(planned));
      gauge.setValues(observed, planned);
    }
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

  private class CategorizationAction extends AbstractAction {

    private CategorizationAction() {
      super(Lang.get("budgetArea.uncategorized"));
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoCategorization();
    }
  }

  private GlobList getUncategorizedTransactions(Glob month, GlobRepository repository) {
    GlobMatcher matcher = and(
      fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
      not(fieldEquals(Transaction.PLANNED, true))
    );
    return repository.findByIndex(Transaction.MONTH_INDEX, month.get(Month.ID))
      .filterSelf(matcher, repository);
  }
}
