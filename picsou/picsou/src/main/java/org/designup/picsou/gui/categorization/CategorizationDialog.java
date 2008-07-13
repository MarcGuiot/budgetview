package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.categorization.components.BudgetAreaComponentFactory;
import org.designup.picsou.gui.categorization.components.EnvelopeSeriesComponentFactory;
import org.designup.picsou.gui.categorization.components.OccasionalCategoriesComponentFactory;
import org.designup.picsou.gui.categorization.components.SeriesComponentFactory;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesCreationDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CategorizationDialog {
  private SelectionService selectionService = new SelectionService();
  private LocalGlobRepository localRepository;
  private Glob currentTransaction;
  private PicsouDialog dialog;
  private int transactionIndex = 0;
  private GlobList transactions;
  private JToggleButton invisibleBudgetAreaToggle;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {

    final Directory localDirectory = init(repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    builder.addMultiLineTextView("transactionLabel", Transaction.TYPE);

    builder.add("nextTransaction", new NextTransactionAction(selectionService));
    builder.add("previousTransaction", new PreviousTransactionAction(selectionService));

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

    JPanel panel = builder.load();
    dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
  }

  private Directory init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE)
      .get();

    Directory localDirectory = new DefaultDirectory(directory);
    localDirectory.add(selectionService);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransaction = selection.getAll(Transaction.TYPE).get(0);
        if ((currentTransaction != null) && (currentTransaction.get(Transaction.SERIES) == null)) {
          invisibleBudgetAreaToggle.doClick();
        }
      }
    }, Transaction.TYPE);

    return localDirectory;
  }

  public void show(GlobList transactions) {
    if (transactions.isEmpty()) {
      return;
    }
    localRepository.rollback();
    transactionIndex = 0;
    localRepository.reset(transactions, Transaction.TYPE);
    this.transactions = localRepository.getAll(Transaction.TYPE).sort(TransactionComparator.ASCENDING);
    selectionService.select(this.transactions.get(transactionIndex));
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class NextTransactionAction extends AbstractAction implements GlobSelectionListener {
    private SelectionService selectionService;

    private NextTransactionAction(SelectionService selectionService) {
      super("nextTransaction");
      this.selectionService = selectionService;
      selectionService.addListener(this, Transaction.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      if (transactionIndex < transactions.size() - 1) {
        transactionIndex++;
        selectionService.select(transactions.get(transactionIndex));
      }
      setEnabled(transactionIndex < transactions.size() - 1);
    }

    public void selectionUpdated(GlobSelection selection) {
      setEnabled(transactionIndex < transactions.size() - 1);
    }
  }

  private class PreviousTransactionAction extends AbstractAction implements GlobSelectionListener {
    private SelectionService selectionService;

    private PreviousTransactionAction(SelectionService selectionService) {
      super("previousTransaction");
      this.selectionService = selectionService;
      setEnabled(false);
      this.selectionService.addListener(this, Transaction.TYPE);
    }

    public void actionPerformed(ActionEvent e) {
      if (transactionIndex > 0) {
        transactionIndex--;
        selectionService.select(transactions.get(transactionIndex));
      }
      setEnabled(transactionIndex > 0);
    }

    public void selectionUpdated(GlobSelection selection) {
      setEnabled(transactionIndex >= 1);
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
