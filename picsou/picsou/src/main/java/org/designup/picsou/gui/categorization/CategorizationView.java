package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categories.CategoryEditionDialog;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.filtering.CustomFilterMessagePanel;
import org.designup.picsou.gui.components.filtering.FilterSet;
import org.designup.picsou.gui.components.filtering.FilterSetListener;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.autoTooltip;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Pair;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class CategorizationView extends View implements TableView, Filterable, ColorChangeListener {
  private GlobList currentTransactions = GlobList.EMPTY;
  private GlobTableView transactionTable;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private JCheckBox autoHideCheckBox;
  private JCheckBox autoSelectNextCheckBox;
  private java.util.List<Pair<PicsouMatchers.SeriesFirstEndDateFilter, GlobRepeat>> seriesRepeat =
    new ArrayList<Pair<PicsouMatchers.SeriesFirstEndDateFilter, GlobRepeat>>();

  private Directory parentDirectory;

  private static final int[] COLUMN_SIZES = {10, 12, 28, 10};
  private SeriesEditionDialog seriesEditionDialog;

  private Color transactionColorNormal;
  private Color transactionColorError;
  private PicsouTableHeaderPainter headerPainter;
  private FilterSet filterSet;
  private GlobMatcher filter = GlobMatchers.ALL;

  public CategorizationView(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    this.parentDirectory = parentDirectory;
    parentDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList selectedMonth = selection.getAll(Month.TYPE);
        selectedMonthIds = selectedMonth.getValueSet(Month.ID);
        updateTableFilter();
        selectionService.select(selectedMonth, Month.TYPE);
      }
    }, Month.TYPE);

    colorService.addListener(this);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("categorizationView", createPanelBuilder());
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateTableFilter();
    headerPainter.setFiltered(matcher != GlobMatchers.ALL);
  }

  private GlobsPanelBuilder createPanelBuilder() {
    JFrame parent = directory.get(JFrame.class);

    seriesEditionDialog = new SeriesEditionDialog(parent, this.repository, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorizationView.splits",
                                                      this.repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    CategorizationGaugePanel gauge = new CategorizationGaugePanel(repository, parentDirectory);
    builder.add("gaugePanel", gauge.getPanel());

    Comparator<Glob> transactionComparator = getTransactionComparator();
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    transactionTable =
      builder.addTable("transactionTable", Transaction.TYPE, transactionComparator)
        .setDefaultLabelCustomizer(new TransactionLabelCustomizer())
        .addColumn(Lang.get("date"), new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER),
                   LabelCustomizers.fontSize(9))
        .addColumn(Lang.get("series"), new CompactSeriesStringifier(directory),
                   LabelCustomizers.fontSize(9))
        .addColumn(Lang.get("label"), descriptionService.getStringifier(Transaction.LABEL),
                   chain(LabelCustomizers.BOLD, autoTooltip()))
        .addColumn(Lang.get("amount"), descriptionService.getStringifier(Transaction.AMOUNT), LabelCustomizers.ALIGN_RIGHT);
    headerPainter = PicsouTableHeaderPainter.install(transactionTable, directory);

    Gui.setColumnSizes(transactionTable.getComponent(), COLUMN_SIZES);
    installDoubleClickHandler();

    this.filterSet = new FilterSet(this);
    CustomFilterMessagePanel filterMessagePanel = new CustomFilterMessagePanel(filterSet, repository, directory);
    builder.add("customFilterMessage", filterMessagePanel.getPanel());
    filterSet.addListener(new FilterSetListener() {
      public void filterUpdated(String name, boolean enabled) {
        if (name.equals(CustomFilterMessagePanel.CUSTOM) && !enabled) {
          transactionTable.clearSelection();
        }
      }
    });

    autoSelectNextCheckBox = new JCheckBox();
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

    BudgetAreaSelector selector = new BudgetAreaSelector(repository, directory);
    selector.registerComponents(builder);

    addSingleCategorySeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSingleCategorySeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);

    addMultiCategoriesSeriesChooser("envelopeSeriesChooser", BudgetArea.ENVELOPES, builder);
    addMultiCategoriesSeriesChooser("specialSeriesChooser", BudgetArea.SPECIAL, builder);
    addSingleCategorySeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);

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
        Set<Integer> months = new HashSet<Integer>();
        for (Glob transaction : currentTransactions) {
          months.add(transaction.get(Transaction.MONTH));
        }
        for (Pair<PicsouMatchers.SeriesFirstEndDateFilter, GlobRepeat> filter : seriesRepeat) {
          filter.getFirst().filterDates(months);
          filter.getSecond().setFilter(filter.getFirst());
        }
      }
    }, Transaction.TYPE);
  }

  private void initUpdateListener(GlobRepository repository) {
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        Set<Key> updated = changeSet.getUpdated(Transaction.SERIES);
        if ((updated.size() > 0) && (autoSelectNextCheckBox.isSelected())) {
          selectNext(updated);
        }
      }
    });
  }

  private void addSingleCategorySeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/singleCategorySeriesChooserPanel.splits",
                                                           repository, directory);

    panelBuilder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    NoSeriesMessage noSeriesMessage = new NoSeriesMessage(budgetArea, repository);
    panelBuilder.add("noSeriesMessage", noSeriesMessage.getComponent());

    JToggleButton invisibleToggle = new JToggleButton(name);
    panelBuilder.add("invisibleToggle", invisibleToggle);
    GlobRepeat repeat = panelBuilder.addRepeat("seriesRepeat",
                                               Series.TYPE,
                                               linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                                               new SingleCategorySeriesComponentFactory(invisibleToggle, repository, directory));
    seriesRepeat.add(
      new Pair<PicsouMatchers.SeriesFirstEndDateFilter, GlobRepeat>(
        PicsouMatchers.seriesDateFilter(budgetArea.getId(), true), repeat));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    builder.add(name, panelBuilder);
  }

  private void addMultiCategoriesSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder builder) {

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/multiCategoriesSeriesChooserPanel.splits",
                                                           repository, directory);

    panelBuilder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    NoSeriesMessage noSeriesMessage = new NoSeriesMessage(budgetArea, repository);
    panelBuilder.add("noSeriesMessage", noSeriesMessage.getComponent());

    final JToggleButton invisibleToggle = new JToggleButton("name");
    panelBuilder.add("invisibleToggle", invisibleToggle);
    GlobRepeat repeat = panelBuilder.addRepeat("seriesRepeat",
                                               Series.TYPE,
                                               linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                                               new MultiCategoriesSeriesComponentFactory(budgetArea, invisibleToggle,
                                                                                         repository, directory));
    seriesRepeat.add(new Pair<PicsouMatchers.SeriesFirstEndDateFilter, GlobRepeat>(
      PicsouMatchers.seriesDateFilter(budgetArea.getId(), true), repeat));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    builder.add(name, panelBuilder);
  }

  private void addOccasionalSeriesChooser(GlobsPanelBuilder builder) {
    JToggleButton invisibleOccasionalToggle = new JToggleButton("occasional");
    builder.add("invisibleOccasionalToggle", invisibleOccasionalToggle);
    builder.addRepeat("occasionalSeriesRepeat",
                      Category.TYPE,
                      new GlobMatcher() {
                        public boolean matches(Glob category, GlobRepository repository) {
                          return Category.isMaster(category) && !Category.isAll(category) && !Category.isNone(category);
                        }
                      },
                      new OccasionalCategoriesComponentFactory("occasionalSeries", "occasionalCategoryToggle",
                                                               BudgetArea.OCCASIONAL,
                                                               invisibleOccasionalToggle,
                                                               repository, directory));
    builder.add("editCategories", new EditCategoriesAction());
  }

  private Comparator<Glob> getTransactionComparator() {
    return new Comparator<Glob>() {
      public int compare(Glob transaction1, Glob transaction2) {
        int labelDiff = Utils.compare(transaction1.get(Transaction.LABEL),
                                      transaction2.get(Transaction.LABEL));
        if (labelDiff != 0) {
          return labelDiff;
        }
        return TransactionComparator.ASCENDING_SPLIT_AFTER.compare(transaction1, transaction2);
      }
    };
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

  public void show(GlobList transactions) {
    if (transactions.size() < 2) {
      filterSet.clear();
      updateTableFilter();
    }
    else {
      filterSet.replaceAllWith(CustomFilterMessagePanel.CUSTOM,
                               GlobMatchers.fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
      updateTableFilter();
    }
    transactionTable.select(transactions);
  }

  private class CreateSeriesAction extends AbstractAction {
    private final BudgetArea budgetArea;

    public CreateSeriesAction(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public void actionPerformed(ActionEvent e) {
      Key key = seriesEditionDialog.showNewSeries(currentTransactions, budgetArea);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        Integer category = seriesEditionDialog.getCurrentCategory();
        if (category == null) {
          category = series.get(Series.DEFAULT_CATEGORY);
        }
        repository.enterBulkDispatchingMode();
        try {
          for (Glob transaction : currentTransactions) {
            repository.update(transaction.getKey(),
                              FieldValue.value(Transaction.SERIES, series.get(Series.ID)),
                              FieldValue.value(Transaction.CATEGORY, category));
          }
        }
        finally {
          repository.completeBulkDispatchingMode();
        }
      }
    }
  }

  private class EditAllSeriesAction extends EditSeriesAction {
    private EditAllSeriesAction(BudgetArea budgetArea) {
      super(repository, directory, seriesEditionDialog, budgetArea);
    }

    public Integer getSelectedSeries() {
      if (currentTransactions.size() == 0) {
        return null;
      }
      Integer seriesId = currentTransactions.get(0).get(Transaction.SERIES);
      for (Glob transaction : currentTransactions) {
        if (!seriesId.equals(transaction.get(Transaction.SERIES))) {
          return null;
        }
      }
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (budgetArea.getId().equals(series.get(Series.BUDGET_AREA))) {
        return seriesId;
      }
      return null;
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
        filter,
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
      else if ((transaction != null) && Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
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
