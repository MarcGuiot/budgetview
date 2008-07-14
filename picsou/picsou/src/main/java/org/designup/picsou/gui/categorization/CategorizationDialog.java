package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.categorization.components.BudgetAreaComponentFactory;
import org.designup.picsou.gui.categorization.components.EnvelopeSeriesComponentFactory;
import org.designup.picsou.gui.categorization.components.OccasionalCategoriesComponentFactory;
import org.designup.picsou.gui.categorization.components.SeriesComponentFactory;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.series.SeriesCreationDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CategorizationDialog {
  private SelectionService selectionService = new SelectionService();
  private LocalGlobRepository localRepository;
  private GlobList currentTransactions = GlobList.EMPTY;
  private PicsouDialog dialog;
  private JToggleButton invisibleBudgetAreaToggle;
  private GlobTableView transactionTable;
  private NextTransactionAction nextTransactionAction;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {

    final Directory localDirectory = init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    GlobFieldComparator transactionComparator = new GlobFieldComparator(Transaction.LABEL);
    LabelCustomizer transactionHighlighter = new LabelCustomizer() {
      public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
        if ((transaction.get(Transaction.SERIES) == null) || (transaction.get(Transaction.CATEGORY) == null)) {
          label.setForeground(Color.RED);
        }
        else {
          label.setForeground(Color.BLACK);
        }
      }
    };
    transactionTable =
      builder.addTable("transactionTable", Transaction.TYPE, transactionComparator)
        .addColumn(Lang.get("date"), new TransactionDateStringifier(transactionComparator), transactionHighlighter)
        .addColumn(Transaction.LABEL, transactionHighlighter)
        .addColumn(Transaction.AMOUNT, transactionHighlighter);

    builder.addMultiLineTextView("transactionLabel", Transaction.TYPE);

    nextTransactionAction = new NextTransactionAction(selectionService);
    builder.add("nextTransaction", nextTransactionAction);

    final CardHandler cardHandler = builder.addCardHandler("cards");

    invisibleBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show("noBudgetArea");
      }
    });
    builder.add("invisibleBudgetAreaToggle", invisibleBudgetAreaToggle);
    builder.addRepeat("budgetAreas", BudgetArea.TYPE.getConstants(),
                      new BudgetAreaComponentFactory(cardHandler, invisibleBudgetAreaToggle,
                                                     localRepository, localDirectory));

    JToggleButton invisibleIncomeToggle = new JToggleButton();
    builder.add("invisibleIncomeToggle", invisibleIncomeToggle);
    builder.addRepeat("incomeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.INCOME.getGlob(), Series.BUDGET_AREA),
                      new SeriesComponentFactory(invisibleIncomeToggle, localRepository, localDirectory));
    builder.add("createIncomeSeries", new SeriesCreationAction(BudgetArea.INCOME, localDirectory));

    JToggleButton invisibleRecurringToggle = new JToggleButton();
    builder.add("invisibleRecurringToggle", invisibleRecurringToggle);
    builder.addRepeat("recurringSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.RECURRING_EXPENSES.getGlob(), Series.BUDGET_AREA),
                      new SeriesComponentFactory(invisibleRecurringToggle, localRepository, localDirectory));
    builder.add("createRecurringSeries", new SeriesCreationAction(BudgetArea.RECURRING_EXPENSES, localDirectory));

    final JToggleButton invisibleEnvelopeToggle = new JToggleButton();
    builder.add("invisibleEnvelopeToggle", invisibleEnvelopeToggle);
    builder.addRepeat("envelopeSeriesRepeat",
                      Series.TYPE,
                      GlobMatchers.linkedTo(BudgetArea.EXPENSES_ENVELOPE.getGlob(), Series.BUDGET_AREA),
                      new EnvelopeSeriesComponentFactory(invisibleEnvelopeToggle, localRepository, localDirectory));

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
                                                               localRepository, localDirectory));

    builder.add("ok", new AbstractAction("ok") {
      public void actionPerformed(ActionEvent e) {
        localRepository.commitChanges(false);
        dialog.setVisible(false);
      }
    });

    builder.add("cancel", new AbstractAction("cancel") {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });

    Container panel = builder.load();
    dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
    dialog.pack();
  }

  private Directory init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE)
      .get();

    Directory localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
        nextTransactionAction.update();
      }
    }, Transaction.TYPE);

    return localDirectory;
  }

  public void show(GlobList transactions, boolean selectAll) {
    if (transactions.isEmpty()) {
      return;
    }
    localRepository.rollback();
    localRepository.reset(transactions, Transaction.TYPE);
    if (selectAll) {
      transactionTable.select(localRepository.getAll(Transaction.TYPE), true);
    }
    else {
      transactionTable.selectFirst();
    }
    GuiUtils.showCentered(dialog);
  }

  private class NextTransactionAction extends AbstractAction {
    private SelectionService selectionService;

    private NextTransactionAction(SelectionService selectionService) {
      super("nextTransaction");
      this.selectionService = selectionService;
    }

    public void actionPerformed(ActionEvent e) {
      if (currentTransactions.isEmpty()) {
        return;
      }
      final int currentIndex = transactionTable.indexOf(currentTransactions.get(0));
      if (currentIndex < transactionTable.getRowCount() - 1) {
        Glob nextTransaction = transactionTable.getGlobAt(currentIndex + 1);
        selectionService.select(nextTransaction);
      }
    }

    public void update() {
      if (currentTransactions.isEmpty()) {
        setEnabled(false);
        return;
      }
      final int currentIndex = transactionTable.indexOf(currentTransactions.get(0));
      setEnabled(currentIndex < transactionTable.getRowCount() - 1);
    }
  }

  private class SeriesCreationAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private final Directory localDirectory;

    public SeriesCreationAction(BudgetArea budgetArea, Directory localDirectory) {
      this.budgetArea = budgetArea;
      this.localDirectory = localDirectory;
    }

    public void actionPerformed(ActionEvent e) {
      SeriesCreationDialog creationDialog = new SeriesCreationDialog(budgetArea, dialog, localRepository, localDirectory);
      creationDialog.show();
    }
  }
}
