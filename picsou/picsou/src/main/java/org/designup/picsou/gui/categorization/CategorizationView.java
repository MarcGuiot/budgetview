package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.CreateAccountAction;
import org.designup.picsou.gui.categorization.actions.CategorizationTableActions;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.categorization.reconciliation.ReconciliationWarningPanel;
import org.designup.picsou.gui.categorization.special.*;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.table.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.filtering.FilterClearer;
import org.designup.picsou.gui.components.filtering.FilterListener;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.components.filtering.components.FilterClearingPanel;
import org.designup.picsou.gui.description.stringifiers.SeriesDescriptionStringifier;
import org.designup.picsou.gui.description.stringifiers.SeriesNameComparator;
import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.printing.actions.PrintTransactionsAction;
import org.designup.picsou.gui.projects.actions.CreateProjectAction;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.signpost.guides.*;
import org.designup.picsou.gui.signpost.sections.SkipCategorizationPanel;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.columns.ReconciliationCustomizer;
import org.designup.picsou.gui.transactions.columns.TransactionKeyListener;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.transactions.creation.ShowCreateTransactionAction;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ReconciliationAnnotationColumn;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ShowReconciliationAction;
import org.designup.picsou.gui.transactions.search.TransactionFilterPanel;
import org.designup.picsou.gui.transactions.utils.TransactionLabelCustomizer;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.utils.TableView;
import org.designup.picsou.importer.utils.BankFormatExporter;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.DisabledAction;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.utils.ShowHideButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Pair;
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
import java.util.*;
import java.util.List;

