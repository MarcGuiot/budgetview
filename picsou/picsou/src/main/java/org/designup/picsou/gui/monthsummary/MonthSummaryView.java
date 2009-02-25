package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.budget.BudgetAreaHeaderUpdater;
import org.designup.picsou.gui.budget.BudgetAreaSummaryComputer;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;

public class MonthSummaryView extends View implements GlobSelectionListener {
  private static final GlobMatcher USER_SERIES_MATCHER =
    fieldIn(Series.BUDGET_AREA,
            BudgetArea.INCOME.getId(),
            BudgetArea.RECURRING.getId(),
            BudgetArea.ENVELOPES.getId(),
            BudgetArea.SPECIAL.getId(),
            BudgetArea.SAVINGS.getId());
  private CardHandler cards;
  private GlobStringifier budgetAreaStringifier;
  private ImportFileAction importFileAction;
  private Directory parentDirectory;
  private GlobStringifier accountStringifier;
  private JPanel savingsPanel;

  public MonthSummaryView(ImportFileAction importFileAction, GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    this.importFileAction = importFileAction;
    this.parentDirectory = parentDirectory;
    SelectionService parentSelectionService = parentDirectory.get(SelectionService.class);
    parentSelectionService.addListener(this, Month.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);
  }

  private interface BudgetElement {

    String getLabel();

    String getName();

    BudgetArea getBudgetArea();

    Gauge createGauge();

    Double getObserved(Glob stat, BudgetArea budgetArea);

    Double getPlanned(Glob stat, BudgetArea budgetArea);

    Double getRemaining(Glob stat, BudgetArea budgetArea);
  }

  private class DefaultBudgetArea implements BudgetElement {
    private BudgetArea budgetArea;

    private DefaultBudgetArea(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public String getLabel() {
      return budgetAreaStringifier.toString(budgetArea.getGlob(), repository);
    }

    public String getName() {
      return budgetArea.getGlob().get(BudgetArea.NAME);
    }

    public BudgetArea getBudgetArea() {
      return budgetArea;
    }

    public Gauge createGauge() {
      return BudgetAreaGaugeFactory.createGauge(budgetArea);
    }

    public Double getObserved(Glob stat, BudgetArea budgetArea) {
      return stat.get(BalanceStat.getObserved(budgetArea));
    }

    public Double getPlanned(Glob stat, BudgetArea budgetArea) {
      return stat.get(BalanceStat.getPlanned(budgetArea));
    }

    public Double getRemaining(Glob stat, BudgetArea budgetArea) {
      return stat.get(BalanceStat.getRemaining(budgetArea));
    }
  }

  private class SavingsBudgetArea implements BudgetElement {
    private BudgetArea budgetArea = BudgetArea.SAVINGS;
    private String label;
    private boolean in;
    private DoubleField observedField;
    private DoubleField plannedField;
    private DoubleField remainingField;

    private SavingsBudgetArea(boolean in) {
      this.in = in;
      label = Lang.get("monthsummary.savings." + (in ? "in" : "out"));
      if (in) {
        remainingField = BalanceStat.SAVINGS_REMAINING_IN;
        observedField = BalanceStat.SAVINGS_IN;
        plannedField = BalanceStat.SAVINGS_PLANNED_IN;
      }
      else {
        remainingField = BalanceStat.SAVINGS_REMAINING_OUT;
        observedField = BalanceStat.SAVINGS_OUT;
        plannedField = BalanceStat.SAVINGS_PLANNED_OUT;
      }
    }

    public String getLabel() {
      return label;
    }

    public String getName() {
      return budgetArea.getGlob().get(BudgetArea.NAME) + (in ? ":in" : ":out");
    }

    public BudgetArea getBudgetArea() {
      return budgetArea;
    }

    public Gauge createGauge() {
      return new Gauge(!in, in);
    }

    public Double getObserved(Glob stat, BudgetArea budgetArea) {
      return Math.abs(stat.get(observedField));
    }

    public Double getPlanned(Glob stat, BudgetArea budgetArea) {
      return Math.abs(stat.get(plannedField));
    }

    public Double getRemaining(Glob stat, BudgetArea budgetArea) {
      return Math.abs(stat.get(remainingField));
    }
  }


  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/monthSummaryView.splits", repository, directory);

    final GlobListStringifier balanceStatStringifier = GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT,
                                                                                BalanceStat.MONTH_BALANCE);

