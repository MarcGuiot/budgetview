package org.designup.picsou.gui.categorization;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.signpost.guides.CategorizationAreaSignpost;
import org.designup.picsou.gui.signpost.guides.CategorizationSelectionSignpost;
import org.designup.picsou.gui.signpost.guides.CategorizationCompletionSignpost;
import org.designup.picsou.gui.signpost.guides.FirstCategorizationDoneSignpost;
import org.designup.picsou.gui.signpost.Signpost;
import org.designup.picsou.gui.accounts.NewAccountAction;
import org.designup.picsou.gui.categorization.components.*;
import org.designup.picsou.gui.categorization.special.*;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.utils.SeriesCreationHandler;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.filtering.CustomFilterMessagePanel;
import org.designup.picsou.gui.components.filtering.FilterSet;
import org.designup.picsou.gui.components.filtering.FilterSetListener;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.description.SeriesDescriptionStringifier;
import org.designup.picsou.gui.description.SeriesNameComparator;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.columns.TransactionKeyListener;
import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
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
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.utils.ShowHideButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class CategorizationView extends View implements TableView, Filterable, ColorChangeListener {

  private Directory parentDirectory;

  private GlobList currentTransactions = GlobList.EMPTY;
  private GlobTableView transactionTable;
  private JComboBox filteringModeCombo;
  private FilteredRepeats seriesRepeat = new FilteredRepeats();
  private Color envelopeSeriesLabelForegroundColor;
  private Color envelopeSeriesLabelBackgroundColor;

  private static final int[] COLUMN_SIZES = {10, 12, 28, 10};
  private SeriesEditionDialog seriesEditionDialog;

  private Signpost signpost;

  private TransactionRendererColors colors;
  private PicsouTableHeaderPainter headerPainter;
  private FilterSet filterSet;
  private GlobMatcher filter = GlobMatchers.ALL;
  private GlobMatcher currentTableFilter;
  private Set<Key> modifiedTransactions = new HashSet<Key>();
  private CategorizationLevel categorizationLevel;

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

    this.signpost = new CategorizationCompletionSignpost(categorizationLevel, repository, parentDirectory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("categorizationView", createPanelBuilder());
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateTableFilter();
    headerPainter.setFiltered(matcher != GlobMatchers.ALL);
  }

  public Signpost getCompletionSignpost() {
    return signpost;
  }

  private GlobsPanelBuilder createPanelBuilder() {

    seriesEditionDialog = directory.get(SeriesEditionDialog.class);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationView.splits",
                                                      this.repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    CategorizationGaugePanel gauge = new CategorizationGaugePanel(categorizationLevel, repository, parentDirectory);
    builder.add("gaugePanel", gauge.getPanel());

    addFilteringModeCombo(builder);

    Comparator<Glob> transactionComparator = getTransactionComparator();
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    transactionTable =
      builder.addTable("transactionsToCategorize", Transaction.TYPE, transactionComparator)
        .setDefaultLabelCustomizer(new TransactionLabelCustomizer())
        .addColumn(Lang.get("date"), new TransactionDateStringifier(TransactionComparator.DESCENDING_SPLIT_AFTER),
                   fontSize(9))
        .addColumn(Lang.get("series"), new CompactSeriesStringifier(directory),
                   chain(new OnChangeLabelCustomizer(fontSize(9)), tooltip(SeriesDescriptionStringifier.transactionSeries(), repository)))
        .addColumn(Lang.get("label"), descriptionService.getStringifier(Transaction.LABEL),
                   chain(BOLD, autoTooltip()))
        .addColumn(Lang.get("amount"), descriptionService.getStringifier(Transaction.AMOUNT), ALIGN_RIGHT);

    headerPainter = PicsouTableHeaderPainter.install(transactionTable, directory);

    final JTable table = transactionTable.getComponent();
    TransactionKeyListener.install(table, -1, directory, repository, true);
    ApplicationColors.installSelectionColors(table, directory);
    Gui.setColumnSizes(table, COLUMN_SIZES);
    installDoubleClickHandler();
    registerBankFormatExporter(transactionTable);
    Signpost signpost = new CategorizationSelectionSignpost(repository, directory);
    signpost.attach(table);

    Signpost firstCategorization = new FirstCategorizationDoneSignpost(repository, directory);
    firstCategorization.attach(table);

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

    addSeriesChooser("incomeSeriesChooser", BudgetArea.INCOME, builder);
    addSeriesChooser("recurringSeriesChooser", BudgetArea.RECURRING, builder);
    addSeriesChooser("variableSeriesChooser", BudgetArea.VARIABLE, builder);
    addSeriesChooser("extrasSeriesChooser", BudgetArea.EXTRAS, builder);
    addSeriesChooser("savingsSeriesChooser", BudgetArea.SAVINGS, builder);
    addOtherSeriesChooser("otherSeriesChooser", builder);

    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory, this);
    transactionDetailsView.registerComponents(builder);

    initSelectionListener();
    updateTableFilter();
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Transaction.TYPE) &&
            changeSet.containsUpdates(Transaction.SERIES)) {
          modifiedTransactions = changeSet.getUpdated(Transaction.SERIES);
          table.repaint();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });

    JPanel budgetAreaSelectionPanel = new JPanel();
    builder.add("budgetAreaSelectionPanel", budgetAreaSelectionPanel);
    CategorizationAreaSignpost areaSignpost = new CategorizationAreaSignpost(repository, directory);
    areaSignpost.attach(budgetAreaSelectionPanel);

    return builder;
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
        if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.CATEGORIZATION_FILTERING_MODE)) {
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
        Set<Integer> months = currentTransactions.getValueSet(Transaction.BUDGET_MONTH);
        for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
          filter.getFirst().filterDates(months, currentTransactions);
          filter.getSecond().setFilter(filter.getFirst());
        }
        colors.setSplitGroupSourceId(getSplitGroupSourceId());
        transactionTable.getComponent().repaint();
        modifiedTransactions.clear();
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

    NoSeriesMessage noSeriesMessage = NoSeriesMessageFactory.create(budgetArea, repository, directory);
    builder.add("noSeriesMessage", noSeriesMessage.getComponent());

    JRadioButton invisibleRadio = new JRadioButton("invisibleButton");
    builder.add("invisibleToggle", invisibleRadio);

    Matchers.CategorizationFilter filter = Matchers.seriesCategorizationFilter(budgetArea.getId());
    SeriesChooserComponentFactory componentFactory = new SeriesChooserComponentFactory(budgetArea, invisibleRadio,
                                                                                       seriesEditionDialog,
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
    builder.add("editSeries", new EditAllSeriesAction(budgetArea));
    builder.add("additionalAction", getAdditionalAction(budgetArea));

    parentBuilder.add(name, builder);
    return componentFactory;
  }

  private Action getAdditionalAction(BudgetArea budgetArea) {
    if (BudgetArea.SAVINGS.equals(budgetArea)) {
      NewAccountAction action = new NewAccountAction(AccountType.SAVINGS, repository, directory);
      action.setAccountTypeEditable(false);
      return action;
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

  public void reset() {
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

      JPanel panel = categorizationPanel.loadPanel(repository, directory, seriesRepeat, seriesEditionDialog, handler);
      panel.setVisible(false);
      cellBuilder.add("specialCasePanel", panel);

      String label = Lang.get("categorization.specialCases." + categorizationPanel.getId());

      ImageLocator imageLocator = directory.get(ImageLocator.class);
      ImageIcon rightArrow = imageLocator.get("arrow_right.png");
      ImageIcon downArrow = imageLocator.get("arrow_down.png");

      final ShowHideButton showHide = new ShowHideButton(panel, label, label);
      showHide.setIcons(rightArrow, downArrow);
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
    updateFilteringMode(transactions, forceShowUncategorized);
    doShow(transactions);
  }

  public void showUncategorizedForSelectedMonths() {
    setFilteringMode(TransactionFilteringMode.UNCATEGORIZED_SELECTED_MONTHS);
    filterSet.clear();
    updateTableFilter();
    transactionTable.clearSelection();
  }

  private void updateFilteringMode(GlobList transactions, boolean forceShowUncategorized) {
    if (forceShowUncategorized) {
      setFilteringMode(TransactionFilteringMode.UNCATEGORIZED);
      return;
    }

    for (Glob transaction : transactions) {
      if (!currentTableFilter.matches(transaction, repository)) {
        setFilteringMode(TransactionFilteringMode.SELECTED_MONTHS);
        return;
      }
    }
  }

  private void doShow(GlobList transactions) {
    if (transactions.size() < 2) {
      filterSet.clear();
    }
    else {
      filterSet.replaceAllWith(CustomFilterMessagePanel.CUSTOM,
                               fieldIn(Transaction.ID, transactions.getValueSet(Transaction.ID)));
    }
    updateTableFilter();
    transactionTable.select(transactions);
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
      Key key = seriesEditionDialog.showNewSeries(currentTransactions,
                                                  selectionService.getSelection(Month.TYPE),
                                                  budgetArea,
                                                  forcedValues);
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
      for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : seriesRepeat) {
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

    currentTableFilter = and(filter,
                             isFalse(Transaction.PLANNED),
                             isFalse(Transaction.MIRROR),
                             isFalse(Transaction.CREATED_BY_SERIES),
                             getCurrentFilteringModeMatcher()
    );

    transactionTable.setFilter(currentTableFilter);
  }

  public void setFilteringMode(TransactionFilteringMode mode) {
    filteringModeCombo.setSelectedItem(mode);
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

  private class OnChangeLabelCustomizer implements LabelCustomizer {
    private Font font;
    private Font originalFont;

    private OnChangeLabelCustomizer(FontCustomizer fontCustomizer) {
      originalFont = fontCustomizer.getFont();
      font = originalFont.deriveFont(originalFont.getStyle() ^ Font.BOLD, 11);
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      if (modifiedTransactions.contains(glob.getKey())) {
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
}
