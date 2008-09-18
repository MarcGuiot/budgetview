package org.designup.picsou.gui.transactions.split;

import org.designup.picsou.gui.categorization.components.CompactSeriesStringifier;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.transactions.columns.AbstractTransactionEditor;
import org.designup.picsou.gui.transactions.columns.TransactionKeyListener;
import org.designup.picsou.gui.transactions.columns.TransactionNoteEditor;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.Icons;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;

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
  private JLabel messageLabel = new JLabel("");
  private JTextField amountField;
  private JTextField noteField;
  private PicsouDialog dialog;
  private SplitTransactionDialog.OkAction okAction;
  private GlobRepository parentRepository;
  private GlobTableView tableView;
  private SelectionService selectionService;

  public SplitTransactionDialog(GlobRepository repository, Directory directory) {
    this.parentRepository = repository;

    localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Month.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE)
        .get();

    localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);
    descriptionService = localDirectory.get(DescriptionService.class);
    rendererColors = new TransactionRendererColors(localDirectory);

    parentSelectionService = directory.get(SelectionService.class);

    createDialog(directory);
  }

  private void createDialog(Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/splitTransaction.splits",
                                                      localRepository, localDirectory);
    addAmountPanel(builder);
    addTable(builder);

    okAction = new OkAction();
    dialog = PicsouDialog.createWithButtons(directory.get(JFrame.class),
                                            builder.<JPanel>load(),
                                            okAction, new CancelAction(),
                                            directory);
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

    okAction.setEnabled(false);
    amountField.requestFocus();
    GuiUtils.showCentered(dialog);
  }

  private void addAmountPanel(GlobsPanelBuilder builder) {
    amountField = new JTextField();
    registerAmountListener();
    noteField = new JTextField();

    EnterKeyValidator validator = new EnterKeyValidator();
    amountField.addKeyListener(validator);
    noteField.addKeyListener(validator);

    builder.add("amount", amountField);
    builder.add("message", messageLabel);
    builder.add("note", noteField);
  }

  private void addTable(GlobsPanelBuilder builder) {

    TransactionComparator transactionComparator = TransactionComparator.ASCENDING;

    tableView = builder.addTable("transaction", Transaction.TYPE, transactionComparator);

    tableView.setFilter(GlobMatchers.isNotNull(Transaction.AMOUNT));

    tableView.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    PicsouTableHeaderPainter.install(tableView, localDirectory);

    DeleteSplitTransactionColumn deleteSplitColumn = new DeleteSplitTransactionColumn();

    LabelCustomizer defaultCustomizer = new SplitLabelCustomizer();

    tableView
      .addColumn(Lang.get("category"),
                 new CompactSeriesStringifier(localDirectory),
                 chain(defaultCustomizer, LabelCustomizers.fontSize(9)))
      .addColumn(Lang.get("label"), LABEL,
                 chain(defaultCustomizer, LabelCustomizers.BOLD))
      .addColumn(Lang.get("amount"), Transaction.AMOUNT, LabelCustomizers.ALIGN_RIGHT)
      .addColumn(Lang.get("note"), NOTE, new TransactionNoteEditor(localRepository, localDirectory))
      .addColumn(" ", deleteSplitColumn, deleteSplitColumn, transactionComparator);

    JTable table = tableView.getComponent();

    TransactionKeyListener.install(table, NOTE_COLUMN_INDEX);
    Gui.installRolloverOnButtons(table, DELETE_SPLIT_COLUMN_INDEX);

    TableUtils.setSize(table, LABEL_COLUMN_INDEX, 30 * 7);
    TableUtils.setSize(table, DELETE_SPLIT_COLUMN_INDEX, 18);
  }

  private void validate() {
    Double initialAmount = sourceTransaction.get(Transaction.AMOUNT);

    Double amount = null;
    if (!Strings.isNullOrEmpty(amountField.getText())) {
      try {
        amount = getEnteredAmount() * Math.signum(initialAmount);
      }
      catch (NumberFormatException e) {
        messageLabel.setText(Lang.get("split.transaction.invalid.amount"));
        return;
      }

      if (Math.abs(amount) >= Math.abs(initialAmount)) {
        messageLabel.setText(Lang.get("split.transaction.amount.too.large", Math.abs(initialAmount)));
        return;
      }
    }

    try {
      localRepository.enterBulkDispatchingMode();
      if (amount == null) {
        localRepository.delete(splittedTransaction.getKey());
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
      localRepository.completeBulkDispatchingMode();
    }

    localRepository.commitChanges(false);

    if (splittedTransaction != null) {
      parentSelectionService.select(parentRepository.get(splittedTransaction.getKey()));
    }
    else {
      parentSelectionService.select(parentRepository.get(sourceTransaction.getKey()));
    }

    dialog.setVisible(false);
    resetAddAmountFields();
  }

  private Glob createSplittedTransaction() {
    return localRepository.create(
      Transaction.TYPE,
      value(Transaction.ACCOUNT, sourceTransaction.get(Transaction.ACCOUNT)),
      value(Transaction.IMPORT, sourceTransaction.get(Transaction.IMPORT)),
      value(Transaction.LABEL, sourceTransaction.get(Transaction.LABEL)),
      value(Transaction.ORIGINAL_LABEL, sourceTransaction.get(Transaction.ORIGINAL_LABEL)),
      value(Transaction.MONTH, sourceTransaction.get(Transaction.MONTH)),
      value(Transaction.DAY, sourceTransaction.get(Transaction.DAY)),
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
    messageLabel.setText("");
    amountField.requestFocus();
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("split.transaction.ok"));
    }

    public void actionPerformed(ActionEvent e) {
      validate();
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("split.transaction.close"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.dispose();
      dialog.setVisible(false);
    }
  }

  private void registerAmountListener() {
    Document document = amountField.getDocument();
    document.addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        okAction.setEnabled(true);
      }

      public void changedUpdate(DocumentEvent e) {
        okAction.setEnabled(true);
      }

      public void removeUpdate(DocumentEvent e) {
        okAction.setEnabled(true);
      }
    });
    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        okAction.setEnabled(true);
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        okAction.setEnabled(false);
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
          if (okAction.isEnabled()) {
            okAction.actionPerformed(null);
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

    protected Component getComponent(Glob transaction, boolean render) {
      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      addDeleteButton(panel, transaction);
      panel.add(Box.createRigidArea(new Dimension(3, 0)));
      rendererColors.setBackground(panel, isSelected, row);
      return panel;
    }

    private void addDeleteButton(JPanel panel, final Glob transaction) {
      final JButton deleteButton = new JButton();
      deleteButton.setAction(new AbstractAction() {
        public void actionPerformed(ActionEvent event) {
          tableView.getComponent().requestFocus();
          selectionService.select(transaction);
          Glob source = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
          repository.delete(transaction.getKey());
          repository.update(source.getKey(), Transaction.SPLIT,
                            repository.findLinkedTo(source, Transaction.SPLIT_SOURCE).isEmpty());
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
