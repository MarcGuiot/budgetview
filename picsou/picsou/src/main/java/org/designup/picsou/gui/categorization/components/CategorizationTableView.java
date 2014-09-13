package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.actions.CategorizationTableActions;
import org.designup.picsou.gui.components.filtering.FilterClearer;
import org.designup.picsou.gui.components.filtering.FilterListener;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.components.filtering.components.FilterMessagePanel;
import org.designup.picsou.gui.components.table.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.stringifiers.SeriesDescriptionStringifier;
import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.gui.signpost.PersistentSignpost;
import org.designup.picsou.gui.signpost.guides.CategorizationSelectionSignpost;
import org.designup.picsou.gui.signpost.guides.FirstCategorizationDoneSignpost;
import org.designup.picsou.gui.signpost.guides.ReconciliationSignpost;
import org.designup.picsou.gui.transactions.columns.ReconciliationCustomizer;
import org.designup.picsou.gui.transactions.columns.SplitTransactionCustomizer;
import org.designup.picsou.gui.transactions.columns.TransactionKeyListener;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ReconciliationAnnotationColumn;
import org.designup.picsou.gui.transactions.search.TransactionFilterPanel;
import org.designup.picsou.gui.transactions.utils.TransactionLabelCustomizer;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.importer.utils.BankFormatExporter;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.*;

import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import static org.globsframework.model.utils.GlobMatchers.*;

public class CategorizationTableView extends View implements TableView, Filterable, ColorChangeListener {

  private GlobTableView transactionTable;
  private JComboBox filteringModeCombo;

  public static final int[] COLUMN_SIZES = {10, 12, 28, 10};
  public static final int[] COLUMN_SIZES_WITH_RECONCILIATION = {5, 10, 12, 24, 10};
  public static final String TRANSACTIONS_FILTER = "transactions";

  private GlobMatcher filter = GlobMatchers.ALL;
  private GlobMatcher currentTableFilter;
  private FilterManager filterManager;

  private ReconciliationAnnotationColumn column;
  private boolean isReconciliationColumnShown = false;

  private Color envelopeSeriesLabelForegroundColor;
  private Color envelopeSeriesLabelBackgroundColor;

  private TransactionRendererColors colors;
  private PicsouTableHeaderPainter headerPainter;
  private CategorizationTableActions actions;

  private GlobList currentTransactions;
  private Set<Key> categorizedTransactions = new HashSet<Key>();
  private Set<Key> reconciledTransactions = new HashSet<Key>();

  public CategorizationTableView(GlobList currentTransactions, GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.currentTransactions = currentTransactions;
    this.colors = new TransactionRendererColors(directory);
    this.colorService.addListener(this);
  }

