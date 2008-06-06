package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.utils.PopupMenuFactory;
import org.crossbowlabs.globs.gui.utils.TableUtils;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.alignRight;
import static org.crossbowlabs.globs.gui.views.utils.LabelCustomizers.chain;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.utils.*;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

public class CategoryView extends View {
  public static final int EXPANSION_COLUMN_INDEX = 0;
  public static final int CATEGORY_COLUMN_INDEX = 1;
  public static final int AMOUNT_COLUMN_INDEX = 2;

  private GlobTableView tableView;
  private JTable table;
  private CreateCategoryAction createCategoryAction;
  private RenameCategoryAction renameCategoryAction;
  private DeleteCategoryAction deleteCategoryAction;
  protected CategoryExpansionModel expansionModel;

  public CategoryView(GlobRepository repository, final Directory directory) {
    super(repository, directory);
    createCategoryAction = new CreateCategoryAction(repository, directory){
      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(directory.get(JFrame.class));
      }
    };
    renameCategoryAction = new RenameCategoryAction(repository, directory){
      public JDialog getDialog(ActionEvent e) {
        return PicsouDialog.create(directory.get(JFrame.class));
      }
      
    };
    deleteCategoryAction = new DeleteCategoryAction(repository, directory);
    createTable();
  }

  public void registerComponents(SplitsBuilder builder) {
    builder.add(table);
    builder.add("createCategory", createCategoryAction);
    builder.add("renameCategory", renameCategoryAction);
    builder.add("deleteCategory", deleteCategoryAction);
  }

  public void select(Integer categoryId) {
    tableView.select(repository.get(Key.create(Category.TYPE, categoryId)));
  }

  private void createTable() {
    GlobStringifier categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    CategoryDataProvider provider = new CategoryDataProvider(repository, directory);
    GlobStringifier amountStringifier = new AmountStringifier(provider);
    CategoryComparator categoryComparator = new CategoryComparator(repository, categoryStringifier);

    tableView = GlobTableView.init(Category.TYPE, repository, categoryComparator, directory);

    CategoryLabelCustomizer customizer = new CategoryLabelCustomizer(directory);
    CategoryBackgroundPainter backgroundPainter = new CategoryBackgroundPainter(directory);
    CategoryExpansionColumn expandColumn = new CategoryExpansionColumn(backgroundPainter, selectionService);
    CategoryColumn categoryColumn = new CategoryColumn(customizer, backgroundPainter, tableView,
                                                       descriptionService, repository, directory);

    tableView.addColumn(" ", expandColumn, expandColumn, categoryStringifier.getComparator(repository))
      .addColumn(Lang.get("category"), categoryColumn, categoryColumn, categoryComparator)
      .addColumn(Lang.get("amount"), amountStringifier, chain(alignRight(), customizer), backgroundPainter)
      .setHeaderCustomizer(new PicsouTableHeaderCustomizer(directory, PicsouColors.CATEGORY_TABLE_HEADER_TITLE),
                           new PicsouTableHeaderPainter(directory,
                                                        PicsouColors.CATEGORY_TABLE_HEADER_DARK,
                                                        PicsouColors.CATEGORY_TABLE_HEADER_MEDIUM,
                                                        PicsouColors.CATEGORY_TABLE_HEADER_LIGHT,
                                                        PicsouColors.CATEGORY_TABLE_HEADER_BORDER))
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    tableView.setPopupFactory(new CategoryPopupMenuFactory());

    provider.setView(tableView);

    table = tableView.getComponent();

    expansionModel = new CategoryExpansionModel(repository, this);
    expandColumn.init(this, expansionModel);

    setInitialColumnSizes(expandColumn);

    installDoubleClickExpansion();
    Gui.installRolloverOnButtons(table, new int[]{CATEGORY_COLUMN_INDEX});
    table.setDragEnabled(false);
  }

  private void installDoubleClickExpansion() {
    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        Glob category = getSelectedCategory();
        expansionModel.toggleExpansion(category);
      }
    });
  }

  private void setInitialColumnSizes(CategoryExpansionColumn column) {
    TableUtils.setSize(table, EXPANSION_COLUMN_INDEX, column.getPreferredWidth());

    JLabel renderer = (JLabel) TableUtils.getRenderedComponentAt(table, 0, AMOUNT_COLUMN_INDEX);
    renderer.setText(format(PicsouSamples.AMOUNT_SAMPLE));
    TableUtils.setSize(table, AMOUNT_COLUMN_INDEX, TableUtils.getPreferredWidth(renderer));

    TableUtils.autosizeColumn(table, CATEGORY_COLUMN_INDEX);
  }

  Glob getSelectedCategory() {
    return tableView.getGlobAt(table.getSelectedRow());
  }

  void select(Glob category) {
    tableView.select(category);
  }

  void setFilter(GlobMatcher matcher) {
    tableView.setFilter(matcher);
  }

  private class AmountStringifier extends AbstractGlobStringifier {
    private CategoryDataProvider dataProvider;

    public AmountStringifier(CategoryDataProvider dataProvider) {
      this.dataProvider = dataProvider;
    }

    public String toString(Glob category, GlobRepository repository) {
      return format(dataProvider.getAmount(category.get(Category.ID)));
    }
  }

  private String format(double amount) {
    if (amount == 0.0) {
      return "";
    }
    return PicsouDescriptionService.INTEGER_FORMAT.format(amount);
  }

  private class CategoryPopupMenuFactory implements PopupMenuFactory {
    public JPopupMenu createPopup() {
      JPopupMenu menu = new JPopupMenu();
      add(createCategoryAction, "create.category.popup", menu);
      add(renameCategoryAction, "rename.category.popup", menu);
      add(deleteCategoryAction, "delete.category.popup", menu);
      return menu;
    }

    private void add(Action action, String label, JPopupMenu menu) {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
      item.setLabel(Lang.get(label));
      item.setFont(Gui.DEFAULT_TABLE_FONT);
      menu.add(item);
    }
  }
}

