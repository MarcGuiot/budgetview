package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.utils.TableUtils;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.gui.views.utils.LabelCustomizers;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.alignRight;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.chain;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.LocalGlobRepository;
import org.crossbowlabs.globs.model.utils.LocalGlobRepositoryBuilder;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Transaction;
import static org.designup.picsou.model.Transaction.LABEL;
import static org.designup.picsou.model.Transaction.NOTE;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CategoryPropagationDialog {
  private static final int DATE_COLUMN_INDEX = 0;
  private static final int LABEL_COLUMN_INDEX = 2;

  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private CategoryPropagationCallback callback;
  private GlobList transactions;
  private TransactionRendererColors rendererColors;
  private Directory localDirectory;
  private DescriptionService descriptionService;

  public CategoryPropagationDialog(CategoryPropagationCallback callback, GlobList transactions,
                                   TransactionRendererColors rendererColors,
                                   GlobRepository repository, Directory directory) {
    this.callback = callback;
    this.transactions = transactions;
    this.rendererColors = rendererColors;
    descriptionService = directory.get(DescriptionService.class);

    localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Category.TYPE)
        .copy(transactions)
        .get();

    localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(selectionService);
    descriptionService = localDirectory.get(DescriptionService.class);
    rendererColors = new TransactionRendererColors(localDirectory);

    GlobsPanelBuilder builder = GlobsPanelBuilder.init(localRepository, localDirectory);
    addTable(builder);
    builder.add("ok", new OkAction());
    builder.add("cancel", new CancelAction());

    JPanel panel = (JPanel) builder.parse(getClass(), "/layout/categoryPropagation.splits");
    dialog = PicsouDialog.create(directory.get(JFrame.class));
    dialog.getContentPane().add(panel);
  }

  public void show() {
    dialog.setSize(700, 600);
    GuiUtils.showCentered(dialog);
  }

  private void addTable(GlobsPanelBuilder builder) {
    TransactionComparator comparator = new TransactionComparator(false);
    GlobTableView view = builder.addTable(Transaction.TYPE, comparator);

    view.setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    TransactionViewUtils.configureHeader(view, localDirectory);

    GlobStringifier amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    GlobStringifier categoriesStringifier =
      new TransactionCategoriesStringifier(descriptionService.getStringifier(Category.TYPE));

    view
      .addColumn(Lang.get("date"), new TransactionDateStringifier(comparator))
      .addColumn(descriptionService.getLabel(Category.TYPE), categoriesStringifier)
      .addColumn(LABEL)
      .addColumn(Lang.get("amount"),
                 amountStringifier,
                 chain(alignRight(), LabelCustomizers.stringifier(amountStringifier, localRepository)))
      .addColumn(NOTE);

    JTable table = view.getComponent();
    table.setDefaultRenderer(Glob.class,
                             new TransactionTableRenderer(table.getDefaultRenderer(Glob.class),
                                                          rendererColors,
                                                          -1));

    adjustColumnsSize(table);
  }

  private void adjustColumnsSize(JTable table) {
    TableUtils.autosizeColumn(table, LABEL_COLUMN_INDEX);
    adjustColumnSize(table, DATE_COLUMN_INDEX);
  }

  private void adjustColumnSize(JTable table, int columnIndex) {
    TableUtils.setSize(table, columnIndex,
                       TableUtils.getPreferredWidth(
                         TableUtils.getRenderedComponent(table, transactions.get(0), 0, columnIndex)));
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("split.transaction.ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.dispose();
      dialog.setVisible(false);
      callback.propagate();
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("split.transaction.close"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.dispose();
      dialog.setVisible(false);
    }
  }
}