  public static GlobTableView createTransactionTable(String name,
                                                     GlobsPanelBuilder builder,
                                                     TransactionRendererColors colors,
                                                     Comparator<Glob> transactionComparator,
                                                     LabelCustomizer extraSeriesLabelCustomizer,
                                                     GlobRepository repository,
                                                     Directory directory) {

    DescriptionService descriptionService = directory.get(DescriptionService.class);

    return builder.addTable(name, Transaction.TYPE, transactionComparator)
      .setDefaultLabelCustomizer(new TransactionLabelCustomizer(colors))
      .setDefaultBackgroundPainter(colors.getBackgroundPainter())
      .addColumn(Lang.get("date"), new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER),
                 fontSize(9))
      .addColumn(Lang.get("series"), new CompactSeriesStringifier(directory),
                 chain(extraSeriesLabelCustomizer, tooltip(SeriesDescriptionStringifier.transactionSeries(), repository)))
      .addColumn(Lang.get("label"),
                 new CategorizationTransationStringifier(descriptionService.getStringifier(Transaction.LABEL)),
                 chain(BOLD,
                       new SplitTransactionCustomizer(directory),
                       new ReconciliationCustomizer(directory),
                       autoTooltip()))
      .addColumn(Lang.get("amount"), descriptionService.getStringifier(Transaction.AMOUNT),
                 LabelCustomizers.chain(ALIGN_RIGHT, new TransactionAmountCustomizer(colors)));
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    addFilteringModeCombo(builder);

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Transaction.TYPE) &&
            (changeSet.containsUpdates(Transaction.SERIES) ||
             changeSet.containsUpdates(Transaction.RECONCILIATION_ANNOTATION_SET))) {
          categorizedTransactions.clear();
          categorizedTransactions.addAll(changeSet.getUpdated(Transaction.SERIES));
          reconciledTransactions.clear();
          reconciledTransactions.addAll(changeSet.getUpdated(Transaction.RECONCILIATION_ANNOTATION_SET));
          updateTableFilter();
        }
      }
    });

    Comparator<Glob> transactionComparator = getTransactionComparator();
    transactionTable =
      createTransactionTable("transactionsToCategorize", builder, colors,
                             transactionComparator,
                             new OnChangeLabelCustomizer(fontSize(9)),
                             repository, directory);
    headerPainter = PicsouTableHeaderPainter.install(transactionTable, directory);
    actions = new CategorizationTableActions(transactionTable.getCopySelectionAction(Lang.get("copy")),
                                             repository, directory);
    transactionTable.setPopupFactory(actions);

    final JTable table = transactionTable.getComponent();
    TransactionKeyListener.install(table, -1).setDeleteEnabled(actions.getDelete());
    ApplicationColors.installSelectionColors(table, directory);
    Gui.setColumnSizes(table, COLUMN_SIZES);
    installDoubleClickHandler();
    registerBankFormatExporter(transactionTable);

    PersistentSignpost signpost = new CategorizationSelectionSignpost(repository, directory);
    signpost.attach(table);
    PersistentSignpost firstCategorization = new FirstCategorizationDoneSignpost(repository, directory);
    firstCategorization.attach(table);

    PersistentSignpost reconciliation = new ReconciliationSignpost(repository, directory);
    reconciliation.attach(table);
    installReconciliationAnnotationUpdater(transactionTable, repository);

    this.filterManager = new FilterManager(this);
    this.filterManager.addListener(new FilterListener() {
      public void filterUpdated(Collection<String> changedFilters) {
        transactionTable.clearSelection();
      }
    });
    this.filterManager.addClearer(new FilterClearer() {
      public java.util.List<String> getAssociatedFilters() {
        return Arrays.asList(TRANSACTIONS_FILTER);
      }

      public void clear() {
        filterManager.remove(TRANSACTIONS_FILTER);
      }
    });

    TransactionFilterPanel search = new TransactionFilterPanel(filterManager, repository, directory);
    builder.add("transactionSearch", search.getPanel());

    FilterMessagePanel filterClearingPanel = new FilterMessagePanel(filterManager, repository, directory);
    builder.add("customFilterMessage", filterClearingPanel.getPanel());

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Transaction.TYPE) &&
            changeSet.containsUpdates(Transaction.SERIES)) {
          table.repaint();
        }
      }
    });

    repository.addChangeListener(new KeyChangeListener(UserPreferences.KEY) {
      public void update() {
        transactionTable.setFilter(currentTableFilter);
      }
    });

  }

  public CategorizationTableActions getActions() {
    return actions;
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateTableFilter();
    headerPainter.setFiltered(matcher != GlobMatchers.ALL);
  }

  private void installReconciliationAnnotationUpdater(final GlobTableView tableView, GlobRepository repository) {
    this.column = new ReconciliationAnnotationColumn(tableView, repository, directory);
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE) {
      public void update(GlobRepository repository) {
        toggleReconciliationColumn(tableView);
      }
    });
    toggleReconciliationColumn(tableView);
  }

  private void toggleReconciliationColumn(GlobTableView tableView) {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return;
    }
    boolean show = preferences.isTrue(UserPreferences.SHOW_RECONCILIATION);
    if (show && !isReconciliationColumnShown) {
      isReconciliationColumnShown = true;
      tableView.insertColumn(0, column);
      Gui.setColumnSizes(tableView.getComponent(), COLUMN_SIZES_WITH_RECONCILIATION);
      filteringModeCombo.setModel(new DefaultComboBoxModel(CategorizationFilteringMode.getValues(true)));
      setFilteringMode(CategorizationFilteringMode.ALL);
    }
    else if (!show && isReconciliationColumnShown) {
      isReconciliationColumnShown = false;
      tableView.removeColumn(0);
      Gui.setColumnSizes(tableView.getComponent(), COLUMN_SIZES);
      filteringModeCombo.setModel(new DefaultComboBoxModel(CategorizationFilteringMode.getValues(false)));
      setFilteringMode(CategorizationFilteringMode.ALL);
    }
  }

  public void reset() {
    filterManager.removeAll();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    envelopeSeriesLabelForegroundColor = colorLocator.get("categorization.changed.envelope.fg");
    envelopeSeriesLabelBackgroundColor = colorLocator.get("categorization.changed.envelope.bg");
  }

  public GlobTableView getTable() {
    return transactionTable;
  }

  public void updateSelection() {
    colors.setSplitGroupSourceId(getSplitGroupSourceId());
    transactionTable.getComponent().repaint();
    categorizedTransactions.clear();
    reconciledTransactions.clear();
  }

  private Integer getSplitGroupSourceId() {
    if (currentTransactions.size() != 1) {
      return null;
    }
    Glob transaction = currentTransactions.getFirst();
    if (transaction == null) {
      return null;
    }
    if (transaction.isTrue(Transaction.SPLIT)) {
      return transaction.get(Transaction.ID);
    }
    return transaction.get(Transaction.SPLIT_SOURCE);
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

  public void updateTableFilter() {
    if (transactionTable == null) {
      return;
    }

    currentTableFilter = and(filter,
                             not(fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.OPEN_ACCOUNT_EVENT.getId())),
                             not(fieldEquals(Transaction.TRANSACTION_TYPE, TransactionType.CLOSE_ACCOUNT_EVENT.getId())),
                             isFalse(Transaction.PLANNED),
                             isFalse(Transaction.MIRROR),
                             isFalse(Transaction.CREATED_BY_SERIES),
                             getCurrentFilteringModeMatcher()
    );

    transactionTable.setFilter(currentTableFilter);
  }

  public void setFilteringMode(CategorizationFilteringMode mode) {
    filteringModeCombo.setSelectedItem(mode);
  }

  private GlobMatcher getCurrentFilteringModeMatcher() {
    CategorizationFilteringMode mode = (CategorizationFilteringMode)filteringModeCombo.getSelectedItem();
    return mode.getMatcher(repository, selectionService, categorizedTransactions, reconciledTransactions);
  }

  public void addTableListener(TableModelListener listener) {
    this.transactionTable.getComponent().getModel().addTableModelListener(listener);
  }

  public GlobList getDisplayedGlobs() {
    return transactionTable.getGlobs();
  }

  public TransactionRendererColors getColors() {
    return colors;
  }

  public void show(GlobList transactions, boolean forceShowUncategorized) {
    categorizedTransactions.clear();
    reconciledTransactions.clear();
    updateFilteringMode(transactions, forceShowUncategorized);

    if (transactions.size() < 2) {
      filterManager.reset();
    }
    else {
      filterManager.removeAll();
      filterManager.replaceAllWith(TRANSACTIONS_FILTER,
                                   Lang.get("filter.transaction.several", transactions.size()),
                                   fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
    }
    updateTableFilter();
    transactionTable.select(transactions);
  }

  public GlobMatcher getToReconcileMatcher() {
    return new ToReconcileMatcher();
  }

  private class OnChangeLabelCustomizer implements LabelCustomizer {
    private Font font;
    private Font originalFont;

    private OnChangeLabelCustomizer(LabelCustomizers.FontCustomizer fontCustomizer) {
      originalFont = fontCustomizer.getFont();
      font = originalFont.deriveFont(originalFont.getStyle() ^ Font.BOLD, 11);
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      if (categorizedTransactions.contains(glob.getKey())) {
        label.setForeground(envelopeSeriesLabelForegroundColor);
        label.setFont(font);
        if (isSelected) {
          label.setBackground(envelopeSeriesLabelBackgroundColor);
        }
      }
      else {
        label.setFont(originalFont);
      }
    }
  }

  private void addFilteringModeCombo(GlobsPanelBuilder builder) {
    filteringModeCombo = builder.add("transactionFilterCombo",
                                     new JComboBox(CategorizationFilteringMode.getValues(false))).getComponent();
    filteringModeCombo.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        CategorizationFilteringMode mode = (CategorizationFilteringMode)filteringModeCombo.getSelectedItem();
        categorizedTransactions.clear();
        reconciledTransactions.clear();
        repository.update(UserPreferences.KEY, UserPreferences.CATEGORIZATION_FILTERING_MODE, mode.getId());
        updateTableFilter();
      }
    });
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.CATEGORIZATION_FILTERING_MODE)) {
          updateFilteringCombo(repository);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateFilteringCombo(repository);
      }
    });
    updateFilteringCombo(repository);
  }

  private void updateFilteringCombo(GlobRepository repository) {
    Glob preferences = repository.find(UserPreferences.KEY);
    if (preferences == null) {
      return;
    }
    Integer defaultFilteringModeId =
      preferences.get(UserPreferences.CATEGORIZATION_FILTERING_MODE);
    Object mode = CategorizationFilteringMode.get(defaultFilteringModeId);
    filteringModeCombo.setSelectedItem(mode);
  }

  private void installDoubleClickHandler() {
    transactionTable.getComponent().addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2 && !e.isPopupTrigger()) {
          autoSelectSimilarTransactions();
        }
      }
    });
  }

  public void updateFilteringMode(GlobList transactions, boolean forceShowUncategorized) {
    if (forceShowUncategorized) {
      setFilteringMode(CategorizationFilteringMode.UNCATEGORIZED);
      return;
    }

    for (Glob transaction : transactions) {
      if (!currentTableFilter.matches(transaction, repository)) {
        setFilteringMode(CategorizationFilteringMode.SELECTED_MONTHS);
        return;
      }
    }
  }

  private void registerBankFormatExporter(final GlobTableView transactionTable) {
    transactionTable.addKeyBinding(GuiUtils.ctrl(KeyEvent.VK_B), "ExportBankFormat", new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        try {
          String text = BankFormatExporter.export(repository, transactionTable.getCurrentSelection());
          GuiUtils.copyTextToClipboard(text);
        }
        catch (IOException e) {
          Log.write("Bank format export failed", e);
        }
      }
    });
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

  public void showWithMode(CategorizationFilteringMode filteringMode) {
    setFilteringMode(filteringMode);
    filterManager.reset();
    updateTableFilter();
    transactionTable.clearSelection();
  }

  private static class CategorizationTransationStringifier implements GlobStringifier {
    private GlobStringifier stringifier;

    public CategorizationTransationStringifier(GlobStringifier transactionStringifier) {
      this.stringifier = transactionStringifier;
    }

    public String toString(Glob transaction, GlobRepository repository) {
      if (Transaction.isSplitTransaction(transaction) && Strings.isNotEmpty(transaction.get(Transaction.NOTE))) {
        return stringifier.toString(transaction, repository) + " - " + transaction.get(Transaction.NOTE);
      }
      else {
        return stringifier.toString(transaction, repository);
      }
    }

    public Comparator<Glob> getComparator(GlobRepository repository) {
      return stringifier.getComparator(repository);
    }
  }

  private class ToReconcileMatcher implements GlobMatcher {
    public boolean matches(Glob transaction, GlobRepository repository) {
      return Transaction.isToReconcile(transaction) && !categorizedTransactions.contains(transaction.getKey());
    }
  }
}
