package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.categorization.components.BudgetAreaComponentFactory;
import org.designup.picsou.gui.categorization.components.MultiCategoriesSeriesComponentFactory;
import org.designup.picsou.gui.categorization.components.OccasionalCategoriesComponentFactory;
import org.designup.picsou.gui.categorization.components.SeriesComponentFactory;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class CategorizationView extends View implements TableView, ColorChangeListener {
  private GlobList currentTransactions = GlobList.EMPTY;
  private GlobTableView transactionTable;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private JCheckBox autoSelectionCheckBox;
  private JCheckBox autoHideCheckBox;
  private JCheckBox autoSelectNextCheckBox;
  private java.util.List<SeriesFilter> seriesRepeat = new ArrayList<SeriesFilter>();

  private static final int[] COLUMN_SIZES = {10, 28, 10};
  private SeriesEditionDialog seriesEditionDialog;

  private Color transactionColorNormal;
  private Color transactionColorError;

  public CategorizationView(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    parentDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        updateTableFilter();
      }
    }, Month.TYPE);

    colorService.addListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("categorizationView", createPanelBuilder());
  }

  private GlobsPanelBuilder createPanelBuilder() {
    JFrame parent = directory.get(JFrame.class);

    seriesEditionDialog = new SeriesEditionDialog(parent, this.repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationView.splits",
                                                      this.repository, directory);

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
    installDoubleClickHandler();


    autoSelectionCheckBox = new JCheckBox(new AutoSelectAction());
    autoSelectionCheckBox.setSelected(false);
    builder.add("autoSelectSimilar", autoSelectionCheckBox);

    autoSelectNextCheckBox = new JCheckBox(new AutoSelectAction());
    autoSelectNextCheckBox.setSelected(false);
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
                      BudgetArea.getGlobs(this.repository, BudgetArea.INCOME, BudgetArea.SAVINGS,
                                          BudgetArea.RECURRING_EXPENSES, BudgetArea.EXPENSES_ENVELOPE,
                                          BudgetArea.PROJECTS, BudgetArea.OCCASIONAL_EXPENSES),
                      new BudgetAreaComponentFactory(cardHandler, invisibleBudgetAreaToggle,
                                                     this.repository, directory, parent));

    addSingleCategorySeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSingleCategorySeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING_EXPENSES, builder);

    addMultiCategoriesSeriesChooser("envelopeSeriesChooser", BudgetArea.EXPENSES_ENVELOPE, builder);
    addMultiCategoriesSeriesChooser("projectSeriesChooser", BudgetArea.PROJECTS, builder);
    addMultiCategoriesSeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);

    addOccasionalSeriesChooser(builder);

    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory, this);
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();
    initUpdateListener(repository);
    updateTableFilter();

    return builder;
  }

  private void installDoubleClickHandler() {
    transactionTable.getComponent().addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2) {
          autoSelectSimilarTransactions();
        }
      }
    });
  }

  public void colorsChanged(ColorLocator colorLocator) {
    transactionColorNormal = colorLocator.get("categorization.transactions.normal");
    transactionColorError = colorLocator.get("categorization.transactions.error");
  }

  private void initSelectionListener() {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
        if (autoSelectionCheckBox.isSelected()) {
          autoSelectSimilarTransactions();
        }
        Set<Integer> months = new HashSet<Integer>();
        for (Glob transaction : currentTransactions) {
          months.add(transaction.get(Transaction.MONTH));
          for (SeriesFilter filter : seriesRepeat) {
            filter.filterDates(months);
          }
        }
      }
    }, Transaction.TYPE);
  }

  private void initUpdateListener(GlobRepository repository) {
    repository.addChangeListener(new DefaultChangeSetListener() {
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

  static class SeriesFilter implements GlobMatcher {
    private Integer budgetAreaId;
    private GlobRepeat repeat;
    private Set<Integer> monthIds;

    public SeriesFilter(Integer budgetAreaId, GlobRepeat repeat) {
      this.budgetAreaId = budgetAreaId;
      this.repeat = repeat;
    }

    void filterDates(Set<Integer> monthIds) {
      this.monthIds = monthIds;
      repeat.setFilter(this);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (budgetAreaId.equals(series.get(Series.BUDGET_AREA))) {
        Integer firstMonth = series.get(Series.FIRST_MONTH);
        Integer lastMonth = series.get(Series.LAST_MONTH);
        if (firstMonth == null && lastMonth == null) {
          return true;
        }
        if (firstMonth == null) {
          firstMonth = 0;
        }
        if (lastMonth == null) {
          lastMonth = Integer.MAX_VALUE;
        }
        for (Integer id : monthIds) {
          if (id < firstMonth || id > lastMonth) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }

  private void addSingleCategorySeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/singleCategorySeriesChooserPanel.splits",
                                                           repository, directory);

    JToggleButton invisibleToggle = new JToggleButton();
    panelBuilder.add("invisibleToggle", invisibleToggle);
    seriesRepeat.add(
      new SeriesFilter(budgetArea.getId(),
                       panelBuilder.addRepeat("seriesRepeat",
                                              Series.TYPE,
                                              linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                                              new SeriesComponentFactory(invisibleToggle, repository, directory))));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    builder.add(name, panelBuilder);
  }

  private void addMultiCategoriesSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/multiCategoriesSeriesChooserPanel.splits",
                                                           repository, directory);

    final JToggleButton invisibleToggle = new JToggleButton();
    panelBuilder.add("invisibleToggle", invisibleToggle);
    seriesRepeat.add(new SeriesFilter(budgetArea.getId(),
                                      panelBuilder.addRepeat("seriesRepeat",
                                                             Series.TYPE,
                                                             linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                                                             new MultiCategoriesSeriesComponentFactory(budgetArea, invisibleToggle,
                                                                                                       repository, directory))));
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
                                                               repository, directory));
    builder.add("editCategories", new EditCategoriesAction());
  }

  private Comparator<Glob> getTransactionComparator() {
    return new GlobFieldsComparator(Transaction.LABEL, true,
                                    Transaction.MONTH, false,
                                    Transaction.DAY, false,
                                    Transaction.AMOUNT, false);
  }

  private static Directory createLocalDirectory(Directory directory) {
    Directory localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);
    return localDirectory;
  }

  private void selectNext(Set<Key> updated) {
    int minIndex = -1;
    for (Key key : updated) {
      int index = transactionTable.indexOf(repository.get(key));
      if ((index >= 0) && ((minIndex == -1) || (index < minIndex))) {
        minIndex = index;
      }
    }
    Glob transaction = findNextUncategorizedTransaction(minIndex);
    if (transaction != null) {
      selectionService.select(transaction);
    }
    else {
      selectionService.clear(Transaction.TYPE);
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
    Glob series = repository.get(seriesKey);
    GlobList seriesToCategories = repository.findLinkedTo(series, SeriesToCategory.SERIES);
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
      repository.enterBulkDispatchingMode();
      for (Glob transaction : currentTransactions) {
        repository.setTarget(transaction.getKey(), Transaction.SERIES, seriesKey);
        repository.setTarget(transaction.getKey(), Transaction.CATEGORY, categoryKey);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
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
      super(repository, directory, seriesEditionDialog, budgetArea);
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

    final GlobList similarTransactions = getSimilarTransactions(currentTransactions.get(0));
    if (similarTransactions.size() > 1) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          selectionService.select(similarTransactions, Transaction.TYPE);
        }
      });
    }
  }

  public GlobList getSimilarTransactions(Glob reference) {
    final String referenceLabel = reference.get(Transaction.LABEL_FOR_CATEGORISATION);
    if (Strings.isNullOrEmpty(referenceLabel)) {
      return new GlobList(reference);
    }
    GlobList result = new GlobList();
    for (Glob transaction : transactionTable.getGlobs()) {
      if (referenceLabel.equals(transaction.get(Transaction.LABEL_FOR_CATEGORISATION))) {
        result.add(transaction);
      }
    }
    return result;
  }

  private void updateTableFilter() {
    if (transactionTable == null) {
      return;
    }

    GlobMatcher matcher =
      and(
        fieldEquals(Transaction.PLANNED, false),
        autoHideCheckBox.isSelected() ? fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID) : ALL,
        fieldIn(Transaction.MONTH, selectedMonthIds)
      );

    transactionTable.setFilter(matcher);
  }

  private class AutoHideAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      updateTableFilter();
    }
  }

  private class EditCategoriesAction extends AbstractAction {
    private EditCategoriesAction() {
      super(Lang.get("categorization.categories.edit"));
    }

    public void actionPerformed(ActionEvent e) {
      CategoryEditionDialog dialog = new CategoryEditionDialog(repository, directory);
      dialog.show(GlobList.EMPTY);
    }
  }

  private class TransactionLabelCustomizer implements LabelCustomizer {
    public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
      else if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        label.setForeground(transactionColorError);
      }
      else {
        label.setForeground(transactionColorNormal);
      }
    }
  }

  public void addTableListener(TableModelListener listener) {
    this.transactionTable.getComponent().getModel().addTableModelListener(listener);
  }

  public GlobList getDisplayedGlobs() {
    return transactionTable.getGlobs();
  }
}
