package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.categorization.components.BudgetAreaComponentFactory;
import org.designup.picsou.gui.categorization.components.EnvelopeSeriesComponentFactory;
import org.designup.picsou.gui.categorization.components.OccasionalCategoriesComponentFactory;
import org.designup.picsou.gui.categorization.components.SeriesComponentFactory;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.Set;

public class CategorizationDialog {
  private SelectionService selectionService = new SelectionService();
  private LocalGlobRepository localRepository;
  private GlobList currentTransactions = GlobList.EMPTY;
  private PicsouDialog dialog;
  private GlobTableView transactionTable;
  private JCheckBox autoSelectionCheckBox;
  private JCheckBox autoHideCheckBox;

  private static final int[] COLUMN_SIZES = {10, 28, 10};
  private Directory localDirectory;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {
    dialog = PicsouDialog.create(parent, Lang.get("categorization.title"));

    init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    Comparator<Glob> transactionComparator = getTransactionComparator();
    transactionTable =
      builder.addTable("transactionTable", Transaction.TYPE, transactionComparator)
        .addColumn(Lang.get("date"), new TransactionDateStringifier(transactionComparator),
                   LabelCustomizers.fontSize(9))
        .addColumn(Transaction.LABEL)
        .addColumn(Transaction.AMOUNT);
    Gui.setColumnSizes(transactionTable.getComponent(), COLUMN_SIZES);

    autoSelectionCheckBox = new JCheckBox(new AutoSelectAction());
    autoSelectionCheckBox.setSelected(true);
    builder.add("autoSelection", autoSelectionCheckBox);

    autoHideCheckBox = new JCheckBox(new AutoHideAction());
    autoHideCheckBox.setSelected(true);
    builder.add("autoHide", autoHideCheckBox);

    builder.addLabel("transactionLabel", Transaction.LABEL);

    final CardHandler cardHandler = builder.addCardHandler("cards");

    JToggleButton invisibleBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show("noBudgetArea");
      }
    });
    builder.add("invisibleBudgetAreaToggle", invisibleBudgetAreaToggle);
    GlobList constants = BudgetArea.TYPE.getConstants();
    constants.removeAll(GlobMatchers.fieldEquals(BudgetArea.ID, BudgetArea.UNCATEGORIZED.getId()), repository);
    builder.addRepeat("budgetAreas", constants,
                      new BudgetAreaComponentFactory(cardHandler, invisibleBudgetAreaToggle,
                                                     localRepository, localDirectory, dialog));

    JToggleButton invisibleIncomeToggle = new JToggleButton();
    builder.add("invisibleIncomeToggle", invisibleIncomeToggle);
    builder.addRepeat("incomeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.INCOME.getGlob(), Series.BUDGET_AREA),
                      new SeriesComponentFactory(invisibleIncomeToggle, localRepository, localDirectory, dialog));
    builder.add("createIncomeSeries", new SeriesCreationAction(BudgetArea.INCOME, localDirectory));

    JToggleButton invisibleRecurringToggle = new JToggleButton();
    builder.add("invisibleRecurringToggle", invisibleRecurringToggle);
    builder.addRepeat("recurringSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.RECURRING_EXPENSES.getGlob(), Series.BUDGET_AREA),
                      new SeriesComponentFactory(invisibleRecurringToggle, localRepository, localDirectory, dialog));
    builder.add("createRecurringSeries", new SeriesCreationAction(BudgetArea.RECURRING_EXPENSES, localDirectory));

    final JToggleButton invisibleEnvelopeToggle = new JToggleButton();
    builder.add("invisibleEnvelopeToggle", invisibleEnvelopeToggle);
    builder.addRepeat("envelopeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.EXPENSES_ENVELOPE.getGlob(), Series.BUDGET_AREA),
                      new EnvelopeSeriesComponentFactory(invisibleEnvelopeToggle, localRepository, localDirectory, dialog));
    builder.add("createEnvelopeSeries", new SeriesCreationAction(BudgetArea.EXPENSES_ENVELOPE, localDirectory));

    JToggleButton invisibleOccasionalToggle = new JToggleButton();
    builder.add("invisibleOccasionalToggle", invisibleOccasionalToggle);
    builder.addRepeat("occasionalSeriesRepeat",
                      Category.TYPE,
                      new GlobMatcher() {
                        public boolean matches(Glob category, GlobRepository repository) {
                          return Category.isMaster(category) && !Category.isAll(category) && !Category.isNone(category);
                        }
                      },
                      new OccasionalCategoriesComponentFactory("occasionalSeries", "occasionalCategoryToggle",
                                                               BudgetArea.OCCASIONAL_EXPENSES,
                                                               invisibleOccasionalToggle,
                                                               localRepository, localDirectory, dialog));

    builder.add("ok", new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    Container panel = builder.load();
    dialog.setContentPane(panel);
    dialog.pack();
  }

  private Comparator<Glob> getTransactionComparator() {
    return new GlobFieldsComparator(Transaction.LABEL, true,
                                    Transaction.MONTH, false,
                                    Transaction.DAY, false,
                                    Transaction.AMOUNT, false);
  }

  private void init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE, Month.TYPE)
      .get();

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
        if (autoSelectionCheckBox.isSelected()) {
          autoSelectSimilarTransactions();
        }
      }
    }, Transaction.TYPE);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        Set<Key> created = changeSet.getCreated(Series.TYPE);
        if (created.size() == 1) {
          setSeries(created.iterator().next());
        }
      }
    });
  }

  private void setSeries(Key seriesKey) {
    Glob series = localRepository.get(seriesKey);
    GlobList seriesToCategories = localRepository.findLinkedTo(series, SeriesToCategory.SERIES);
    if (seriesToCategories.size() > 1) {
      return;
    }

    Key categoryKey;
    if (seriesToCategories.isEmpty()) {
      categoryKey = Key.create(Category.TYPE, series.get(Series.DEFAULT_CATEGORY));
    }
    else {
      categoryKey = Key.create(Category.TYPE, seriesToCategories.iterator().next().get(SeriesToCategory.CATEGORY));
    }

    try {
      localRepository.enterBulkDispatchingMode();
      for (Glob transaction : currentTransactions) {
        localRepository.setTarget(transaction.getKey(), Transaction.SERIES, seriesKey);
        localRepository.setTarget(transaction.getKey(), Transaction.CATEGORY, categoryKey);
      }
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }

  }

  public void show(GlobList transactions, boolean selectAll, boolean autoHideCategorized) {
    if (transactions.isEmpty()) {
      return;
    }
    localRepository.rollback();
    localRepository.reset(transactions, Transaction.TYPE);
    autoHideCheckBox.setSelected(autoHideCategorized);
    updateAutoHide();
    if (selectAll) {
      transactionTable.select(localRepository.getAll(Transaction.TYPE), true);
    }
    else {
      transactionTable.selectFirst();
    }
    GuiUtils.showCentered(dialog);
  }

  private class SeriesCreationAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private final Directory localDirectory;

    public SeriesCreationAction(BudgetArea budgetArea, Directory localDirectory) {
      this.budgetArea = budgetArea;
      this.localDirectory = localDirectory;
    }

    public void actionPerformed(ActionEvent e) {
      SeriesEditionDialog editionDialog = new SeriesEditionDialog(dialog, localRepository, localDirectory);
      editionDialog.show(currentTransactions, budgetArea);
    }
  }

  private class AutoSelectAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      if (autoSelectionCheckBox.isSelected()) {
        autoSelectSimilarTransactions();
      }
    }
  }

  private void autoSelectSimilarTransactions() {
    if (currentTransactions.size() != 1) {
      return;
    }

    final GlobList similarTransactions = getSimilarTransactions(currentTransactions.get(0), localRepository);
    if (similarTransactions.size() > 1) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          selectionService.select(similarTransactions, Transaction.TYPE);
        }
      });
    }
  }

  public static GlobList getSimilarTransactions(Glob transaction, GlobRepository repository) {
    final String referenceLabel = transaction.get(Transaction.LABEL_FOR_CATEGORISATION);
    if (Strings.isNullOrEmpty(referenceLabel)) {
      return new GlobList(transaction);
    }
    return repository.findByIndex(Transaction.LABEL_FOR_CATEGORISATION_INDEX, referenceLabel);
  }

  private void updateAutoHide() {
    if (autoHideCheckBox.isSelected()) {
      transactionTable.setFilter(GlobMatchers.fieldEquals(Transaction.SERIES, Series.UNKNOWN_SERIES_ID));
    }
    else {
      transactionTable.setFilter(GlobMatchers.ALL);
    }
  }

  private class AutoHideAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      updateAutoHide();
    }
  }
}
