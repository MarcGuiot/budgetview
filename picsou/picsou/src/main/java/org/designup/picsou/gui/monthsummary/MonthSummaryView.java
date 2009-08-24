package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.budget.BudgetAreaHeaderUpdater;
import org.designup.picsou.gui.budget.BudgetAreaSummaryComputer;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.components.charts.BalanceGraph;
import org.designup.picsou.gui.components.charts.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
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
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @deprecated A SUPPRIMER APRES LA MIGRATION DE LA VUE D'ACCUEIL
 */
public class MonthSummaryView extends View implements GlobSelectionListener {
  private CardHandler cards;
  private GlobStringifier budgetAreaStringifier;
  private Directory parentDirectory;

  public MonthSummaryView(ImportFileAction importFileAction, GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
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
        remainingField = BalanceStat.SAVINGS_IN_REMAINING;
        observedField = BalanceStat.SAVINGS_IN;
        plannedField = BalanceStat.SAVINGS_IN_PLANNED;
      }
      else {
        remainingField = BalanceStat.SAVINGS_OUT_REMAINING;
        observedField = BalanceStat.SAVINGS_OUT;
        plannedField = BalanceStat.SAVINGS_OUT_PLANNED;
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
      return -Math.abs(stat.get(observedField));
    }

    public Double getPlanned(Glob stat, BudgetArea budgetArea) {
      return -Math.abs(stat.get(plannedField));
    }

    public Double getRemaining(Glob stat, BudgetArea budgetArea) {
      return -Math.abs(stat.get(remainingField));
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

    parentBuilder.add("monthSummaryView", builder);

  }

  private List<BudgetElement> getBudgetAreas() {
    return Arrays.asList(new DefaultBudgetArea(BudgetArea.INCOME),
                         new SavingsBudgetArea(true),
                         new SavingsBudgetArea(false),
                         new DefaultBudgetArea(BudgetArea.RECURRING),
                         new DefaultBudgetArea(BudgetArea.ENVELOPES),
                         new DefaultBudgetArea(BudgetArea.SPECIAL));
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
        for (Glob transaction : Transaction.getUncategorizedTransactions(month, repository)) {
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
}
