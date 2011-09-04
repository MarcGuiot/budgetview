package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.chart.TransactionAccountPositionsChartView;
import org.designup.picsou.gui.accounts.utils.AccountFilter;
import org.designup.picsou.gui.card.ImportPanel;
import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.components.DefaultTableCellPainter;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.components.filtering.components.FilterClearingPanel;
import org.designup.picsou.gui.components.filtering.components.TextFilterPanel;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.series.analysis.histobuilders.range.SelectionHistoChartRange;
import org.designup.picsou.gui.transactions.actions.TransactionTableActions;
import org.designup.picsou.gui.transactions.columns.*;
import org.designup.picsou.gui.transactions.search.TransactionFilterPanel;
import org.designup.picsou.gui.transactions.utils.LegendStringifier;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

import static org.designup.picsou.model.Transaction.TYPE;
import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionView extends View implements Filterable {
  public static final int DATE_COLUMN_INDEX = 0;
  public static final int BANK_DATE_COLUMN_INDEX = 1;
  public static final int SERIES_COLUMN_INDEX = 2;
  public static final int LABEL_COLUMN_INDEX = 3;
  public static final int AMOUNT_COLUMN_INDEX = 4;
  public static final int NOTE_COLUMN_INDEX = 5;
  public static final int ACCOUNT_BALANCE_INDEX = 6;
  public static final int BALANCE_INDEX = 7;
  public static final int ACCOUNT_NAME_INDEX = 8;

  public static final String SERIES_FILTER = "series";
  private static final int[] COLUMN_SIZES = {10, 10, 15, 40, 9, 15, 10, 10, 30};
  private static final GlobMatcher HIDE_PLANNED_MATCHER = not(isTrue(Transaction.PLANNED));

  private GlobTableView view;
  private TransactionRendererColors rendererColors;
  private TransactionSelection transactionSelection;
  private GlobMatcher showPlannedTransactionsMatcher = HIDE_PLANNED_MATCHER;
  private GlobMatcher filter = GlobMatchers.ALL;
  private FilterManager filterManager;
  private PicsouTableHeaderPainter headerPainter;
  private JCheckBox showPlannedTransactionsCheckbox;
  private TextFilterPanel search;

  public TransactionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    rendererColors = new TransactionRendererColors(directory);
    createTable();
    this.transactionSelection = new TransactionSelection(filterManager, repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/transactions/transactionView.splits",
                                                      repository, directory);

    AccountFilter.init(filterManager, repository, directory);
    addShowPlannedTransactionsCheckbox(builder);
    builder.add(view.getComponent());

    ImportPanel importPanel = new ImportPanel(repository, directory);
    importPanel.registerComponents(builder);

    builder.add("gotoCategorization", new GotoCardAction(Card.CATEGORIZATION, directory));

    FilterClearingPanel filterClearingPanel = new FilterClearingPanel(filterManager, repository, directory);
    builder.add("customFilterMessage", filterClearingPanel.getPanel());

    search = new TransactionFilterPanel(filterManager, repository, directory);
    builder.add("transactionSearch", search.getPanel());

    builder.addLabel("sum", Transaction.TYPE,
                     GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT, false, Transaction.AMOUNT))
      .setAutoHideIfEmpty(true);

    TransactionAccountPositionsChartView accountChart =
      new TransactionAccountPositionsChartView("accountChart",
                                               new SelectionHistoChartRange(repository, directory),
                                               repository, directory);
    accountChart.registerComponents(builder);

    GlobLabelView legend = builder.addLabel("accountChartLegend", Account.TYPE, new LegendStringifier(directory));
    selectionService.addListener(legend, Month.TYPE);

    parentBuilder.add("transactionView", builder);
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateFilter();
  }

  private void updateFilter() {
    GlobMatcher newFilter = and(showPlannedTransactionsMatcher, filter,
                                GlobMatchers.not(GlobMatchers.fieldEquals(Transaction.ACCOUNT, Account.EXTERNAL_ACCOUNT_ID)));
    view.setFilter(newFilter);
    headerPainter.setFiltered(filterManager.hasClearableFilters());
  }

  public void setPlannedTransactionsShown() {
    if (!showPlannedTransactionsCheckbox.isSelected()) {
      showPlannedTransactionsCheckbox.doClick();
    }
  }

  public void setAccountFilter(Key accountKey) {
    selectionService.select(repository.get(accountKey));
  }

  public void setSeriesFilter(Glob series) {
    Set<Integer> seriesSet = Collections.singleton(series.get(Series.ID));
    filterManager.set(SERIES_FILTER, Matchers.transactionsForSeries(seriesSet));
  }

  private void addShowPlannedTransactionsCheckbox(GlobsPanelBuilder builder) {
    showPlannedTransactionsCheckbox = builder.add("showPlannedTransactions", new JCheckBox()).getComponent();
    showPlannedTransactionsCheckbox.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateShowTransactionsMatcher();
        updateFilter();
      }
    });
    showPlannedTransactionsCheckbox.setSelected(false);
  }

  private void updateShowTransactionsMatcher() {
    if (showPlannedTransactionsCheckbox.isSelected()) {
      showPlannedTransactionsMatcher = GlobMatchers.ALL;
    }
    else {
      showPlannedTransactionsMatcher = HIDE_PLANNED_MATCHER;
    }
  }

  private JTable createTable() {
    this.view = createGlobTableView(repository, descriptionService, directory, rendererColors);
    this.view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    headerPainter = PicsouTableHeaderPainter.install(view, directory);
    this.filterManager = new FilterManager(this);

    TransactionTableActions actions = new TransactionTableActions(view.getCopyAction(Lang.get("copy")),
                                                                  repository, directory);
    view.setPopupFactory(actions);

    JTable table = view.getComponent();
    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          SERIES_COLUMN_INDEX));

    TransactionKeyListener.install(table, NOTE_COLUMN_INDEX).setDeleteEnabled(actions.getDelete());

    Gui.installRolloverOnButtons(table, SERIES_COLUMN_INDEX, AMOUNT_COLUMN_INDEX);
    table.setDragEnabled(false);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
    Gui.setColumnSizes(table, COLUMN_SIZES);

    return table;
  }

  private static GlobTableView createGlobTableView(GlobRepository repository,
                                                   final DescriptionService descriptionService, Directory directory,
                                                   TransactionRendererColors rendererColors) {
    TransactionComparator comparator = TransactionComparator.DESCENDING_SPLIT_AFTER;

    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);

    GlobTableView view = GlobTableView.init(TYPE, repository, comparator, directory)
      .setName("transactionsTable");

    view.setDefaultBackgroundPainter(new DefaultTableCellPainter(directory));

    TransactionAmountColumn amountColumn =
      new TransactionAmountColumn(view, Transaction.AMOUNT, rendererColors, descriptionService, repository, directory);
    TransactionAmountColumn accountBalanceColumn =
      new TransactionAmountColumn(view, Transaction.ACCOUNT_POSITION, rendererColors, descriptionService, repository, directory);
    TransactionAmountColumn balanceColumn =
      new TransactionAmountColumn(view, Transaction.SUMMARY_POSITION, rendererColors, descriptionService, repository, directory);

    FontLocator fontLocator = directory.get(FontLocator.class);
    Font dateFont = fontLocator.get("transactionView.date");

    view
      .addColumn(Lang.get("transactionView.date.user"),
                 new TransactionDateStringifier(comparator), LabelCustomizers.font(dateFont))
      .addColumn(Lang.get("transactionView.date.bank"),
                 new TransactionDateStringifier(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER,
                                                Transaction.BANK_MONTH,
                                                Transaction.BANK_DAY), LabelCustomizers.font(dateFont))
      .addColumn(new TransactionSeriesColumn(view, rendererColors, descriptionService, repository, directory))
      .addColumn(Lang.get("label"),
                 descriptionService.getStringifier(Transaction.LABEL),
                 LabelCustomizers.chain(LabelCustomizers.BOLD,
                                        new PlannedLabelCustomizer(rendererColors),
                                        LabelCustomizers.autoTooltip()))
      .addColumn(Lang.get("amount"),
                 amountColumn,
                 amountStringifier);

    view.startColumn()
      .setName(Lang.get("note"))
      .setField(Transaction.NOTE)
      .setEditor(new TransactionNoteEditor(repository, directory));
    view
      .addColumn(Lang.get("transactionView.account.position"),
                 accountBalanceColumn, accountBalanceColumn.getStringifier())
      .addColumn(Lang.get("transactionView.position"),
                 balanceColumn, balanceColumn.getStringifier())
      .addColumn(Lang.get("transactionView.account.name"),
                 descriptionService.getStringifier(Transaction.ACCOUNT));

    view.registerSaving(UserPreferences.KEY, new GlobTableView.FieldAccess() {
                          public IntegerField getPosField(int modelIndex) {
                            if (UserPreferences.TRANSACTION_POS1.getIndex() + modelIndex > UserPreferences.TRANSACTION_POS9.getIndex()) {
                              throw new RuntimeException("missing column in UserPreference");
                            }
                            return (IntegerField)UserPreferences.TYPE.getField(UserPreferences.TRANSACTION_POS1.getIndex() + modelIndex);
                          }
                        }, repository);
    return view;
  }

  public void reset() {
    view.resetSort();
    transactionSelection.init();
    selectionService.clear(Account.TYPE);
    showPlannedTransactionsCheckbox.setSelected(false);
    search.reset();
    updateShowTransactionsMatcher();
    setFilter(GlobMatchers.ALL);
  }

  private static class PlannedLabelCustomizer implements LabelCustomizer {
    private TransactionRendererColors rendererColors;

    public PlannedLabelCustomizer(TransactionRendererColors rendererColors) {
      this.rendererColors = rendererColors;
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      if (glob.isTrue(Transaction.PLANNED)) {
        if (isSelected) {
          label.setForeground(rendererColors.getTransactionSelectedTextColor());
        }
        else {
          label.setForeground(rendererColors.getTransactionPlannedTextColor());
        }
      }
      else {
        if (isSelected) {
          label.setForeground(rendererColors.getTransactionSelectedTextColor());
        }
        else {
          label.setForeground(rendererColors.getTransactionTextColor());
        }
      }
    }
  }

}
