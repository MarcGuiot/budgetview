package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobSelectionBuilder;
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
  private static final GlobMatcher USER_SERIES_MATCHER =
    GlobMatchers.fieldIn(Series.BUDGET_AREA,
                         BudgetArea.INCOME.getId(),
                         BudgetArea.RECURRING_EXPENSES.getId(),
                         BudgetArea.EXPENSES_ENVELOPE.getId(),
                         BudgetArea.PROJECTS.getId(),
                         BudgetArea.SAVINGS.getId());
  private CardHandler cards;
  private SelectionService parentSelectionService;
  private JLabel balance;
  private JLabel income;
  private JLabel fixe;
  private JLabel total;
  private JLabel saving;

  public MonthSummaryView(GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    parentSelectionService = parentDirectory.get(SelectionService.class);
    parentSelectionService.addListener(this, Month.TYPE);
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SeriesStat.TYPE) || changeSet.containsUpdates(Transaction.BALANCE)) {
          updateDetails();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
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

    cards = builder.addCardHandler("cards");

    builder.add("import", ImportFileAction.init(Lang.get("import"), repository, directory, null));

    builder.addRepeat("budgetAreaRepeat",
                      getBudgetAreas(),
                      new RepeatComponentFactory<BudgetArea>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {
                          JLabel nameLabel = new JLabel(descriptionService.getStringifier(BudgetArea.TYPE)
                            .toString(budgetArea.getGlob(), repository));
                          cellBuilder.add("budgetAreaName", nameLabel);
                          nameLabel.setName(budgetArea.getGlob().get(BudgetArea.NAME));

                          JLabel amountLabel =
                            cellBuilder.add("budgetAreaAmount", new JLabel());
                          amountLabel.setName(budgetArea.getName() + ":budgetAreaAmount");

                          JLabel plannedLabel = cellBuilder.add("budgetAreaPlannedAmount", new JLabel());
                          plannedLabel.setName(budgetArea.getName() + ":budgetAreaPlannedAmount");

                          Gauge gauge = cellBuilder.add("budgetAreaGauge",
                                                        BudgetAreaGaugeFactory.createGauge(budgetArea));
                          gauge.setName(budgetArea.getName() + ":budgetAreaGauge");

                          new BudgetAreaUpdater(budgetArea, amountLabel, plannedLabel, gauge);
                        }
                      });

    builder.addLabel("uncategorizedAmountLabel", Month.TYPE, new UncategorizedStringifier())
      .setAutoHideIfEmpty(true)
      .setUpdateMatcher(ChangeSetMatchers.changesForType(Transaction.TYPE));

    builder.add("categorize", new CategorizationAction(Lang.get("budgetArea.uncategorized"), false));
    builder.add("categorizeAll", new CategorizationAction(null, true));

    balance = new JLabel();
    builder.add("detailBalance", balance);
    income = new JLabel();
    builder.add("detailIncome", income);
    fixe = new JLabel();
    builder.add("detailFixe", fixe);
    saving = new JLabel();
    builder.add("detailSaving", saving);
    total = new JLabel();
    builder.add("detailTotal", total);
    parentBuilder.add("monthSummaryView", builder);

    registerCardUpdater();
  }

  private void registerCardUpdater() {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
            changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateCard();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(Series.TYPE)) {
          updateCard();
        }
      }
    });
  }

  public void init() {
    updateCard();
  }

  private void updateCard() {
    if (!repository.contains(Transaction.TYPE)) {
      cards.show("noData");
    }
    else if (!repository.contains(Series.TYPE, USER_SERIES_MATCHER)) {
      cards.show("noSeries");
    }
    else {
      cards.show("standard");
    }
  }

  private List<BudgetArea> getBudgetAreas() {
    List<BudgetArea> result = new ArrayList<BudgetArea>(Arrays.asList(BudgetArea.values()));
    result.remove(BudgetArea.UNCATEGORIZED);
    return result;
  }

  private void updateDetails() {
    SortedSet<Integer> currentMonths = parentSelectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    SortedSet<Glob> tmp =
      repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_BANK,
                           GlobMatchers.fieldIn(Transaction.MONTH, currentMonths));
    Glob[] transactions = tmp.toArray(new Glob[tmp.size()]);
    Double balanceAmount = null;
    int i;
    for (i = transactions.length - 1; i >= 0; i--) {
      Glob transaction = transactions[i];
      if (transaction.get(Transaction.PLANNED)) {
        continue;
      }
      balanceAmount = transaction.get(Transaction.BALANCE);
      if (balanceAmount != null) {
        break;
      }
    }
    if (balanceAmount == null) {
      return;
    }
    String libelle = PicsouDescriptionService.toString(balanceAmount);
    balance.setText(libelle);
    GlobList series = repository.getAll(Series.TYPE);
    Set<Integer> incomeSeries = new HashSet<Integer>();
    Set<Integer> fixeSeries = new HashSet<Integer>();
    Set<Integer> savingSeries = new HashSet<Integer>();

    for (Glob oneSeries : series) {
      if (BudgetArea.INCOME.getId().equals(oneSeries.get(Series.BUDGET_AREA))) {
        incomeSeries.add(oneSeries.get(Series.ID));
      }
      else if (BudgetArea.RECURRING_EXPENSES.getId().equals(oneSeries.get(Series.BUDGET_AREA))) {
        fixeSeries.add(oneSeries.get(Series.ID));
      }
      else if (BudgetArea.SAVINGS.getId().equals(oneSeries.get(Series.BUDGET_AREA))) {
        savingSeries.add(oneSeries.get(Series.ID));
      }
    }
    double incomeAmount = 0;
    double fixeAmount = 0;
    double savingAmount = 0;
    for (; i < transactions.length; i++) {
      Glob transaction = transactions[i];
      if (transaction.get(Transaction.PLANNED)) {
        Integer transactionSeries = transaction.get(Transaction.SERIES);
        if (incomeSeries.contains(transactionSeries)) {
          incomeAmount += transaction.get(Transaction.AMOUNT);
        }
        if (fixeSeries.contains(transactionSeries)) {
          fixeAmount += transaction.get(Transaction.AMOUNT);
        }
        if (savingSeries.contains(transactionSeries)) {
          savingAmount += transaction.get(Transaction.AMOUNT);
        }
      }
    }
    income.setText(PicsouDescriptionService.toString(incomeAmount));
    fixe.setText(PicsouDescriptionService.toString(fixeAmount));
    saving.setText(PicsouDescriptionService.toString(savingAmount));
    total.setText(PicsouDescriptionService.toString(balanceAmount + incomeAmount + fixeAmount + savingAmount));
  }


  private class BudgetAreaUpdater implements ChangeSetListener, GlobSelectionListener {
    private BudgetArea budgetArea;
    private JLabel amountLabel;
    private JLabel plannedLabel;
    private Gauge gauge;
    private int multiplier;
    private SortedSet<Integer> selectedMonths = new TreeSet<Integer>();

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
        selectedMonths = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
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
    localSelectionService.select(GlobSelectionBuilder.init().add(selectedMonthStats, MonthStat.TYPE)
      .add(selection.getAll(Month.TYPE), Month.TYPE).add(selectedSeriesStats, SeriesStat.TYPE).get());
    updateDetails();
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
    private boolean selectAll;

    private CategorizationAction(final String title, boolean all) {
      super(title);
      this.selectAll = all;
    }

    public void actionPerformed(ActionEvent e) {
      if (selectAll) {
        parentSelectionService.select(repository.getAll(Month.TYPE), Month.TYPE);
      }
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