    builder.addLabel("mainAccountsBalanceAmount", BalanceStat.TYPE,
                     new GlobListStringifier() {
                       public String toString(GlobList list, GlobRepository repository) {
                         String balance = balanceStatStringifier.toString(list, repository);
                         if (balance.startsWith("-") || "0.00".equals(balance)) {
                           return balance;
                         }
                         return "+" + balance;
                       }
                     });
    builder.add("mainAccountsTotalBalance",
                new BalanceGraph(repository, directory, BalanceStat.TYPE, BalanceStat.MONTH,
                                 BalanceStat.INCOME, BalanceStat.INCOME_REMAINING,
                                 BalanceStat.EXPENSE, BalanceStat.EXPENSE_REMAINING));

    final GlobListStringifier savingsStatStringifier = GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT,
                                                                                SavingsBalanceStat.BALANCE);

    builder.addLabel("savingsBalanceAmount", SavingsBalanceStat.TYPE,
                     new GlobListStringifier() {
                       public String toString(GlobList list, GlobRepository repository) {
                         String balance = savingsStatStringifier.toString(list, repository);
                         if (balance.startsWith("-") || "0.00".equals(balance) || Strings.isNullOrEmpty(balance)) {
                           return balance;
                         }
                         return "+" + balance;
                       }
                     });
    builder.add("savingsTotalBalance",
                new BalanceGraph(repository, directory, SavingsBalanceStat.TYPE, SavingsBalanceStat.MONTH,
                                 SavingsBalanceStat.SAVINGS, SavingsBalanceStat.SAVINGS_REMAINING,
                                 SavingsBalanceStat.OUT, SavingsBalanceStat.OUT_REMAINING));

    cards = builder.addCardHandler("cards");

    builder.add("import", importFileAction);

    builder.addRepeat("budgetAreaRepeat",
                      getBudgetAreas(),
                      new RepeatComponentFactory<BudgetElement>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final BudgetElement budgetElement) {

                          String label = budgetElement.getLabel();
                          JButton nameButton = new JButton(new AbstractAction(label) {
                            public void actionPerformed(ActionEvent e) {
                              directory.get(NavigationService.class).gotoBudget();
                            }
                          });
                          cellBuilder.add("budgetAreaName", nameButton);
                          String name = budgetElement.getName();
                          nameButton.setName(name);

                          JButton amountButton =
                            cellBuilder.add("budgetAreaAmount", new JButton(new AbstractAction() {
                              public void actionPerformed(ActionEvent e) {
                                directory.get(NavigationService.class).gotoData(budgetElement.getBudgetArea());
                              }
                            }));
                          amountButton.setName(name + ":budgetAreaAmount");

                          JLabel plannedLabel = cellBuilder.add("budgetAreaPlannedAmount", new JLabel());
                          plannedLabel.setName(name + ":budgetAreaPlannedAmount");

                          Gauge gauge = cellBuilder.add("budgetAreaGauge",
                                                        budgetElement.createGauge());

                          gauge.setName(name + ":budgetAreaGauge");

                          new BudgetAreaUpdater(budgetElement, amountButton, plannedLabel, gauge);
                        }
                      });

    builder.addLabel("uncategorizedAmountLabel", Month.TYPE, new UncategorizedStringifier())
      .setAutoHideIfEmpty(true)
      .setUpdateMatcher(ChangeSetMatchers.changesForType(Transaction.TYPE));

    builder.add("categorize", new CategorizationAction(Lang.get("budgetArea.uncategorized")));

    builder.add("openSeriesWizard", new OpenSeriesWizardAction());

    builder.add("hyperlinkHandler", new HyperlinkHandler(parentDirectory));

    accountStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);

    savingsPanel = new JPanel();
    builder.add("savingsPanel", savingsPanel);

    builder.addRepeat("savingsAccountRepeat", Account.TYPE,
                      and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
                          not(fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID))),
                      new SavingsRepeatComponentFactory());

    parentBuilder.add("monthSummaryView", builder);

    registerCardUpdater();
  }

  private void registerComponent(RepeatCellBuilder cellBuilder, final Glob account, Gauge inGauge, final boolean in,
                                 final InOrOutLine inOrOutLine) {
    String accountName = accountStringifier.toString(account, repository);
    String sens = in ? "In" : "Out";
    JButton amountButton =
      cellBuilder.add("savings" + sens + "Amount", new JButton(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          directory.get(NavigationService.class).gotoDataForSavingsAccount(account.get(Account.ID));
        }
      }));
    amountButton.setName(accountName + ":savings" + sens + "Amount");

    JLabel plannedLabel = cellBuilder.add("savingsPlanned" + sens + "Amount", new JLabel());
    plannedLabel.setName(accountName + ":savingsPlanned" + sens + "Amount");

    Gauge gauge = cellBuilder.add("savings" + sens + "Gauge", inGauge);

    gauge.setName(accountName + ":" + sens + "Gauge");

    new SavingsAccountsUpdater(inOrOutLine, amountButton, plannedLabel, inGauge, in, account.get(Account.ID));
  }

  private void registerCardUpdater() {
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
            changeSet.containsCreationsOrDeletions(Series.TYPE)) {
          updateCard();
        }
        if (changeSet.containsChanges(BalanceStat.TYPE)) {
          selectStatAndMonth(selectionService.getSelection(Month.TYPE));
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
      savingsPanel.setVisible(false);
    }
    else if (!repository.contains(Series.TYPE, USER_SERIES_MATCHER)) {
      cards.show("noSeries");
      savingsPanel.setVisible(false);
    }
    else {
      cards.show("standard");
      savingsPanel.setVisible(true);
    }
  }

  private List<BudgetElement> getBudgetAreas() {
    return Arrays.asList(new DefaultBudgetArea(BudgetArea.INCOME),
                         new SavingsBudgetArea(true),
                         new SavingsBudgetArea(false),
                         new DefaultBudgetArea(BudgetArea.RECURRING),
                         new DefaultBudgetArea(BudgetArea.ENVELOPES),
                         new DefaultBudgetArea(BudgetArea.OCCASIONAL),
                         new DefaultBudgetArea(BudgetArea.SPECIAL));
  }

  private class SavingsAccountsUpdater implements ChangeSetListener, GlobSelectionListener {
    private SortedSet<Integer> selectedMonths = new TreeSet<Integer>();
    private BudgetAreaSummaryComputer summaryComputer;
    private Gauge gauge;
    private Integer accountId;
    DoubleField savings;
    DoubleField planned;
    DoubleField remaining;
    private InOrOutLine inOrOutLine;

    public SavingsAccountsUpdater(InOrOutLine inOrOutLine,
                                  JButton amountLabel, JLabel plannedLabel, Gauge gauge,
                                  final boolean in, Integer accountId) {
      this.inOrOutLine = inOrOutLine;
      if (in) {
        savings = SavingsBalanceStat.SAVINGS;
        planned = SavingsBalanceStat.SAVINGS_PLANNED;
        remaining = SavingsBalanceStat.SAVINGS_REMAINING;
      }
      else {
        savings = SavingsBalanceStat.OUT;
        planned = SavingsBalanceStat.OUT_PLANNED;
        remaining = SavingsBalanceStat.OUT_REMAINING;
      }
      this.gauge = gauge;
      this.accountId = accountId;
      directory.get(SelectionService.class).addListener(this, Month.TYPE);
      repository.addChangeListener(this);
      this.summaryComputer =
        new BudgetAreaHeaderUpdater(TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel),
                                    gauge, repository, directory) {
          protected Double getObserved(Glob stat, BudgetArea budgetArea) {
            return stat.get(savings);
          }

          protected Double getPlanned(Glob stat, BudgetArea budgetArea) {
            return stat.get(planned);
          }

          protected Double getRemaining(Glob stat, BudgetArea budgetArea) {
            return stat.get(remaining);
          }

          protected void clearComponents() {
            super.clearComponents();
            gauge.setVisible(false);
          }
        };
      update();
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(SavingsBalanceStat.TYPE)) {
        update();
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(SavingsBalanceStat.TYPE)) {
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
      GlobList balanceStats =
        repository.getAll(SavingsBalanceStat.TYPE,
                          and(fieldIn(SavingsBalanceStat.MONTH, selectedMonths),
                              fieldEquals(SavingsBalanceStat.ACCOUNT, accountId),
                              and(not(and(fieldEquals(planned, 0.),
                                          fieldEquals(remaining, 0.),
                                          fieldEquals(savings, 0.))))));
      if (balanceStats.isEmpty()) {
        inOrOutLine.hidden();
      }
      else {
        inOrOutLine.shown();
      }
      summaryComputer.update(balanceStats, BudgetArea.SAVINGS);
    }

  }

  interface InOrOutLine {
    public abstract void hidden();

    public abstract void shown();
  }

  private class BudgetAreaUpdater implements ChangeSetListener, GlobSelectionListener {
    private SortedSet<Integer> selectedMonths = new TreeSet<Integer>();
    private BudgetAreaSummaryComputer summaryComputer;
    private BudgetElement budgetElement;

    public BudgetAreaUpdater(final BudgetElement budgetElement, JButton amountLabel, JLabel plannedLabel, Gauge gauge) {
      this.budgetElement = budgetElement;
      directory.get(SelectionService.class).addListener(this, Month.TYPE);
      repository.addChangeListener(this);
      this.summaryComputer =
        new BudgetAreaHeaderUpdater(
          TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
          repository, directory) {
          protected Double getObserved(Glob stat, BudgetArea budgetArea) {
            return budgetElement.getObserved(stat, budgetArea);
          }

          protected Double getPlanned(Glob stat, BudgetArea budgetArea) {
            return budgetElement.getPlanned(stat, budgetArea);
          }

          protected Double getRemaining(Glob stat, BudgetArea budgetArea) {
            return budgetElement.getRemaining(stat, budgetArea);
          }
        };
      update();
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(BalanceStat.TYPE)
          || changeSet.containsChanges(SeriesStat.TYPE)) {
        update();
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(BalanceStat.TYPE)
          || changedTypes.contains(SeriesStat.TYPE)) {
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
      GlobList balanceStats = repository.getAll(BalanceStat.TYPE, fieldIn(BalanceStat.MONTH, selectedMonths));
      summaryComputer.update(balanceStats, budgetElement.getBudgetArea());
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    selectStatAndMonth(months);
  }

  private void selectStatAndMonth(GlobList months) {
    GlobList selectedMonthStats = new GlobList();
    GlobList selectedMonthSavingsStats = new GlobList();
    GlobList selectedSeriesStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(repository.getAll(BalanceStat.TYPE, fieldEquals(BalanceStat.MONTH, monthId)));
      selectedMonthSavingsStats.addAll(repository.getAll(SavingsBalanceStat.TYPE,
                                                         and(fieldEquals(SavingsBalanceStat.MONTH, monthId),
                                                             not(fieldEquals(SavingsBalanceStat.ACCOUNT,
                                                                             Account.SAVINGS_SUMMARY_ACCOUNT_ID)))));
      selectedSeriesStats.addAll(repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, monthId)));
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(
      GlobSelectionBuilder.init()
        .add(selectedMonthStats, BalanceStat.TYPE)
        .add(selectedMonthSavingsStats, SavingsBalanceStat.TYPE)
        .add(months, Month.TYPE)
        .add(selectedSeriesStats, SeriesStat.TYPE)
        .get());
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

      DecimalFormat format = Formatting.DECIMAL_FORMAT;
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

    private CategorizationAction(final String title) {
      super(title);
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoCategorization();
    }
  }

  private GlobList getUncategorizedTransactions(Glob month, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)
      .findByIndex(Transaction.MONTH, month.get(Month.ID)).getGlobs()
      .filterSelf(fieldEquals(Transaction.PLANNED, false), repository);
  }

  private class OpenSeriesWizardAction extends AbstractAction {
    public OpenSeriesWizardAction() {
      super(Lang.get("monthsummary.openSeriesWizard"));
    }

    public void actionPerformed(ActionEvent e) {
      SeriesWizardDialog dialog = new SeriesWizardDialog(repository, parentDirectory);
      dialog.show();
    }
  }

  private class SavingsRepeatComponentFactory implements RepeatComponentFactory<Glob> {
    int hiddenCount;

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {
      String accountName = accountStringifier.toString(account, repository);
      final JPanel accountGroup = new JPanel();
      accountGroup.setName("accountGroup:" + accountName);
      cellBuilder.add("accountGroup", accountGroup);
      GlobLabelView accountNameView =
        GlobLabelView.init(Account.TYPE, repository, directory)
          .forceSelection(account);
      cellBuilder.add("accountName", accountNameView.getComponent());

      DefaultInOrOutLine outLine = new DefaultInOrOutLine(accountGroup);
      registerComponent(cellBuilder, account, new Gauge(false, false), true, outLine);
      registerComponent(cellBuilder, account, new Gauge(true, true), false, outLine);
    }

    private class DefaultInOrOutLine implements InOrOutLine {
      boolean hidden;
      private final JPanel accountGroup;

      public DefaultInOrOutLine(JPanel accountGroup) {
        this.accountGroup = accountGroup;
      }

      public void hidden() {
        if (!hidden) {
          hidden = true;
          hiddenCount++;
          accountGroup.setVisible(hiddenCount != 2);
        }
      }

      public void shown() {
        if (hidden) {
          hidden = false;
          hiddenCount--;
          accountGroup.setVisible(hiddenCount != 2);
        }
      }
    }
  }
}
