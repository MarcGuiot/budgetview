package org.designup.picsou.gui.transactions;

import org.designup.picsou.gui.TransactionSelection;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.PicsouTableHeaderCustomizer;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.columns.*;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Transaction;
import static org.designup.picsou.model.Transaction.TYPE;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import java.awt.*;

public class TransactionView extends View implements GlobSelectionListener {
  public static final int DATE_COLUMN_INDEX = 0;
  public static final int CATEGORY_COLUMN_INDEX = 1;
  public static final int LABEL_COLUMN_INDEX = 2;
  public static final int AMOUNT_COLUMN_INDEX = 3;
  public static final int NOTE_COLUMN_INDEX = 4;

  private static final int[] COLUMN_SIZES = {10, 16, 30, 9};

  private GlobTableView view;
  private TransactionRendererColors rendererColors;
  private TransactionSelection transactionSelection;
  private String searchFilter;
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
    builder.add(view.getComponent());
  }

  public void setSearchFilter(String filter) {
    this.searchFilter = filter;
    updateFilter();
  }

  public void addTableListener(TableModelListener listener) {
    this.view.getComponent().getModel().addTableModelListener(listener);
  }

  private void updateFilter() {
    view.setFilter(and(or(fieldContainsIgnoreCase(Transaction.LABEL, searchFilter),
                          fieldContainsIgnoreCase(Transaction.NOTE, searchFilter)),
                       transactionSelection.getCurrentMatcher()));
    headerPainter.setFiltered(Strings.isNotEmpty(searchFilter));
  }

  private JTable createTable() {
    view = createGlobTableView(repository, descriptionService, directory, rendererColors);
    view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    headerPainter = PicsouTableHeaderPainter.install(view, directory);

    JTable table = view.getComponent();

    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          CATEGORY_COLUMN_INDEX));

    TransactionKeyListener.install(table, NOTE_COLUMN_INDEX);
    Gui.installRolloverOnButtons(table, new int[]{CATEGORY_COLUMN_INDEX, AMOUNT_COLUMN_INDEX});
    table.setDragEnabled(false);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
    Gui.setColumnSizes(table, COLUMN_SIZES);

    return table;
  }

  private static GlobTableView createGlobTableView(GlobRepository repository,
                                                   DescriptionService descriptionService, Directory directory,
                                                   TransactionRendererColors rendererColors) {
    TransactionComparator comparator = TransactionComparator.DESCENDING;

    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);

    GlobTableView view = GlobTableView.init(TYPE, repository, comparator, directory);

    TransactionSeriesColumn seriesColumn =
      new TransactionSeriesColumn(view, rendererColors, descriptionService, repository, directory);

    TransactionAmountColumn amountColumn =
      new TransactionAmountColumn(view, rendererColors, descriptionService, repository, directory);

    FontLocator fontLocator = directory.get(FontLocator.class);
    Font dateFont = fontLocator.get("transactionView.date");

    view
      .addColumn(Lang.get("date"), new TransactionDateStringifier(comparator), LabelCustomizers.font(dateFont))
      .addColumn(Lang.get("category"), seriesColumn, seriesColumn,
                 seriesColumn.getComparator())
      .addColumn(Lang.get("label"),
                 descriptionService.getStringifier(Transaction.LABEL), LabelCustomizers.BOLD)
      .addColumn(Lang.get("amount"), amountColumn, amountStringifier.getComparator(repository));

    view.startColumn()
      .setName(Lang.get("note"))
      .setField(Transaction.NOTE)
      .setEditor(new TransactionNoteEditor(repository, directory));

    return view;
  }

  public GlobTableView getView() {
    return view;
  }
}
