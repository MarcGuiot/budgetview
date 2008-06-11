package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.utils.TableUtils;
import org.crossbowlabs.globs.gui.views.CellPainter;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.gui.views.utils.LabelCustomizers;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobBuilder;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouSamples;
import org.designup.picsou.model.*;
import static org.designup.picsou.model.Transaction.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;

import javax.swing.*;
import java.awt.*;

public class TransactionView extends View implements GlobSelectionListener, ChangeSetListener {
  public static final int DATE_COLUMN_INDEX = 0;
  public static final int CATEGORY_COLUMN_INDEX = 1;
  public static final int AMOUNT_COLUMN_INDEX = 3;
  public static final int NOTE_COLUMN_INDEX = 4;

  private GlobTableView view;

  private TransactionRendererColors rendererColors;
  private CategoryChooserAction categoryChooserAction;
  private TransactionSelection transactionSelection;

  public TransactionView(GlobRepository repository, Directory directory, TransactionSelection transactionSelection) {
    super(repository, directory);
    rendererColors = new TransactionRendererColors(directory);
    categoryChooserAction = new CategoryChooserAction(rendererColors, repository, directory);
    createTable();
    this.transactionSelection = transactionSelection;
    directory.get(ColorService.class).addListener(this);
    transactionSelection.addListener(this);
    repository.addChangeListener(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    setFilter(transactionSelection.getCurrentMatcher());
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(TransactionToCategory.TYPE)) {
      view.refresh();
    }
  }

  public void globsReset(GlobRepository repository, java.util.List<GlobType> changedTypes) {
    view.reset();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(view.getComponent());
  }

  public void setFilter(GlobMatcher matcher) {
    view.setFilter(matcher);
  }

  private JTable createTable() {
    view = createGlobTableView(categoryChooserAction, repository, descriptionService, directory, rendererColors);
    view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    TransactionViewUtils.configureHeader(view, directory);

    JTable table = view.getComponent();

    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          CATEGORY_COLUMN_INDEX));

    TransactionViewUtils.installKeyboardCategorization(table, categoryChooserAction, NOTE_COLUMN_INDEX);
    Gui.installRolloverOnButtons(table, new int[]{CATEGORY_COLUMN_INDEX, AMOUNT_COLUMN_INDEX});
    table.setDragEnabled(false);
    table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
    setInitialColumnSizes(table);

    return table;
  }

  private static GlobTableView createGlobTableView(CategoryChooserAction categoryChooserAction, GlobRepository repository,
                                                   DescriptionService descriptionService, Directory directory,
                                                   TransactionRendererColors rendererColors) {
    TransactionComparator comparator = TransactionComparator.DESCENDING;

    GlobStringifier categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);

    GlobTableView view = GlobTableView.init(TYPE, repository, comparator, directory);

    TransactionCategoryColumn categoryColumn =
      new TransactionCategoryColumn(categoryChooserAction, view, rendererColors, descriptionService, repository, directory);

    TransactionAmountColumn amountColumn =
      new TransactionAmountColumn(view, rendererColors, descriptionService, repository, directory);

    return view
      .addColumn(Lang.get("date"), new TransactionDateStringifier(comparator))
      .addColumn(descriptionService.getLabel(Category.TYPE), categoryColumn, categoryColumn,
                 new TransactionCategoriesStringifier(categoryStringifier).getComparator(repository))
      .addColumn(LABEL, LabelCustomizers.bold(), CellPainter.NULL)
      .addColumn(Lang.get("amount"), amountColumn, amountColumn, amountStringifier.getComparator(repository))
      .addColumn(NOTE, new TransactionNoteEditor(repository, directory));
  }

  private void setInitialColumnSizes(JTable targetTable) {
    Glob sampleTransaction =
      GlobBuilder.init(TYPE)
        .set(ID, 111)
        .set(MONTH, 200612)
        .set(DAY, 30)
        .set(TRANSACTION_TYPE, TransactionType.CHECK.getId())
        .set(CATEGORY, MasterCategory.FOOD.getId())
        .set(LABEL, PicsouSamples.LABEL_SAMPLE)
        .set(NOTE, PicsouSamples.NOTE_SAMPLE)
        .set(AMOUNT, new Double(PicsouSamples.AMOUNT_SAMPLE))
        .set(SPLIT, Boolean.TRUE)
        .get();

    LocalGlobRepository tempRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(sampleTransaction)
      .copy(TransactionType.TYPE, Category.TYPE)
      .get();

    tempRepository.update(sampleTransaction.getKey(), TRANSACTION_TYPE, getLargestType(tempRepository));

    GlobTableView tempTableView = createGlobTableView(categoryChooserAction, tempRepository,
                                                      descriptionService, directory, rendererColors);
    JTable tempTable = tempTableView.getComponent();

    for (int column = 0; column < tempTable.getColumnCount() - 1; column++) {
      Component component = TableUtils.getRenderedComponentAt(tempTable, 0, column);
      TableUtils.setSize(targetTable, column, TableUtils.getPreferredWidth(component));
    }

    tempTableView.dispose();
  }

  private Integer getLargestType(GlobRepository repository) {
    GlobStringifier globStringifier = descriptionService.getStringifier(TransactionType.TYPE);

    int largestString = 0;
    Integer largestGlobId = 0;

    for (Glob glob : repository.getAll(TransactionType.TYPE)) {
      int fieldLength = globStringifier.toString(glob, repository).length();
      if (largestString < fieldLength) {
        largestString = fieldLength;
        largestGlobId = glob.get(TransactionType.ID);
      }
    }

    return largestGlobId;
  }
}
