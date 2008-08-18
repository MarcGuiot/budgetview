package org.designup.picsou.gui.transactions.split;

import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.components.PicsouTableHeaderCustomizer;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.BalanceStringifier;
import org.designup.picsou.gui.description.TransactionCategoriesStringifier;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.categorization.TransactionCategoryChooserCallback;
import org.designup.picsou.gui.transactions.columns.*;
import org.designup.picsou.gui.transactions.details.CategorisationHyperlinkButton;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.LABEL;
import static org.designup.picsou.model.Transaction.NOTE;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.AbstractGlobSelectionAction;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.alignRight;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class SplitTransactionDialog {
  public static final int CATEGORY_COLUMN_INDEX = 0;
  public static final int LABEL_COLUMN_INDEX = 1;
  public static final int NOTE_COLUMN_INDEX = 3;
  public static final int REMOVE_SPLIT_COLUMN_INDEX = 4;

  public static Icon EXPANDED_ICON = Gui.ICON_LOCATOR.get("arrowdown.png");
  public static Icon COLLAPSED_ICON = Gui.ICON_LOCATOR.get("arrowright.png");
  public static Icon ROLLOVER_ICON = Gui.ICON_LOCATOR.get("arrowrightrollover.png");
  public static Icon CHOOSER_ICON = Gui.ICON_LOCATOR.get("add.png");
  public static Icon CHOOSER_ROLLOVER_ICON = Gui.ICON_LOCATOR.get("addrollover.png");

  private Glob transactionToSplit;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private DescriptionService descriptionService;
  private ColorService colorService;
  private TransactionRendererColors rendererColors;

  private JPanel initialAmountPanel = new JPanel();
  private JLabel initialLabel = new JLabel();
  private JLabel initialAmount = new JLabel();
  private JLabel initialDate = new JLabel();
  private JLabel messageLabel = new JLabel("");
  private JTextField amountField;
  private JTextField noteField;
  private JCheckBox dispensableBox = new JCheckBox();
  private PicsouDialog dialog;
  private AddAction addAction = new AddAction();
  private JPanel addAmountPanel = new JPanel();
  private JPanel subAmountPanel = new JPanel();
  private JToggleButton toggleButton = new JToggleButton();
  private BalanceColorChangeListener colorChangeListener;
  private Glob splittedTransaction;
  private SelectionService selectionServiceForSplitPanel;
  private DefaultDirectory directoryForSplitPanel;
  private LocalGlobRepository repositoryForSplitPanel;

  public SplitTransactionDialog(Glob initialTransaction, GlobRepository repository, Directory directory) {
    if (Transaction.isSplitPart(initialTransaction)) {
      initialTransaction = repository.findLinkTarget(initialTransaction, Transaction.SPLIT_SOURCE);
    }

    localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Category.TYPE, Series.TYPE, SeriesToCategory.TYPE, Month.TYPE)
        .copy(initialTransaction)
        .copy(repository.findLinkedTo(initialTransaction, Transaction.SPLIT_SOURCE))
        .get();

    transactionToSplit = localRepository.get(initialTransaction.getKey());

    repositoryForSplitPanel = LocalGlobRepositoryBuilder.init(localRepository)
      .copy(Category.TYPE, Series.TYPE, SeriesToCategory.TYPE, Month.TYPE)
      .get();
    selectionServiceForSplitPanel = new SelectionService();
    directoryForSplitPanel = new DefaultDirectory(directory);
    directoryForSplitPanel.add(selectionServiceForSplitPanel);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    descriptionService = localDirectory.get(DescriptionService.class);
    colorService = localDirectory.get(ColorService.class);
    rendererColors = new TransactionRendererColors(localDirectory);

    dialog = PicsouDialog.create(directory.get(JFrame.class), Lang.get("split.transaction.title"));

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/splitTransaction.splits",
                                                      localRepository, localDirectory);
    addInitialTransactionPanel(builder);
    addSwitcher(builder);
    addAmountPanel(builder);
    addTable(builder);
    builder.add("ok", new OkAction());
    builder.add("cancel", new CancelAction());
    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        dialog.setContentPane((JPanel)component);
      }
    });
    builder.load();

    toggleButton.setSelected(true);
    createSplittedTransaction();
    showAddAmountPanel();
  }

  public void show() {
    amountField.requestFocus();
    Dimension defaultSize = new Dimension(500, 600);
    dialog.setPreferredSize(defaultSize);
    dialog.setSize(defaultSize);
    GuiUtils.showCentered(dialog);
  }

  private void addInitialTransactionPanel(GlobsPanelBuilder builder) {
    TransactionComparator comparator = TransactionComparator.ASCENDING;

    initialLabel.setText(transactionToSplit.get(Transaction.LABEL));
    initialAmount.setText(descriptionService.getStringifier(Transaction.AMOUNT).toString(transactionToSplit, localRepository));

    initialDate.setText("le " + new TransactionDateStringifier(comparator).toString(transactionToSplit, localRepository));
    initialDate.setFont(Gui.getDefaultFont().deriveFont(Font.PLAIN, Gui.getDefaultFont().getSize() - 2));

    final BalanceStringifier balanceStringifier = new BalanceStringifier(localRepository, localDirectory);
    updateInitialAmount(balanceStringifier);
    colorChangeListener = new BalanceColorChangeListener(balanceStringifier);
    colorService.addListener(colorChangeListener);

    builder.add("initialAmountPanel", initialAmountPanel);
    builder.add("initialLabel", initialLabel);
    builder.add("initialAmount", initialAmount);
    builder.add("initialDate", initialDate);
  }

  private void updateInitialAmount(BalanceStringifier balanceStringifier) {
    initialAmount.setText(balanceStringifier.toString(localRepository.getAll(Transaction.TYPE), localRepository));
  }

  private void addAmountPanel(GlobsPanelBuilder builder) {
    amountField = new JTextField();
    registerAmountListener();
    noteField = new JTextField();

    EnterKeyValidator validator = new EnterKeyValidator();
    amountField.addKeyListener(validator);
    noteField.addKeyListener(validator);
    dispensableBox.addKeyListener(validator);
    AbstractGlobSelectionAction categorizationAction =
      new AbstractGlobSelectionAction(Transaction.TYPE, directoryForSplitPanel) {

        public String toString(GlobList globs) {
          return "categorization";
        }

        public void actionPerformed(ActionEvent e) {
          CategorizationDialog categorizationDialog =
            new CategorizationDialog(dialog, repositoryForSplitPanel, directoryForSplitPanel);
          categorizationDialog.show(new GlobList(splittedTransaction), false, true);
        }
      };

    builder.add("addAmountPanel", addAmountPanel);
    builder.add("category",
                new CategorisationHyperlinkButton(categorizationAction, repositoryForSplitPanel, directoryForSplitPanel));
    builder.add("amount", amountField);
    builder.add("message", messageLabel);
    builder.add("note", noteField);
    builder.add("dispensableBox", dispensableBox);
    builder.add("add", addAction);
  }

  private void addSwitcher(GlobsPanelBuilder builder) {
    Gui.configureIconButton(toggleButton, "switcher", new Dimension(13, 13));
    toggleButton.setIcon(COLLAPSED_ICON);
    toggleButton.setText(Lang.get("split.transaction.add.amount"));
    toggleButton.setFont(Gui.getDefaultFont());
    Gui.setRolloverIcon(toggleButton, ROLLOVER_ICON);
    toggleButton.setSelectedIcon(EXPANDED_ICON);
    builder.add("switchToAddAmountButton", toggleButton);

    toggleButton.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        showOrHideAddAmountPanel();
      }
    });

    Gui.setRolloverColor(toggleButton, Color.GRAY);
  }

  private void showOrHideAddAmountPanel() {
    if (addAmountPanel.isVisible()) {
      showAddAmountSwitcher();
    }
    else {
      showAddAmountPanel();
    }
  }

  private void addTable(GlobsPanelBuilder builder) {
    builder.add("subAmountPanel", subAmountPanel);

    TransactionComparator transactionComparator = TransactionComparator.ASCENDING;
    GlobTableView view = builder.addTable("transaction", Transaction.TYPE, transactionComparator);

    view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    view.setHeaderCustomizer(new PicsouTableHeaderCustomizer(localDirectory, PicsouColors.TRANSACTION_TABLE_HEADER_TITLE),
                             new PicsouTableHeaderPainter(view, localDirectory));

    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    GlobStringifier categoriesStringifier =
      new TransactionCategoriesStringifier(descriptionService.getStringifier(Category.TYPE));
    AbstractGlobSelectionAction categorizationAction =
      new AbstractGlobSelectionAction(Transaction.TYPE, directoryForSplitPanel) {

        public String toString(GlobList globs) {
          return "categorization";
        }

        public void actionPerformed(ActionEvent e) {
          CategorizationDialog categorizationDialog =
            new CategorizationDialog(dialog, localRepository, directoryForSplitPanel);
          categorizationDialog.show(new GlobList(splittedTransaction), false, true);
        }
      };

    TransactionSeriesColumn seriesColumn =
      new TransactionSeriesColumn(view, rendererColors,
                                    descriptionService, localRepository, localDirectory);

    DeleteSplitTransactionColumn deleteSplitColumn =
      new DeleteSplitTransactionColumn(transactionToSplit, view, rendererColors, descriptionService,
                                       localRepository, localDirectory);

    view
      .addColumn(descriptionService.getLabel(Category.TYPE), seriesColumn, seriesColumn,
                 categoriesStringifier.getComparator(localRepository))
      .addColumn(LABEL)
      .addColumn(Lang.get("amount"),
                 amountStringifier,
                 chain(alignRight(), LabelCustomizers.stringifier(amountStringifier, localRepository)))
      .addColumn(NOTE, new TransactionNoteEditor(localRepository, localDirectory))
      .addColumn(" ", deleteSplitColumn, deleteSplitColumn, transactionComparator);

    JTable table = view.getComponent();
    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          CATEGORY_COLUMN_INDEX));

    TransactionViewUtils.installKeyboardCategorization(table, categorizationAction, NOTE_COLUMN_INDEX);
    Gui.installRolloverOnButtons(table, new int[]{CATEGORY_COLUMN_INDEX, REMOVE_SPLIT_COLUMN_INDEX});

    adjustColumnsSize(table);
  }

  private void adjustColumnsSize(JTable table) {
    adjustColumnSize(table, LABEL_COLUMN_INDEX);
    adjustColumnSize(table, REMOVE_SPLIT_COLUMN_INDEX);
  }

  private void adjustColumnSize(JTable table, int columnIndex) {
    TableUtils.setSize(table, columnIndex,
                       TableUtils.getPreferredWidth(
                         TableUtils.getRenderedComponent(table, transactionToSplit, 0, columnIndex)));
  }

  private void doSplit() {
    Double initialAmount = transactionToSplit.get(Transaction.AMOUNT);

    double amount;
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

    double remainder = subtract(initialAmount, amount);

    String note = noteField.getText();

    try {
      localRepository.enterBulkDispatchingMode();
      repositoryForSplitPanel.commitChanges(false);
      localRepository.update(transactionToSplit.getKey(), Transaction.AMOUNT, remainder);
      localRepository.update(transactionToSplit.getKey(), Transaction.SPLIT, Boolean.TRUE);

      localRepository.update(splittedTransaction.getKey(),
                             value(Transaction.AMOUNT, amount),
                             value(Transaction.DISPENSABLE, dispensableBox.isSelected()),
                             value(Transaction.NOTE, note));

      createSplittedTransaction();
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    resetAddAmountFields();
  }

  private void createSplittedTransaction() {
    splittedTransaction = repositoryForSplitPanel.create(
      Transaction.TYPE,
//      value(Transaction.CATEGORY, transactionToSplit.get(Transaction.CATEGORY)),
value(Transaction.ACCOUNT, transactionToSplit.get(Transaction.ACCOUNT)),
value(Transaction.IMPORT, transactionToSplit.get(Transaction.IMPORT)),
value(Transaction.LABEL, transactionToSplit.get(Transaction.LABEL)),
value(Transaction.ORIGINAL_LABEL, transactionToSplit.get(Transaction.ORIGINAL_LABEL)),
value(Transaction.MONTH, transactionToSplit.get(Transaction.MONTH)),
value(Transaction.DAY, transactionToSplit.get(Transaction.DAY)),
value(Transaction.BANK_MONTH, transactionToSplit.get(Transaction.BANK_MONTH)),
value(Transaction.BANK_DAY, transactionToSplit.get(Transaction.BANK_DAY)),
value(Transaction.TRANSACTION_TYPE, transactionToSplit.get(Transaction.TRANSACTION_TYPE)),
value(Transaction.SPLIT_SOURCE, transactionToSplit.get(Transaction.ID))
    );
    selectionServiceForSplitPanel.select(splittedTransaction);
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

  private void amountUpdated() {
    addAction.setEnabled(amountField.getText().trim().length() > 0);
  }

  private String filterAmount(String string) {
    return string.replaceAll("[^0-9., ]", "").replace(',', '.');
  }

  private double subtract(Double initialValue, double amount) {
    return Transaction.subtract(initialValue, amount);
  }

  private void showAddAmountPanel() {
    addAmountPanel.setVisible(true);
    amountField.requestFocus();
  }

  private void showAddAmountSwitcher() {
    resetAddAmountFields();
    addAmountPanel.setVisible(false);
  }

  private void resetAddAmountFields() {
    amountField.setText("");
    noteField.setText("");
    messageLabel.setText("");
    dispensableBox.setSelected(false);
    amountField.requestFocus();
  }

  private class AddAction extends AbstractAction {
    public AddAction() {
      super(Lang.get("split.transaction.add"));
      setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
      doSplit();
    }
  }


  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("split.transaction.ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(true);
      dialog.setVisible(false);
      colorService.removeListener(colorChangeListener);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("split.transaction.close"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.dispose();
      dialog.setVisible(false);
      colorService.removeListener(colorChangeListener);
    }
  }

  private void registerAmountListener() {
    Document document = amountField.getDocument();
    document.addDocumentListener(new DocumentListener() {
      public void insertUpdate(DocumentEvent e) {
        amountUpdated();
      }

      public void changedUpdate(DocumentEvent e) {
        amountUpdated();
      }

      public void removeUpdate(DocumentEvent e) {
        amountUpdated();
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

  private class BalanceColorChangeListener implements ColorChangeListener {
    private final BalanceStringifier balanceStringifier;

    public BalanceColorChangeListener(BalanceStringifier balanceStringifier) {
      this.balanceStringifier = balanceStringifier;
    }

    public void colorsChanged(ColorLocator colorLocator) {
      updateInitialAmount(balanceStringifier);
    }
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

  private class MyCategoryChooserCallback extends TransactionCategoryChooserCallback {

    private GlobList transactions;

    private MyCategoryChooserCallback(Glob transaction) {
      this.transactions = new GlobList(transaction);
    }

    public void categorySelected(Glob category) {
      amountField.requestFocusInWindow();
      amountField.requestFocus();
    }

    protected GlobRepository getRepository() {
      return localRepository;
    }

    protected GlobList getReferenceTransactions() {
      return transactions;
    }
  }
}
