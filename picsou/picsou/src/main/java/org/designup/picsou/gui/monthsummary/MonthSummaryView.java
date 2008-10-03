package org.designup.picsou.gui.monthsummary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.wizard.SeriesWizardDialog;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.BudgetAreaGaugeFactory;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.gui.model.MonthStat;
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
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
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
                         BudgetArea.RECURRING.getId(),
                         BudgetArea.ENVELOPES.getId(),
                         BudgetArea.SPECIAL.getId(),
                         BudgetArea.SAVINGS.getId());
  private CardHandler cards;
  private SelectionService parentSelectionService;
  private GlobStringifier budgetAreaStringifier;
  private ImportFileAction importFileAction;

  public MonthSummaryView(ImportFileAction importFileAction, GlobRepository repository, Directory parentDirectory) {
    super(repository, createDirectory(parentDirectory));
    this.importFileAction = importFileAction;
    parentSelectionService = parentDirectory.get(SelectionService.class);
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

    builder.addLabel("totalReceivedAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_RECEIVED));
    builder.addLabel("totalSpentAmount", MonthStat.TYPE,
                     GlobListStringifiers.sum(PicsouDescriptionService.DECIMAL_FORMAT, MonthStat.TOTAL_SPENT));
    builder.add("totalBalance",
                new BalanceGraph(MonthStat.TYPE, MonthStat.TOTAL_RECEIVED, MonthStat.TOTAL_SPENT, directory));

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

    builder.add("categorize", new CategorizationAction(Lang.get("budgetArea.uncategorized"), false));

    builder.add("openSeriesWizard", new OpenSeriesWizardAction());

    builder.add("categorizeAll", new CategorizationAction(null, true));

    builder.add("help", new HelpAction(Lang.get("monthsummary.help"), "import", directory));

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
    result.remove(BudgetArea.ALL);
    result.remove(BudgetArea.UNCATEGORIZED);
    return result;
  }

  private class BudgetAreaUpdater implements ChangeSetListener, GlobSelectionListener {
    private BudgetArea budgetArea;
    private JButton amountLabel;
    private JLabel plannedLabel;
    private Gauge gauge;
    private int multiplier;
    private SortedSet<Integer> selectedMonths = new TreeSet<Integer>();

    public BudgetAreaUpdater(BudgetArea budgetArea, JButton amountLabel, JLabel plannedLabel, Gauge gauge) {
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
    localSelectionService.select(
      GlobSelectionBuilder.init()
        .add(selectedMonthStats, MonthStat.TYPE)
        .add(selection.getAll(Month.TYPE), Month.TYPE)
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
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)
      .findByIndex(Transaction.MONTH, month.get(Month.ID)).getGlobs()
      .filterSelf(fieldEquals(Transaction.PLANNED, false), repository);
  }

  private class OpenSeriesWizardAction extends AbstractAction {
    public OpenSeriesWizardAction() {
      super(Lang.get("monthsummary.openSeriesWizard"));
    }

    public void actionPerformed(ActionEvent e) {
        SeriesWizardDialog dialog = new SeriesWizardDialog(repository, directory);
        dialog.show();
      }
  }
}