import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class CategorizationView extends View implements TableView, Filterable, ColorChangeListener {

  private Directory parentDirectory;

  private GlobList currentTransactions = GlobList.EMPTY;
  private GlobTableView transactionTable;
  private JComboBox filteringModeCombo;
  private FilteredRepeats seriesRepeat = new FilteredRepeats();
  private Color envelopeSeriesLabelForegroundColor;
  private Color envelopeSeriesLabelBackgroundColor;

  public static final int[] COLUMN_SIZES = {10, 12, 28, 10};
  public static final int[] COLUMN_SIZES_WITH_RECONCILIATION = {5, 10, 12, 24, 10};
  public static final String TRANSACTIONS_FILTER = "transactions";

  private Signpost signpost;
  private TransactionRendererColors colors;
  private PicsouTableHeaderPainter headerPainter;
  private FilterManager filterManager;
  private GlobMatcher filter = GlobMatchers.ALL;
  private GlobMatcher currentTableFilter;
  private Set<Key> categorizedTransactions = new HashSet<Key>();
  private Set<Key> reconciledTransactions = new HashSet<Key>();
  private CategorizationLevel categorizationLevel;
  private ReconciliationAnnotationColumn column;
  private boolean isShowing = false;
  private TransactionCreationPanel transactionCreation;

  public CategorizationView(final GlobRepository repository, Directory parentDirectory) {
    super(repository, createLocalDirectory(parentDirectory));
    this.colorService.addListener(this);
    this.parentDirectory = parentDirectory;
    parentDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectionService.select(selection.getAll(Month.TYPE), Month.TYPE);
        updateTableFilter();
      }
    }, Month.TYPE);

    this.colors = new TransactionRendererColors(directory);

    categorizationLevel = new CategorizationLevel(repository, directory);

    this.signpost = new GotoBudgetSignpost(categorizationLevel, repository, parentDirectory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("categorizationView", createPanelBuilder());
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateTableFilter();
    headerPainter.setFiltered(matcher != GlobMatchers.ALL);
  }

  public Signpost getGotoBudgetSignpost() {
    return signpost;
  }

  private GlobsPanelBuilder createPanelBuilder() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationView.splits",
                                                      this.repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    CategorizationGaugePanel gauge = new CategorizationGaugePanel(categorizationLevel, repository, parentDirectory);
    builder.add("gaugePanel", gauge.getPanel());

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

    CategorizationTableActions actions = new CategorizationTableActions(transactionTable.getCopySelectionAction(Lang.get("copy")),
                                                                        repository, directory);
    transactionTable.setPopupFactory(actions);

    headerPainter = PicsouTableHeaderPainter.install(transactionTable, directory);

    transactionCreation = new TransactionCreationPanel(repository, directory, parentDirectory);
    transactionCreation.registerComponents(builder);

    JPopupMenu tableMenu = new JPopupMenu();
    tableMenu.add(transactionCreation.getShowHideAction());
    tableMenu.addSeparator();
    tableMenu.add(new ShowReconciliationAction(repository, directory));
    tableMenu.addSeparator();
    tableMenu.add(transactionTable.getCopyTableAction(Lang.get("copyTable")));
    tableMenu.add(new PrintTransactionsAction(transactionTable, repository, directory));
    builder.add("actionsMenu", new JPopupButton(Lang.get("budgetView.actions"), tableMenu));

    final JTable table = transactionTable.getComponent();
    TransactionKeyListener.install(table, -1).setDeleteEnabled(actions.getDelete());
    ApplicationColors.installSelectionColors(table, directory);
    Gui.setColumnSizes(table, COLUMN_SIZES);
    installDoubleClickHandler();
    registerBankFormatExporter(transactionTable);

    Signpost signpost = new CategorizationSelectionSignpost(repository, directory);
    signpost.attach(table);
    Signpost firstCategorization = new FirstCategorizationDoneSignpost(repository, directory);
    firstCategorization.attach(table);

    Signpost reconciliation = new ReconciliationSignpost(repository, directory);
    reconciliation.attach(table);
    installReconciliationAnnotationUpdater(transactionTable, repository);

    this.filterManager = new FilterManager(this);
    this.filterManager.addListener(new FilterListener() {
      public void filterUpdated(Collection<String> changedFilters) {
        transactionTable.clearSelection();
      }
    });
    this.filterManager.addClearer(new FilterClearer() {
      public List<String> getAssociatedFilters() {
        return Arrays.asList(TRANSACTIONS_FILTER);
      }

      public void clear() {
        filterManager.remove(TRANSACTIONS_FILTER);
      }
    });

    TransactionFilterPanel search = new TransactionFilterPanel(filterManager, repository, directory);
    builder.add("transactionSearch", search.getPanel());

    FilterClearingPanel filterClearingPanel = new FilterClearingPanel(filterManager, repository, directory);
    builder.add("customFilterMessage", filterClearingPanel.getPanel());

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

    SkipCategorizationPanel skipPanel = new SkipCategorizationPanel(repository, directory);
    builder.add("skipCategorizationPanel", skipPanel.getPanel());

    CategorizationSelector selector = new CategorizationSelector(new ToReconcileMatcher(), colors, repository, directory);
    selector.registerComponents(builder);

    addSeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSeriesChooser("variableSeriesChooser", BudgetArea.VARIABLE, builder);
    addSeriesChooser("extrasSeriesChooser", BudgetArea.EXTRAS, builder);
    addSeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);
    addOtherSeriesChooser("otherSeriesChooser", builder);

    TransactionDetailsView transactionDetailsView =
      new TransactionDetailsView(repository, directory, this, actions);
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();
    updateTableFilter();
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Transaction.TYPE) &&
            changeSet.containsUpdates(Transaction.SERIES)) {
          table.repaint();
        }
      }
    });

    repository.addChangeListener(new KeyChangeListener(UserPreferences.KEY) {
      protected void update() {
        transactionTable.setFilter(currentTableFilter);
      }
    });

    JPanel budgetAreaSelectionPanel = new JPanel();
    builder.add("budgetAreaSelectionPanel", budgetAreaSelectionPanel);
    CategorizationAreaSignpost areaSignpost = new CategorizationAreaSignpost(repository, directory);
    areaSignpost.attach(budgetAreaSelectionPanel);

    ReconciliationWarningPanel reconciliationWarningPanel =
      new ReconciliationWarningPanel(this, repository, directory);
    builder.add("reconciliationWarningPanel", reconciliationWarningPanel.getPanel());

    return builder;
  }

  public static GlobTableView createTransactionTable(String name,
                                                     GlobsPanelBuilder builder,
                                                     TransactionRendererColors colors,
                                                     Comparator<Glob> transactionComparator,
                                                     LabelCustomizer extraLabelCustomizer,
                                                     GlobRepository repository,
                                                     Directory directory) {

    DescriptionService descriptionService = directory.get(DescriptionService.class);

    return builder.addTable(name, Transaction.TYPE, transactionComparator)
      .setDefaultLabelCustomizer(new TransactionLabelCustomizer(colors))
      .setDefaultBackgroundPainter(colors.getBackgroundPainter())
      .addColumn(Lang.get("date"), new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER),
                 fontSize(9))
      .addColumn(Lang.get("series"), new CompactSeriesStringifier(directory),
                 chain(extraLabelCustomizer, tooltip(SeriesDescriptionStringifier.transactionSeries(), repository)))
      .addColumn(Lang.get("label"), descriptionService.getStringifier(Transaction.LABEL),
                 chain(BOLD, new ReconciliationCustomizer(directory), autoTooltip()))
      .addColumn(Lang.get("amount"), descriptionService.getStringifier(Transaction.AMOUNT), 
                 LabelCustomizers.chain(ALIGN_RIGHT, new TransactionAmountCustomizer(colors)));
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

  private void initSelectionListener() {
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
        Set<Integer> months = currentTransactions.getValueSet(Transaction.BUDGET_MONTH);
        for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
          filter.getFirst().filterDates(months, currentTransactions);
          filter.getSecond().setFilter(filter.getFirst());
        }
        colors.setSplitGroupSourceId(getSplitGroupSourceId());
        transactionTable.getComponent().repaint();
        categorizedTransactions.clear();
        reconciledTransactions.clear();
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
    if (transaction.isTrue(Transaction.SPLIT)) {
      return transaction.get(Transaction.ID);
    }
    return transaction.get(Transaction.SPLIT_SOURCE);
  }

  private SeriesChooserComponentFactory addSeriesChooser(String name, BudgetArea budgetArea, GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(CategorizationView.class,
                                                      "/layout/categorization/seriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(budgetArea.getHtmlDescription()));

    DynamicMessage noSeriesMessage = NoSeriesMessageFactory.create(budgetArea, repository, directory);
    builder.add("noSeriesMessage", noSeriesMessage.getComponent());

    DynamicMessage categorizationMessage = CategorizationMessageFactory.create(budgetArea, repository, directory);
    builder.add("categorizationMessage", categorizationMessage.getComponent());

    JRadioButton invisibleRadio = new JRadioButton("invisibleButton");
    builder.add("invisibleToggle", invisibleRadio);

    DescriptionPanelHandler descriptionHandler = new DescriptionPanelHandler(repository);
    builder.add("descriptionPanel", descriptionHandler.getPanel());
    builder.add("showDescription", descriptionHandler.getShowAction());
    builder.add("hideDescription", descriptionHandler.getHideAction());

    Matchers.CategorizationFilter filter = Matchers.seriesCategorizationFilter(budgetArea.getId());
    SeriesChooserComponentFactory componentFactory = new SeriesChooserComponentFactory(budgetArea, invisibleRadio,
                                                                                       repository,
                                                                                       directory);
    GlobRepeat repeat = builder.addRepeat("seriesRepeat",
                                          Series.TYPE,
                                          filter,
                                          SeriesNameComparator.INSTANCE,
                                          componentFactory);
    seriesRepeat.add(filter, repeat);

    JPanel groupForSeries = new JPanel();
    builder.add("groupCreateEditSeries", groupForSeries);
    builder.add("createSeries", new CreateSeriesAction(budgetArea));
    builder.add("additionalAction", getAdditionalAction(budgetArea));

    parentBuilder.add(name, builder);
    return componentFactory;
  }

  private Action getAdditionalAction(BudgetArea budgetArea) {
    if (BudgetArea.SAVINGS.equals(budgetArea)) {
      CreateAccountAction createAccountAction = new CreateAccountAction(AccountType.SAVINGS, repository, directory);
      createAccountAction.setAccountTypeEditable(false);
      return createAccountAction;
    }
    if (BudgetArea.EXTRAS.equals(budgetArea)) {
      return new CreateProjectAction(directory);
    }
    return new DisabledAction();
  }

  private void addOtherSeriesChooser(String name, GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(CategorizationView.class,
                                                      "/layout/categorization/otherSeriesChooserPanel.splits",
                                                      repository, directory);

    builder.add("description", GuiUtils.createReadOnlyHtmlComponent(BudgetArea.OTHER.getHtmlDescription()));

    java.util.List<SpecialCategorizationPanel> categorizationPanels = Arrays.asList(
      new DeferredCardCategorizationPanel(),
      new InternalTransfersCategorizationPanel(),
      new HtmlCategorizationPanel("healthReimbursements"),
      new HtmlCategorizationPanel("loans"),
      new HtmlCategorizationPanel("cash"),
      new HtmlCategorizationPanel("exceptionalIncome")
    );

    builder.addRepeat("specialCategorizationPanels",
                      categorizationPanels,
                      new SpecialCategorizationRepeatFactory());

    parentBuilder.add(name, builder);
  }

  public void highlightTransactionCreation() {
    transactionCreation.showTip();
  }

  public void reset() {
    filterManager.clear();
  }

  public void colorsChanged(ColorLocator colorLocator) {
    envelopeSeriesLabelForegroundColor = colorLocator.get("categorization.changed.envelope.fg");
    envelopeSeriesLabelBackgroundColor = colorLocator.get("categorization.changed.envelope.bg");
  }

  public class SpecialCategorizationRepeatFactory implements RepeatComponentFactory<SpecialCategorizationPanel> {
    public void registerComponents(RepeatCellBuilder cellBuilder, SpecialCategorizationPanel categorizationPanel) {

      JPanel blockPanel = new JPanel();
      blockPanel.setName(categorizationPanel.getId());
      cellBuilder.add("specialCaseBlock", blockPanel);

      SeriesCreationHandler handler = new SeriesCreationHandler() {
        public void createSeries(BudgetArea budgetArea, FieldValue... forcedValues) {
          CreateSeriesAction action = new CreateSeriesAction(budgetArea, forcedValues);
          action.actionPerformed(null);
        }
      };

      JPanel panel = categorizationPanel.loadPanel(repository, directory, seriesRepeat, handler);
      panel.setVisible(false);
      cellBuilder.add("specialCasePanel", panel);

      String label = Lang.get("categorization.specialCases." + categorizationPanel.getId());

      ShowHideButton showHide = new ShowHideButton(panel, label, label);
      categorizationPanel.registerController(new ShowHidePanelController(showHide));
      cellBuilder.add("showHide", showHide);
    }
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
    categorizedTransactions.clear();
    reconciledTransactions.clear();
    updateFilteringMode(transactions, forceShowUncategorized);

    if (transactions.size() < 2) {
      filterManager.reset();
    }
    else {
      filterManager.clear();
      filterManager.replaceAllWith(TRANSACTIONS_FILTER,
                                   fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
    }
    updateTableFilter();
    transactionTable.select(transactions);
  }


  public void showUncategorizedForSelectedMonths() {
    showWithMode(CategorizationFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
  }

  public void showWithMode(CategorizationFilteringMode filteringMode) {
    setFilteringMode(filteringMode);
    filterManager.reset();
    updateTableFilter();
    transactionTable.clearSelection();
  }

  private void updateFilteringMode(GlobList transactions, boolean forceShowUncategorized) {
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

  private class CreateSeriesAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private FieldValue[] forcedValues;

    public CreateSeriesAction(BudgetArea budgetArea, FieldValue... forcedValues) {
      super(Lang.get("categorization.series.add"));
      this.budgetArea = budgetArea;
      this.forcedValues = forcedValues;
    }

    public void actionPerformed(ActionEvent e) {
      Key key = SeriesEditor.get(directory).showNewSeries(currentTransactions,
                                                          selectionService.getSelection(Month.TYPE),
                                                          budgetArea,
                                                          forcedValues);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        try {
          repository.startChangeSet();
          for (Glob transaction : currentTransactions) {
            if (!categorize(series, transaction)) {
              Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
              if (mirrorSeries != null) {
                categorize(mirrorSeries, transaction);
              }
            }
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    }

    private boolean categorize(Glob series, final Glob transaction) {
      boolean noneMatch = false;
      for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
        filter.getFirst().filterDates(Collections.singleton(transaction.get(Transaction.BUDGET_MONTH)),
                                      Collections.singletonList(transaction));
        filter.getSecond().setFilter(filter.getFirst());
        noneMatch |= filter.getFirst().matches(series, repository);
      }
      if (!noneMatch) {
        return false;
      }
      Integer subSeriesId = SeriesEditor.get(directory).getLastSelectedSubSeriesId();
      repository.update(transaction.getKey(),
                        value(Transaction.SERIES, series.get(Series.ID)),
                        value(Transaction.SUB_SERIES, subSeriesId),
                        value(Transaction.RECONCILIATION_ANNOTATION_SET, !Transaction.isManuallyCreated(transaction)));
      return true;
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

  private class OnChangeLabelCustomizer implements LabelCustomizer {
    private Font font;
    private Font originalFont;

    private OnChangeLabelCustomizer(FontCustomizer fontCustomizer) {
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

  private void installReconciliationAnnotationUpdater(final GlobTableView tableView, GlobRepository repository) {
    this.column = new ReconciliationAnnotationColumn(tableView, repository, directory);
    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE) {
      protected void update(GlobRepository repository) {
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
    if (show && !isShowing) {
      isShowing = true;
      tableView.insertColumn(0, column);
      Gui.setColumnSizes(tableView.getComponent(), COLUMN_SIZES_WITH_RECONCILIATION);
      filteringModeCombo.setModel(new DefaultComboBoxModel(CategorizationFilteringMode.getValues(true)));
      setFilteringMode(CategorizationFilteringMode.ALL);
    }
    else if (!show && isShowing) {
      isShowing = false;
      tableView.removeColumn(0);
      Gui.setColumnSizes(tableView.getComponent(), CategorizationView.COLUMN_SIZES);
      filteringModeCombo.setModel(new DefaultComboBoxModel(CategorizationFilteringMode.getValues(false)));
      setFilteringMode(CategorizationFilteringMode.ALL);
    }
  }

  private class ToReconcileMatcher implements GlobMatcher {
    public boolean matches(Glob transaction, GlobRepository repository) {
      return Transaction.isToReconcile(transaction) && !categorizedTransactions.contains(transaction.getKey());
    }
  }
}
