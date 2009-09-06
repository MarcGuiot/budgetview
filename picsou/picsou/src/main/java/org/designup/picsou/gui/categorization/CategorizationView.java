package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.filtering.CustomFilterMessagePanel;
import org.designup.picsou.gui.components.filtering.FilterSet;
import org.designup.picsou.gui.components.filtering.FilterSetListener;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.columns.TransactionKeyListener;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.importer.utils.BankFormatExporter;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.autoTooltip;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Pair;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class CategorizationView extends View implements TableView, Filterable {
  private GlobList currentTransactions = GlobList.EMPTY;
  private GlobTableView transactionTable;
  private JComboBox filteringModeCombo;
  private java.util.List<Pair<PicsouMatchers.CategorizationFilter, GlobRepeat>> seriesRepeat =
    new ArrayList<Pair<PicsouMatchers.CategorizationFilter, GlobRepeat>>();

  private Directory parentDirectory;

  private static final int[] COLUMN_SIZES = {10, 12, 28, 10};
  private SeriesEditionDialog seriesEditionDialog;

  private TransactionRendererColors colors;
  private PicsouTableHeaderPainter headerPainter;
  private FilterSet filterSet;
  private GlobMatcher filter = GlobMatchers.ALL;

  public CategorizationView(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    this.parentDirectory = parentDirectory;
    parentDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
        updateTableFilter();
      }
    }, Month.TYPE);

    colors = new TransactionRendererColors(directory);
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
    builder.add("progressMessage", gauge.getProgressMessage());
    builder.add("hideProgressMessage", gauge.getHideProgressMessageAction());

    addFilteringModeCombo(builder);

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

    JTable table = transactionTable.getComponent();
    TransactionKeyListener.install(table, -1, directory, repository, true);
    PicsouColors.installSelectionColors(table, directory);
    Gui.setColumnSizes(table, COLUMN_SIZES);
    installDoubleClickHandler();
    registerBankFormatExporter(transactionTable);

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

    TransactionCreationPanel transactionCreation = new TransactionCreationPanel(repository, directory);
    transactionCreation.registerComponents(builder);

    BudgetAreaSelector selector = new BudgetAreaSelector(repository, directory);
    selector.registerComponents(builder);

    addSingleCategorySeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSingleCategorySeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSingleCategorySeriesChooser("envelopesSeriesChooser", BudgetArea.ENVELOPES, builder);
    addSingleCategorySeriesChooser("specialSeriesChooser", BudgetArea.SPECIAL, builder);
    addSingleCategorySeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);

    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory, this);
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();
    updateTableFilter();

    return builder;
  }

  private void registerBankFormatExporter(final GlobTableView transactionTable) {
    transactionTable.addKeyBinding(GuiUtils.ctrl(KeyEvent.VK_B), "ExportBankFormat", new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        try {
          String text = BankFormatExporter.export(transactionTable.getCurrentSelection());
          GuiUtils.copyTextToClipboard(text);
        }
        catch (IOException e) {
          Log.write("Bank format export failed", e);
        }
      }
    });
  }

  private void addFilteringModeCombo(GlobsPanelBuilder builder) {
    filteringModeCombo = builder.add("transactionFilterCombo", new JComboBox(TransactionFilteringMode.values())).getComponent();
    filteringModeCombo.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        TransactionFilteringMode mode = (TransactionFilteringMode)filteringModeCombo.getSelectedItem();
        repository.update(UserPreferences.KEY, UserPreferences.CATEGORIZATION_FILTERING_MODE, mode.getId());
        updateTableFilter();
      }
    });
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.CATEGORIZATION_FILTERING_MODE)){
          Glob preferences = repository.find(UserPreferences.KEY);
          Integer defaultFilteringModeId =
            preferences.get(UserPreferences.CATEGORIZATION_FILTERING_MODE);
          Object o = TransactionFilteringMode.get(defaultFilteringModeId);
          filteringModeCombo.setSelectedItem(o);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        Glob preferences = repository.find(UserPreferences.KEY);
        if (preferences == null) {
          return;
        }
        Integer defaultFilteringModeId =
          preferences.get(UserPreferences.CATEGORIZATION_FILTERING_MODE);
        Object o = TransactionFilteringMode.get(defaultFilteringModeId);
        filteringModeCombo.setSelectedItem(o);
      }
    });
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

  private void initSelectionListener() {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
        Set<Integer> months = new HashSet<Integer>();
        for (Glob transaction : currentTransactions) {
          months.add(transaction.get(Transaction.MONTH));
        }
        for (Pair<PicsouMatchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
          filter.getFirst().filterDates(months, currentTransactions);
          filter.getSecond().setFilter(filter.getFirst());
        }
        colors.setSplitGroupSourceId(getSplitGroupSourceId());
        transactionTable.getComponent().repaint();
      }
    }, Transaction.TYPE);
  }

  private Integer getSplitGroupSourceId() {
    if (currentTransactions.size() != 1) {
      return null;
    }
    Glob transaction = currentTransactions.getFirst();
    if (transaction == null) {
      return null;
    }
    if (Boolean.TRUE.equals(transaction.get(Transaction.SPLIT))) {
      return transaction.get(Transaction.ID);
    }
    return transaction.get(Transaction.SPLIT_SOURCE);
  }

  private void addSingleCategorySeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/singleChooserPanel.splits",
                                                           repository, directory);

    panelBuilder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    NoSeriesMessage noSeriesMessage = new NoSeriesMessage(budgetArea, repository);
    panelBuilder.add("noSeriesMessage", noSeriesMessage.getComponent());

    JRadioButton invisibleRadio = new JRadioButton(name);
    panelBuilder.add("invisibleToggle", invisibleRadio);
    GlobRepeat repeat = panelBuilder.addRepeat("seriesRepeat",
                                               Series.TYPE,
                                               linkedTo(budgetArea.getGlob(), Series.BUDGET_AREA),
                                               SeriesNameComparator.INSTANCE,
                                               new CategorizationSeriesComponentFactory(budgetArea, invisibleRadio,
                                                                                        seriesEditionDialog,
                                                                                        repository,
                                                                                        directory));
    seriesRepeat.add(
      new Pair<PicsouMatchers.CategorizationFilter, GlobRepeat>(
        PicsouMatchers.seriesFilter(budgetArea.getId()), repeat));
    panelBuilder.add("createSeries", new CreateSeriesAction(budgetArea));
    panelBuilder.add("editSeries", new EditAllSeriesAction(budgetArea));

    panelBuilder.add("openCategorizationTipsAction",
                     new HelpAction(Lang.get("categorization.openTips"), "categorizationTips", parentDirectory));

    parentBuilder.add(name, panelBuilder);
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

  public void show(GlobList transactions, boolean forceShowUncategorized) {
    updateFilteringMode(transactions, forceShowUncategorized);
    doShow(transactions);
  }

  private void updateFilteringMode(GlobList transactions, boolean forceShowUncategorized) {
    if (forceShowUncategorized) {
      setFilteringMode(TransactionFilteringMode.UNCATEGORIZED);
      return;
    }
    if (TransactionFilteringMode.UNCATEGORIZED.equals(getFilteringMode())
        && containsCategorizedTransactions(transactions)) {
      setFilteringMode(TransactionFilteringMode.SELECTED_MONTHS);
    }
  }

  private boolean containsCategorizedTransactions(GlobList transactions) {
    Set<Integer> seriesIds = transactions.getValueSet(Transaction.SERIES);
    seriesIds.remove(Series.UNCATEGORIZED_SERIES_ID);
    return !seriesIds.isEmpty();
  }

  private void doShow(GlobList transactions) {
    if (transactions.size() < 2) {
      filterSet.clear();
      updateTableFilter();
    }
    else {
      filterSet.replaceAllWith(CustomFilterMessagePanel.CUSTOM,
                               fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
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
      Key key = seriesEditionDialog.showNewSeries(currentTransactions,
                                                  selectionService.getSelection(Month.TYPE),
                                                  budgetArea);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        repository.startChangeSet();
        try {
          if (categorize(series)) {
            return;
          }
          Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
          if (mirrorSeries != null) {
            categorize(mirrorSeries);
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    }

    private boolean categorize(Glob series) {
      boolean noneMatch = false;
      for (Pair<PicsouMatchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
        noneMatch |= filter.getFirst().matches(series, repository);
      }
      if (!noneMatch) {
        return false;
      }
      Integer subSeriesId = seriesEditionDialog.getLastSelectedSubSeriesId();
      for (Glob transaction : currentTransactions) {
        repository.update(transaction.getKey(),
                          FieldValue.value(Transaction.SERIES, series.get(Series.ID)),
                          FieldValue.value(Transaction.SUB_SERIES, subSeriesId));
      }
      return true;
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
        fieldEquals(Transaction.MIRROR, false),
        fieldEquals(Transaction.CREATED_BY_SERIES, false),
        getCurrentFilteringModeMatcher()
      );

    transactionTable.setFilter(matcher);
  }

  public void setFilteringMode(TransactionFilteringMode mode) {
    filteringModeCombo.setSelectedItem(mode);
  }

  public TransactionFilteringMode getFilteringMode() {
    return (TransactionFilteringMode)filteringModeCombo.getSelectedItem();
  }

  private GlobMatcher getCurrentFilteringModeMatcher() {
    TransactionFilteringMode mode = (TransactionFilteringMode)filteringModeCombo.getSelectedItem();
    return mode.getMatcher(repository, selectionService);
  }

  private class TransactionLabelCustomizer implements LabelCustomizer {
    public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
      else if ((transaction != null) && Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
        label.setForeground(colors.getTransactionErrorTextColor());
      }
      else {
        label.setForeground(colors.getTransactionTextColor());
      }
      colors.setBackground(label, transaction, isSelected, row);
    }
  }

  public void addTableListener(TableModelListener listener) {
    this.transactionTable.getComponent().getModel().addTableModelListener(listener);
  }

  public GlobList getDisplayedGlobs() {
    return transactionTable.getGlobs();
  }

  private static class SeriesNameComparator implements Comparator<Glob> {
    static Comparator<Glob> INSTANCE = new SeriesNameComparator();

    public int compare(Glob o1, Glob o2) {
      return Utils.compareIgnoreCase(o1.get(Series.NAME), o2.get(Series.NAME));
    }
  }
}
