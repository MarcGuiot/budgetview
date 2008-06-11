package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.utils.TableUtils;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.gui.views.utils.LabelCustomizers;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.alignRight;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.chain;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import static org.designup.picsou.model.Transaction.LABEL;
import static org.designup.picsou.model.Transaction.NOTE;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;

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

  private Glob transaction;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;
  private DescriptionService descriptionService;
  private ColorService colorService;
  private TransactionRendererColors rendererColors;

  private JStyledPanel initialAmountPanel = new JStyledPanel();
  private JLabel initialLabel = new JLabel();
  private JLabel initialAmount = new JLabel();
  private JLabel initialDate = new JLabel();
  private JLabel messageLabel = new JLabel("");
  private JTextField amountField;
  private JTextField noteField;
  private JCheckBox dispensableBox = new JCheckBox();
  private PicsouDialog dialog;
  private JLabel categoryLabel = new JLabel();
  private JButton categoryChooserButton;
  private AddAction addAction = new AddAction();
  private JStyledPanel addAmountPanel = new JStyledPanel();
  private JStyledPanel subAmountPanel = new JStyledPanel();
  private JToggleButton toggleButton = new JToggleButton();
  private CategoryChooserAction categoryChooserAction;
  private BalanceColorChangeListener colorChangeListener;
  private GlobStringifier categoryStringifier;
  private Glob selectedCategory;

  public SplitTransactionDialog(Glob initialTransaction, GlobRepository repository, Directory directory) {
    if (Transaction.isSplitPart(initialTransaction)) {
      initialTransaction = repository.findLinkTarget(initialTransaction, Transaction.SPLIT_SOURCE);
    }

    localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Category.TYPE)
        .copy(initialTransaction)
        .copy(repository.findLinkedTo(initialTransaction, Transaction.SPLIT_SOURCE))
        .get();

    transaction = localRepository.get(initialTransaction.getKey());

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());
    descriptionService = localDirectory.get(DescriptionService.class);
    colorService = localDirectory.get(ColorService.class);
    rendererColors = new TransactionRendererColors(localDirectory);
    categoryChooserAction = new CategoryChooserAction(rendererColors, localRepository, localDirectory);
    categoryStringifier = descriptionService.getStringifier(Category.TYPE);

    GlobsPanelBuilder builder = GlobsPanelBuilder.init(localRepository, localDirectory);
    addInitialTransactionPanel(builder);
    addSwitcher(builder);
    addAmountPanel(builder);
    addTable(builder);
    builder.add("ok", new OkAction());
    builder.add("cancel", new CancelAction());

    JPanel panel = (JPanel)builder.parse(getClass(), "/layout/splitTransaction.splits");
    dialog = PicsouDialog.create(directory.get(JFrame.class), Lang.get("split.transaction.title"));
    dialog.getContentPane().add(panel);

    toggleButton.setSelected(true);
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
    TransactionComparator comparator = new TransactionComparator(true);

    initialLabel.setText(transaction.get(Transaction.LABEL));
    initialAmount.setText(descriptionService.getStringifier(Transaction.AMOUNT).toString(transaction, localRepository));

    initialDate.setText("le " + new TransactionDateStringifier(comparator).toString(transaction, localRepository));
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
    initialAmount.setText(balanceStringifier.toString(localRepository.getAll(Transaction.TYPE)));
  }

  private void addAmountPanel(GlobsPanelBuilder builder) {
    selectedCategory = Category.find(MasterCategory.NONE.getName(), localRepository);
    categoryChooserButton = new JButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        CategoryChooserDialog categoryChooserDialog = new CategoryChooserDialog(new CategoryChooserCallback() {
          public void categorySelected(Glob category) {
            selectedCategory = category;
            updateCategoryField();
            amountField.requestFocusInWindow();
            amountField.requestFocus();
          }
        }, rendererColors, localRepository, localDirectory);
        categoryChooserDialog.show(new GlobList(transaction));
      }
    });
    Gui.configureIconButton(categoryChooserButton, "categoryChooser", new Dimension(13, 13));
    Gui.setIcons(categoryChooserButton, CHOOSER_ICON, CHOOSER_ROLLOVER_ICON, CHOOSER_ROLLOVER_ICON);

    amountField = new JTextField();
    registerAmountListener();
    noteField = new JTextField();

    EnterKeyValidator validator = new EnterKeyValidator();
    amountField.addKeyListener(validator);
    noteField.addKeyListener(validator);
    dispensableBox.addKeyListener(validator);

    builder.add("addAmountPanel", addAmountPanel);
    builder.add("category", categoryLabel);
    builder.add("categoryChooser", categoryChooserButton);
    builder.add("amount", amountField);
    builder.add("message", messageLabel);
    builder.add("note", noteField);
    builder.add("dispensableBox", dispensableBox);
    builder.add("add", addAction);
    builder.add("cancelAdd", new CancelAddAction());
  }

  private void updateCategoryField() {
    categoryLabel.setText(categoryStringifier.toString(selectedCategory, localRepository));
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

    TransactionComparator transactionComparator = new TransactionComparator(true);
    GlobTableView view = builder.addTable(Transaction.TYPE, transactionComparator);

    view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    TransactionViewUtils.configureHeader(view, localDirectory);

    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    GlobStringifier categoriesStringifier =
      new TransactionCategoriesStringifier(descriptionService.getStringifier(Category.TYPE));

    TransactionCategoryColumn categoryColumn =
      new TransactionCategoryColumn(categoryChooserAction, view, rendererColors,
                                    descriptionService, localRepository, localDirectory);

    DeleteSplitTransactionColumn deleteSplitColumn =
      new DeleteSplitTransactionColumn(transaction, view, rendererColors, descriptionService,
                                       localRepository, localDirectory);

    view
      .addColumn(descriptionService.getLabel(Category.TYPE), categoryColumn, categoryColumn,
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

    TransactionViewUtils.installKeyboardCategorization(table, categoryChooserAction, NOTE_COLUMN_INDEX);
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
                         TableUtils.getRenderedComponent(table, transaction, 0, columnIndex)));
  }

  private void doSplit() {
    Double initialAmount = transaction.get(Transaction.AMOUNT);

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
      localRepository.update(transaction.getKey(), Transaction.AMOUNT, remainder);
      localRepository.update(transaction.getKey(), Transaction.SPLIT, Boolean.TRUE);

      localRepository.create(Transaction.TYPE,
                             value(Transaction.AMOUNT, amount),
                             value(Transaction.NOTE, note),
                             value(Transaction.CATEGORY, selectedCategory.get(Category.ID)),
                             value(Transaction.ACCOUNT, transaction.get(Transaction.ACCOUNT)),
                             value(Transaction.DAY, transaction.get(Transaction.DAY)),
                             value(Transaction.IMPORT, transaction.get(Transaction.IMPORT)),
                             value(Transaction.LABEL, transaction.get(Transaction.LABEL)),
                             value(Transaction.ORIGINAL_LABEL, transaction.get(Transaction.ORIGINAL_LABEL)),
                             value(Transaction.MONTH, transaction.get(Transaction.MONTH)),
                             value(Transaction.TRANSACTION_TYPE, transaction.get(Transaction.TRANSACTION_TYPE)),
                             value(Transaction.SPLIT_SOURCE, transaction.get(Transaction.ID)),
                             value(Transaction.DISPENSABLE, dispensableBox.isSelected()));
    }
    finally {
      localRepository.completeBulkDispatchingMode();
    }
    resetAddAmountFields();
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
    resetSelectedCategory();
    addAmountPanel.setVisible(false);
  }

  private void resetSelectedCategory() {
    selectedCategory = Category.find(MasterCategory.NONE.getName(), localRepository);
    updateCategoryField();
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

  private class CancelAddAction extends AbstractAction {
    public CancelAddAction() {
      super(Lang.get("split.transaction.cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      resetAddAmountFields();
      resetSelectedCategory();
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

    public void colorsChanged(ColorSource colorSource) {
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
}
