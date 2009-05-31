package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.AccountFilteringCombo;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.DefaultTableCellPainter;
import org.designup.picsou.gui.components.filtering.FilterSet;
import org.designup.picsou.gui.components.filtering.Filterable;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.columns.*;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import static org.designup.picsou.model.Transaction.TYPE;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobLinkComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.and;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.Comparator;

public class TransactionView extends View implements Filterable, GlobSelectionListener {
  public static final int DATE_COLUMN_INDEX = 0;
  public static final int BANK_DATE_COLUMN_INDEX = 1;
  public static final int SERIES_COLUMN_INDEX = 2;
  public static final int SUBSERIES_COLUMN_INDEX = 3;
  public static final int LABEL_COLUMN_INDEX = 4;
  public static final int AMOUNT_COLUMN_INDEX = 5;
  public static final int NOTE_COLUMN_INDEX = 6;
  public static final int ACCOUNT_BALANCE_INDEX = 7;
  public static final int BALANCE_INDEX = 8;
  public static final int ACCOUNT_NAME_INDEX = 9;

  private static final int[] COLUMN_SIZES = {10, 10, 10, 10, 30, 9, 15, 10, 10, 15};

  private GlobTableView view;
  private AccountFilteringCombo accountFilteringCombo;
  private TransactionRendererColors rendererColors;
  private TransactionSelection transactionSelection;
  private GlobMatcher filter = GlobMatchers.ALL;
  private FilterSet filterSet;
  private PicsouTableHeaderPainter headerPainter;

  public TransactionView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    rendererColors = new TransactionRendererColors(directory);
    createTable();
    this.transactionSelection = transactionSelection;
    transactionSelection.addListener(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    updateFilter();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    addAccountCombo(builder);
    builder.add(view.getComponent());
  }

  public void setFilter(GlobMatcher matcher) {
    this.filter = matcher;
    updateFilter();
  }

  public void addTableListener(TableModelListener listener) {
    this.view.getComponent().getModel().addTableModelListener(listener);
  }

  private void updateFilter() {
    view.setFilter(and(transactionSelection.getCurrentMatcher(),
                       accountFilteringCombo.getCurrentAccountFilter(),
                       this.filter));
    headerPainter.setFiltered((this.filter != null) && (this.filter != GlobMatchers.ALL));
  }

  private void addAccountCombo(GlobsPanelBuilder builder) {
    accountFilteringCombo = new AccountFilteringCombo(repository, directory, new GlobComboView.GlobSelectionHandler() {
      public void processSelection(Glob glob) {
        updateFilter();
      }
    });
    builder.add("accountFilterCombo", accountFilteringCombo.getComponent());
  }

  private JTable createTable() {
    this.view = createGlobTableView(repository, descriptionService, directory, rendererColors);
    this.view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    headerPainter = PicsouTableHeaderPainter.install(view, directory);
    this.filterSet = new FilterSet(this);

    JTable table = view.getComponent();
    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          SERIES_COLUMN_INDEX));

    TransactionKeyListener.install(table, NOTE_COLUMN_INDEX, directory, repository, true);
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

    GlobTableView view = GlobTableView.init(TYPE, repository, comparator, directory);

    view.setDefaultBackgroundPainter(new DefaultTableCellPainter(directory));

    TransactionAmountColumn amountColumn =
      new TransactionAmountColumn(view, Transaction.AMOUNT, rendererColors, descriptionService, repository, directory);
    TransactionAmountColumn accountBalanceColumn =
      new TransactionAmountColumn(view, Transaction.ACCOUNT_POSITION, rendererColors, descriptionService, repository, directory);
    TransactionAmountColumn balanceColumn =
      new TransactionAmountColumn(view, Transaction.SUMMARY_POSITION, rendererColors, descriptionService, repository, directory);

    FontLocator fontLocator = directory.get(FontLocator.class);
    Font dateFont = fontLocator.get("transactionView.date");
    Font subSeriesFont = fontLocator.get("transactionView.category");

    view
      .addColumn(Lang.get("transactionView.date.user"),
                 new TransactionDateStringifier(comparator), LabelCustomizers.font(dateFont))
      .addColumn(Lang.get("transactionView.date.bank"),
                 new TransactionDateStringifier(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER,
                                                Transaction.BANK_MONTH,
                                                Transaction.BANK_DAY), LabelCustomizers.font(dateFont))
      .addColumn(new TransactionSeriesColumn(view, rendererColors, descriptionService, repository, directory))
      .addColumn(Lang.get("subSeries"),
                 descriptionService.getStringifier(Transaction.SUB_SERIES),
                 LabelCustomizers.font(subSeriesFont))
      .addColumn(Lang.get("label"),
                 descriptionService.getStringifier(Transaction.LABEL),
                 LabelCustomizers.chain(LabelCustomizers.BOLD, LabelCustomizers.autoTooltip()))
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

    return view;
  }

  public GlobTableView getView() {
    return view;
  }

  public FilterSet getFilterSet() {
    return filterSet;
  }
}
