package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.budget.BudgetAreaSummaryComputer;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
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
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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

  public MonthSummaryView(ImportFileAction importFileAction, GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    this.importFileAction = importFileAction;
    this.parentDirectory = parentDirectory;
    SelectionService parentSelectionService = parentDirectory.get(SelectionService.class);
    parentSelectionService.addListener(this, Month.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/monthSummaryView.splits", repository, directory);

    final GlobListStringifier globListStringifier = GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT,
                                                                             BalanceStat.MONTH_BALANCE);

    builder.addLabel("balanceAmount", BalanceStat.TYPE,
                     new GlobListStringifier() {
                       public String toString(GlobList list, GlobRepository repository) {
                         String balance = globListStringifier.toString(list, repository);
                         if (balance.startsWith("-") || "0.00".equals(balance)) {
                           return balance;
                         }
                         return "+" + balance;
                       }
                     });
    builder.add("totalBalance", new BalanceGraph(repository, directory));

    cards = builder.addCardHandler("cards");

    builder.add("import", importFileAction);

    builder.addRepeat("budgetAreaRepeat",
                      getBudgetAreas(),
                      new RepeatComponentFactory<BudgetArea>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final BudgetArea budgetArea) {

                          String label = budgetAreaStringifier.toString(budgetArea.getGlob(), repository);
                          JButton nameButton = new JButton(new AbstractAction(label) {
                            public void actionPerformed(ActionEvent e) {
                              directory.get(NavigationService.class).gotoBudget();
                            }
                          });
                          cellBuilder.add("budgetAreaName", nameButton);
                          nameButton.setName(budgetArea.getGlob().get(BudgetArea.NAME));

                          JButton amountButton =
                            cellBuilder.add("budgetAreaAmount", new JButton(new AbstractAction() {
                              public void actionPerformed(ActionEvent e) {
                                directory.get(NavigationService.class).gotoData(budgetArea);
                              }
                            }));
                          amountButton.setName(budgetArea.getName() + ":budgetAreaAmount");

                          JLabel plannedLabel = cellBuilder.add("budgetAreaPlannedAmount", new JLabel());
                          plannedLabel.setName(budgetArea.getName() + ":budgetAreaPlannedAmount");

                          Gauge gauge = cellBuilder.add("budgetAreaGauge",
                                                        BudgetAreaGaugeFactory.createGauge(budgetArea));
                          gauge.setName(budgetArea.getName() + ":budgetAreaGauge");

                          new BudgetAreaUpdater(budgetArea, amountButton, plannedLabel, gauge);
                        }
                      });

    builder.addLabel("uncategorizedAmountLabel", Month.TYPE, new UncategorizedStringifier())
      .setAutoHideIfEmpty(true)
      .setUpdateMatcher(ChangeSetMatchers.changesForType(Transaction.TYPE));

    builder.add("categorize", new CategorizationAction(Lang.get("budgetArea.uncategorized")));

    builder.add("openSeriesWizard", new OpenSeriesWizardAction());

    builder.add("hyperlinkHandler", new HyperlinkHandler(parentDirectory));

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
    }
    else if (!repository.contains(Series.TYPE, USER_SERIES_MATCHER)) {
      cards.show("noSeries");
    }
    else {
      cards.show("standard");
    }
  }

  private List<BudgetArea> getBudgetAreas() {
    return Arrays.asList(BudgetArea.INCOME,
                         BudgetArea.SAVINGS,
                         BudgetArea.RECURRING,
                         BudgetArea.ENVELOPES,
                         BudgetArea.OCCASIONAL,
                         BudgetArea.SPECIAL);
  }

  private class BudgetAreaUpdater implements ChangeSetListener, GlobSelectionListener {
    private SortedSet<Integer> selectedMonths = new TreeSet<Integer>();
    private BudgetAreaSummaryComputer summaryComputer;

    public BudgetAreaUpdater(BudgetArea budgetArea, JButton amountLabel, JLabel plannedLabel, Gauge gauge) {
      directory.get(SelectionService.class).addListener(this, Month.TYPE);
      repository.addChangeListener(this);
      this.summaryComputer =
        new BudgetAreaSummaryComputer(budgetArea,
                                      TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                      repository, directory);
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
      summaryComputer.update(balanceStats);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    selectStatAndMonth(months);
  }

  private void selectStatAndMonth(GlobList months) {
    GlobList selectedMonthStats = new GlobList();
    GlobList selectedSeriesStats = new GlobList();
    for (Glob month : months) {
      Integer monthId = month.get(Month.ID);
      selectedMonthStats.addAll(repository.getAll(BalanceStat.TYPE, fieldEquals(BalanceStat.MONTH, monthId)));
      selectedSeriesStats.addAll(repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, monthId)));
    }

    SelectionService localSelectionService = directory.get(SelectionService.class);
    localSelectionService.select(
      GlobSelectionBuilder.init()
        .add(selectedMonthStats, BalanceStat.TYPE)
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

}
