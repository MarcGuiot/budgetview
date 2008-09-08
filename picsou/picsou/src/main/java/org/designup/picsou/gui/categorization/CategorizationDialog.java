package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.categorization.components.BudgetAreaComponentFactory;
import org.designup.picsou.gui.categorization.components.MultiCategoriesSeriesComponentFactory;
import org.designup.picsou.gui.categorization.components.OccasionalCategoriesComponentFactory;
import org.designup.picsou.gui.categorization.components.SeriesComponentFactory;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.DescriptionService;
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
  private JCheckBox autoSelectNextCheckBox;

  private static final int[] COLUMN_SIZES = {10, 28, 10};
  private Directory localDirectory;
  private SeriesEditionDialog seriesEditionDialog;

  public CategorizationDialog(Window parent, final GlobRepository repository, Directory directory) {
    dialog = PicsouDialog.create(parent, Lang.get("categorization.title"), directory);

    init(repository, directory);

    seriesEditionDialog = new SeriesEditionDialog(dialog, localRepository, localDirectory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationDialog.splits",
                                                      localRepository, localDirectory);

    Comparator<Glob> transactionComparator = getTransactionComparator();
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    transactionTable =
      builder.addTable("transactionTable", Transaction.TYPE, transactionComparator)
        .setDefaultLabelCustomizer(new TransactionLabelCustomizer())
        .addColumn(Lang.get("date"), new TransactionDateStringifier(transactionComparator),
                   LabelCustomizers.fontSize(9))
        .addColumn(Lang.get("label"), descriptionService.getStringifier(Transaction.LABEL), LabelCustomizers.bold())
        .addColumn(Lang.get("amount"), descriptionService.getStringifier(Transaction.AMOUNT), LabelCustomizers.alignRight());
    Gui.setColumnSizes(transactionTable.getComponent(), COLUMN_SIZES);

    autoSelectionCheckBox = new JCheckBox(new AutoSelectAction());
    autoSelectionCheckBox.setSelected(true);
    builder.add("autoSelectSimilar", autoSelectionCheckBox);

    autoSelectNextCheckBox = new JCheckBox(new AutoSelectAction());
    autoSelectNextCheckBox.setSelected(true);
    builder.add("autoSelectNext", autoSelectNextCheckBox);

    autoHideCheckBox = new JCheckBox(new AutoHideAction());
    autoHideCheckBox.setSelected(false);
    builder.add("autoHide", autoHideCheckBox);

    builder.addLabel("transactionLabel", Transaction.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        if (list.isEmpty()) {
          return null;
        }
        if (list.size() > 1) {
          return Lang.get("categorization.many.transactions.label", Integer.toString(list.size()));
        }
        return list.get(0).get(Transaction.LABEL);
      }
    }).setAutoHideIfEmpty(true);

    final CardHandler cardHandler = builder.addCardHandler("cards");

    JToggleButton invisibleBudgetAreaToggle = new JToggleButton(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        cardHandler.show("noBudgetArea");
      }
    });
    builder.add("invisibleBudgetAreaToggle", invisibleBudgetAreaToggle);
    builder.addRepeat("budgetAreas",
                      BudgetArea.getGlobs(localRepository, BudgetArea.INCOME, BudgetArea.SAVINGS,
                                          BudgetArea.RECURRING_EXPENSES, BudgetArea.EXPENSES_ENVELOPE,
                                          BudgetArea.PROJECTS, BudgetArea.OCCASIONAL_EXPENSES),
                      new BudgetAreaComponentFactory(cardHandler, invisibleBudgetAreaToggle,
                                                     localRepository, localDirectory, dialog));

    addSingleCategorySeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSingleCategorySeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING_EXPENSES, builder);

    addMultiCategoriesSeriesChooser("envelopeSeriesChooser", BudgetArea.EXPENSES_ENVELOPE, builder);
    addMultiCategoriesSeriesChooser("projectSeriesChooser", BudgetArea.PROJECTS, builder);
    addMultiCategoriesSeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);

    addOccasionalSeriesChooser(builder);

    dialog.addInPanelWithButton(builder.<JPanel>load(), new OkAction(), new CancelAction());
    dialog.pack();
  }

  private void addSingleCategorySeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationDialog.class,
                                                           "/layout/singleCategorySeriesChooserPanel.splits",
                                                           localRepository, localDirectory);

    JToggleButton invisibleIncomeToggle = new JToggleButton();
    panelBuilder.add("invisibleToggle", invisibleIncomeToggle);
    panelBuilder.addRepeat("seriesRepeat",
                           Series.TYPE,
                           GlobMatchers.linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                           new SeriesComponentFactory(invisibleIncomeToggle, localRepository, localDirectory, dialog));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    builder.add(name, panelBuilder);
  }

  private void addMultiCategoriesSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationDialog.class,
                                                           "/layout/multiCategoriesSeriesChooserPanel.splits",
                                                           localRepository, localDirectory);

    final JToggleButton invisibleToggle = new JToggleButton();
    panelBuilder.add("invisibleToggle", invisibleToggle);
    panelBuilder.addRepeat("seriesRepeat",
                           Series.TYPE,
                           GlobMatchers.linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                           new MultiCategoriesSeriesComponentFactory(budgetArea, invisibleToggle,
                                                                     localRepository, localDirectory, dialog));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    builder.add(name, panelBuilder);
  }

  private void addOccasionalSeriesChooser(GlobsPanelBuilder builder) {
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
    builder.add("editCategories", new EditCategoriesAction());
  }

  private Comparator<Glob> getTransactionComparator() {
    return new GlobFieldsComparator(Transaction.LABEL, true,
                                    Transaction.MONTH, false,
                                    Transaction.DAY, false,
                                    Transaction.AMOUNT, false);
  }

  private void init(GlobRepository repository, Directory directory) {

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(BudgetArea.TYPE, Category.TYPE, Series.TYPE, SeriesToCategory.TYPE, Month.TYPE, SeriesBudget.TYPE)
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

        Set<Key> updated = changeSet.getUpdated(Transaction.SERIES);
        if ((updated.size() > 0) && (autoSelectNextCheckBox.isSelected())) {
          selectNext(updated);
        }
      }
    });
  }

  private void selectNext(Set<Key> updated) {
    int minIndex = -1;
    for (Key key : updated) {
      int index = transactionTable.indexOf(localRepository.get(key));
      if ((index >= 0) && ((minIndex == -1) || (index < minIndex))) {
        minIndex = index;
      }
    }
    Glob transaction = findNextUncategorizedTransaction(minIndex);
    if (transaction != null) {
      selectionService.select(transaction);
    }
  }

  private Glob findNextUncategorizedTransaction(int minIndex) {
    for (int index = minIndex + 1; index < transactionTable.getRowCount(); index++) {
      Glob transaction = transactionTable.getGlobAt(index);
      if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        return transaction;
      }
    }
    for (int index = 0; index < minIndex; index++) {
      Glob transaction = transactionTable.getGlobAt(index);
      if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        return transaction;
      }
    }
    return null;
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

  public void show(GlobList transactions, boolean selectAll) {
    if (transactions.isEmpty()) {
      return;
    }
//    localRepository.rollback();
    localRepository.reset(transactions, Transaction.TYPE);
    updateAutoHide();
    if (selectAll) {
      transactionTable.select(localRepository.getAll(Transaction.TYPE), true);
    }
    else {
      transactionTable.selectFirst();
    }

    GuiUtils.showCentered(dialog);
  }

  public Dialog getDialog() {
    return dialog;
  }

  private class CreateSeriesAction extends AbstractAction {
    private final BudgetArea budgetArea;

    public CreateSeriesAction(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.showNewSeries(currentTransactions, budgetArea);
    }
  }

  private class EditAllSeriesAction extends EditSeriesAction {
    private EditAllSeriesAction(BudgetArea budgetArea) {
      super(localRepository, localDirectory, seriesEditionDialog, budgetArea);
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
      transactionTable.setFilter(GlobMatchers.fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID));
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

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }

  private class EditCategoriesAction extends AbstractAction {
    private EditCategoriesAction() {
      super(Lang.get("categorization.categories.edit"));
    }

    public void actionPerformed(ActionEvent e) {
      CategoryEditionDialog dialog = new CategoryEditionDialog(localRepository, localDirectory);
      dialog.show(GlobList.EMPTY);
    }
  }

  private static class TransactionLabelCustomizer implements LabelCustomizer {
    public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
      else if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        label.setForeground(Colors.toColor("222222"));
      }
      else {
        label.setForeground(Colors.toColor("AAAAAA"));
      }
    }
  }
}
