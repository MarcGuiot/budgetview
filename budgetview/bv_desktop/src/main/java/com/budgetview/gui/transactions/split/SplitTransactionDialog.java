package com.budgetview.gui.transactions.split;

import com.budgetview.gui.categorization.components.CompactSeriesStringifier;
import com.budgetview.gui.components.table.TableHeaderPainter;
import com.budgetview.gui.components.dialogs.PicsouDialog;
import com.budgetview.gui.components.table.TransactionTableHeaderPainter;
import com.budgetview.gui.components.tips.ErrorTip;
import com.budgetview.gui.description.Formatting;
import com.budgetview.gui.help.HyperlinkHandler;
import com.budgetview.gui.transactions.TransactionView;
import com.budgetview.gui.transactions.columns.AbstractTransactionEditor;
import com.budgetview.gui.transactions.columns.TransactionKeyListener;
import com.budgetview.gui.transactions.columns.TransactionNoteEditor;
import com.budgetview.gui.transactions.columns.TransactionRendererColors;
import com.budgetview.gui.utils.Gui;
import com.budgetview.gui.utils.Icons;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

import static com.budgetview.model.Transaction.*;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import static org.globsframework.model.FieldValue.value;

public class SplitTransactionDialog {
  public static final int CATEGORY_COLUMN_INDEX = 0;
  public static final int LABEL_COLUMN_INDEX = 1;
  public static final int NOTE_COLUMN_INDEX = 3;
  public static final int DELETE_SPLIT_COLUMN_INDEX = 4;

  private Glob sourceTransaction;
  private Glob splittedTransaction;

  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private DescriptionService descriptionService;
  private SelectionService parentSelectionService;

  private TransactionRendererColors rendererColors;
  private JTextField amountField;
  private JTextField noteField;
  private PicsouDialog dialog;
  private SplitTransactionDialog.OkAction okAction;
  private GlobRepository parentRepository;
  private GlobTableView tableView;
  private GlobsPanelBuilder builder;
  private TableHeaderPainter headerPainter;
  private SplitTransactionDialog.AddAction addAction;

  public SplitTransactionDialog(GlobRepository repository, Directory directory) {
    this.parentRepository = repository;

    localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Month.TYPE, Series.TYPE, SubSeries.TYPE)
        .get();

    localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);
    descriptionService = localDirectory.get(DescriptionService.class);
    rendererColors = TransactionView.createRendererColors(directory);

    parentSelectionService = directory.get(SelectionService.class);

    createDialog(directory);
  }

  private void createDialog(Directory directory) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/transactions/splitTransactionDialog.splits",
                                    localRepository, localDirectory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));
    addAmountPanel(builder);
    addTable(builder);

    addAction = new AddAction();
    builder.add("add", addAction);

    okAction = new OkAction();
    dialog = PicsouDialog.createWithButtons(this, directory.get(JFrame.class),
                                            directory, builder.<JPanel>load(),
                                            okAction, new CancelAction()
    );
    dialog.pack();
  }

  public void show(Glob initialTransaction) {

    Glob source = initialTransaction;
    if (Transaction.isSplitPart(initialTransaction)) {
      source = parentRepository.findLinkTarget(initialTransaction, Transaction.SPLIT_SOURCE);
    }

    localRepository.rollback();

    GlobList transactions = new GlobList();
    transactions.add(source);
    transactions.addAll(parentRepository.findLinkedTo(source, Transaction.SPLIT_SOURCE));
    localRepository.reset(transactions, Transaction.TYPE);

    sourceTransaction = localRepository.get(source.getKey());

    localDirectory.get(SelectionService.class).select(localRepository.get(initialTransaction.getKey()));

    splittedTransaction = createSplittedTransaction();

    boolean enabled = false;
    setValidationEnabled(enabled);
    amountField.requestFocus();
    dialog.showCentered();
    builder.dispose();
    headerPainter.dispose();
    rendererColors.dispose();
  }

  private void setValidationEnabled(boolean enabled) {
    addAction.setEnabled(enabled);
    okAction.setEnabled(enabled);
  }

  private void addAmountPanel(GlobsPanelBuilder builder) {
    amountField = new JTextField();
    registerAmountListener();
    noteField = new JTextField();

    EnterKeyValidator validator = new EnterKeyValidator();
    amountField.addKeyListener(validator);
    noteField.addKeyListener(validator);

    builder.add("amount", amountField);
    builder.add("note", noteField);
  }

  private void addTable(GlobsPanelBuilder builder) {

    TransactionComparator transactionComparator = TransactionComparator.ASCENDING_SPLIT_AFTER;

    tableView = builder.addTable("transaction", Transaction.TYPE, transactionComparator);

    tableView.setFilter(GlobMatchers.isNotNull(Transaction.AMOUNT));

    tableView.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    headerPainter = TransactionTableHeaderPainter.install(tableView, localDirectory);

    DeleteSplitTransactionColumn deleteSplitColumn = new DeleteSplitTransactionColumn();

    LabelCustomizer defaultCustomizer = new SplitLabelCustomizer();

    tableView
      .addColumn(Lang.get("series"),
                 new CompactSeriesStringifier(localDirectory),
                 chain(defaultCustomizer, LabelCustomizers.fontSize(9)))
      .addColumn(Lang.get("label"), LABEL,
                 chain(defaultCustomizer, LabelCustomizers.BOLD))
      .addColumn(Lang.get("amount"), Transaction.AMOUNT, LabelCustomizers.ALIGN_RIGHT)
      .addColumn(Lang.get("note"), NOTE, new TransactionNoteEditor(localRepository, localDirectory))
      .addColumn(" ", deleteSplitColumn, deleteSplitColumn, GlobStringifiers.empty(transactionComparator));

    JTable table = tableView.getComponent();

    TransactionKeyListener.install(table, NOTE_COLUMN_INDEX);
    Gui.installRolloverOnButtons(table, DELETE_SPLIT_COLUMN_INDEX);

    TableUtils.setSize(table, LABEL_COLUMN_INDEX, 30 * 7);
    TableUtils.setSize(table, DELETE_SPLIT_COLUMN_INDEX, 18);
  }

  private void validate(boolean applyAndClose) {
    Double initialAmount = sourceTransaction.get(Transaction.AMOUNT);
    Double amount = null;
    if (!Strings.isNullOrEmpty(amountField.getText())) {
      try {
        amount = getEnteredAmount() * Math.signum(initialAmount);
      }
      catch (NumberFormatException e) {
        showErrorMessage("split.transaction.invalid.amount");
        return;
      }

      if (Math.abs(amount) >= Math.abs(initialAmount)) {
        showErrorMessage("split.transaction.amount.too.large",
                         Formatting.toString(Math.abs(initialAmount)));
        return;
      }
    }

    try {
      localRepository.startChangeSet();
      if (amount == null) {
        localRepository.delete(splittedTransaction);
        splittedTransaction = null;
      }
      else {
        localRepository.update(splittedTransaction.getKey(),
                               value(Transaction.AMOUNT, amount),
                               value(Transaction.NOTE, noteField.getText()));
        localRepository.update(sourceTransaction.getKey(), Transaction.AMOUNT, subtract(initialAmount, amount));
        localRepository.update(sourceTransaction.getKey(), Transaction.SPLIT, Boolean.TRUE);
      }
    }
    finally {
      localRepository.completeChangeSet();
    }

    if (applyAndClose) {
      localRepository.commitChanges(false);

      if (splittedTransaction != null) {
        parentSelectionService.select(parentRepository.get(splittedTransaction.getKey()));
      }
      else {
        parentSelectionService.select(parentRepository.get(sourceTransaction.getKey()));
      }

      dialog.setVisible(false);
    }
    else {
      splittedTransaction = createSplittedTransaction();
    }

    resetAddAmountFields();
  }

  private void showErrorMessage(String message, Object... args) {
    ErrorTip.showLeft(amountField, Lang.get(message, args), localDirectory);
  }

  private Glob createSplittedTransaction() {
    return localRepository.create(
      Transaction.TYPE,
      value(Transaction.ACCOUNT, sourceTransaction.get(Transaction.ACCOUNT)),
      value(Transaction.ORIGINAL_ACCOUNT, sourceTransaction.get(Transaction.ORIGINAL_ACCOUNT)),
      value(Transaction.IMPORT, sourceTransaction.get(Transaction.IMPORT)),
      value(Transaction.LABEL, sourceTransaction.get(Transaction.LABEL)),
      value(Transaction.ORIGINAL_LABEL, sourceTransaction.get(Transaction.ORIGINAL_LABEL)),
      value(Transaction.MONTH, sourceTransaction.get(Transaction.MONTH)),
      value(Transaction.DAY, sourceTransaction.get(Transaction.DAY)),
      value(Transaction.BUDGET_MONTH, sourceTransaction.get(Transaction.BUDGET_MONTH)),
      value(Transaction.BUDGET_DAY, sourceTransaction.get(Transaction.BUDGET_DAY)),
      value(Transaction.POSITION_MONTH, sourceTransaction.get(Transaction.POSITION_MONTH)),
      value(Transaction.POSITION_DAY, sourceTransaction.get(Transaction.POSITION_DAY)),
      value(Transaction.BANK_MONTH, sourceTransaction.get(Transaction.BANK_MONTH)),
      value(Transaction.BANK_DAY, sourceTransaction.get(Transaction.BANK_DAY)),
      value(Transaction.TRANSACTION_TYPE, sourceTransaction.get(Transaction.TRANSACTION_TYPE)),
      value(Transaction.SPLIT_SOURCE, sourceTransaction.get(Transaction.ID))
    );
  }

  private double getEnteredAmount() throws NumberFormatException {
    double result = 0.0;
    for (String item : amountField.getText().split(" ")) {
      String value = item.trim();
      if (value.length() > 0) {
        result += Double.parseDouble(value);
      }
    }
    return result;
  }

  private String filterAmount(String string) {
    return string.replaceAll("[^0-9., ]", "").replace(',', '.');
  }

  private double subtract(Double initialValue, double amount) {
    return Transaction.subtract(initialValue, amount);
  }

  private void resetAddAmountFields() {
    amountField.setText("");
    noteField.setText("");
    amountField.requestFocus();
  }

  private class AddAction extends AbstractAction {
    public AddAction() {
      super(Lang.get("split.transaction.add"));
    }

    public void actionPerformed(ActionEvent e) {
      validate(false);
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("split.transaction.ok"));
    }

    public void actionPerformed(ActionEvent e) {
      validate(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("split.transaction.close"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }

  private void registerAmountListener() {
    Document document = amountField.getDocument();
    document.addDocumentListener(new AbstractDocumentListener() {
      protected void documentChanged(DocumentEvent e) {
        setValidationEnabled(true);
      }
    });
    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        setValidationEnabled(true);
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        setValidationEnabled(false);
      }
    });
    ((AbstractDocument)document).setDocumentFilter(new DocumentFilter() {
      public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        super.insertString(fb, offset, filterAmount(string), attr);
      }

      public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        super.replace(fb, offset, length, filterAmount(text), attrs);
      }
    });
  }

  private class EnterKeyValidator extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        try {
          if (addAction.isEnabled()) {
            addAction.actionPerformed(null);
          }
        }
        catch (Exception ex) {
          // Ignored
        }
        e.consume();
      }
    }
  }

  private class DeleteSplitTransactionColumn extends AbstractTransactionEditor {
    private DeleteSplitTransactionColumn() {
      super(SplitTransactionDialog.this.tableView,
            SplitTransactionDialog.this.rendererColors,
            SplitTransactionDialog.this.descriptionService,
            SplitTransactionDialog.this.localRepository,
            SplitTransactionDialog.this.localDirectory);

      repository.addTrigger(new DefaultChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
          if (!changeSet.containsChanges(Transaction.TYPE)) {
            return;
          }
          changeSet.safeVisit(new DefaultChangeSetVisitor() {
            public void visitDeletion(Key key, FieldValues values) throws Exception {
              Double amount = values.get(Transaction.AMOUNT);
              if (amount != null) {
                GlobUtils.add(sourceTransaction, Transaction.AMOUNT, amount, repository);
              }
            }
          });
        }
      });
    }

    protected Component getComponent(Glob transaction, boolean edit) {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      addDeleteButton(panel, transaction);
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      rendererColors.setBackground(panel, transaction, isSelected, row);
      return panel;
    }

    private void addDeleteButton(JPanel panel, final Glob transaction) {
      final JButton deleteButton = new JButton();
      deleteButton.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent event) {
          tableView.getComponent().requestFocus();
          selectionService.select(transaction);
          Glob source = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
          repository.startChangeSet();
          try {
            repository.delete(transaction);
            repository.update(source.getKey(), Transaction.SPLIT,
                              repository.findLinkedTo(source, Transaction.SPLIT_SOURCE).isEmpty());
          }
          finally {
            repository.completeChangeSet();
          }
        }

        public boolean isEnabled() {
          return Transaction.isSplitPart(transaction);
        }
      });
      Gui.setIcons(deleteButton, Icons.DELETE_ICON, Icons.DELETE_ROLLOVER_ICON, Icons.DELETE_ROLLOVER_ICON);
      deleteButton.setDisabledIcon(Icons.EMPTY_13_13);
      Gui.configureIconButton(deleteButton, "Delete", new Dimension(13, 13));
      panel.add(deleteButton);
    }
  }

  private static class SplitLabelCustomizer implements LabelCustomizer {
    private Color grey = Colors.toColor("888888");

    public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
      else {
        boolean newTransaction = transaction.get(Transaction.AMOUNT) == null;
        label.setForeground(newTransaction ? grey : Color.BLACK);
      }
    }
  }
}
