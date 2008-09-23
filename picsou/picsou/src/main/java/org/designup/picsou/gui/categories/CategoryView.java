package org.designup.picsou.gui.categories;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.expansion.ExpandableTable;
import org.designup.picsou.gui.components.expansion.TableExpansionColumn;
import org.designup.picsou.gui.categories.columns.*;
import org.designup.picsou.gui.description.CategoryComparator;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.GlobTableView;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CategoryView extends View implements ExpandableTable {
  public static final int EXPANSION_COLUMN_INDEX = 0;
  public static final int CATEGORY_COLUMN_INDEX = 1;
  public static final int AMOUNT_COLUMN_INDEX = 2;

  private GlobTableView globTable;
  private JTable table;
  protected CategoryExpansionModel expansionModel;

  public CategoryView(GlobRepository repository, final Directory directory) {
    super(repository, directory);
    createTable();
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(table);
  }

  public void select(Integer categoryId) {
    globTable.select(repository.get(Key.create(Category.TYPE, categoryId)));
  }

  private void createTable() {
    // attention CategoryExpansionModel doit etre enregistré comme listener de changetSet avant la table.
    expansionModel = new CategoryExpansionModel(repository, this);

    GlobStringifier categoryStringifier = descriptionService.getStringifier(Category.TYPE);
    CategoryDataProvider provider = new CategoryDataProvider(repository, directory);
    GlobStringifier amountStringifier = new AmountStringifier(provider);
    CategoryComparator categoryComparator = new CategoryComparator(repository, categoryStringifier);

    globTable = GlobTableView.init(Category.TYPE, repository, categoryComparator, directory);

    CategoryLabelCustomizer customizer = new CategoryLabelCustomizer(directory);
    CategoryBackgroundPainter backgroundPainter = new CategoryBackgroundPainter(directory);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    globTable
      .setDefaultBackgroundPainter(backgroundPainter)
      .addColumn(" ", expandColumn, expandColumn, categoryStringifier.getComparator(repository))
      .addColumn(Lang.get("category"), categoryStringifier, customizer)
      .addColumn(Lang.get("amount"), amountStringifier, chain(ALIGN_RIGHT, customizer))
      .hideHeader()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    provider.setView(globTable);

    table = globTable.getComponent();

    setFilter(expansionModel);
    expandColumn.init(this, expansionModel);

    setInitialColumnSizes(expandColumn);

    installDoubleClickExpansion();
    Gui.installRolloverOnButtons(table, new int[]{CATEGORY_COLUMN_INDEX});
    table.setDragEnabled(false);

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsCreationsOrDeletions(Category.TYPE)) {
          setFilter(expansionModel);
        }
      }
    });
  }

  private void installDoubleClickExpansion() {
    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        Glob category = getSelectedGlob();
        expansionModel.toggleExpansion(category);
      }
    });
  }

  private void setInitialColumnSizes(TableExpansionColumn column) {
    TableUtils.setSize(table, EXPANSION_COLUMN_INDEX, column.getPreferredWidth());

    JLabel renderer = (JLabel)TableUtils.getRenderedComponentAt(table, 0, AMOUNT_COLUMN_INDEX);
    renderer.setText(format(-10000));
    TableUtils.setSize(table, AMOUNT_COLUMN_INDEX, TableUtils.getPreferredWidth(renderer));

    TableUtils.autosizeColumn(table, CATEGORY_COLUMN_INDEX);
  }

  public Glob getSelectedGlob() {
    return globTable.getGlobAt(table.getSelectedRow());
  }

  public void select(Glob category) {
    globTable.select(category);
  }

  public void setFilter(GlobMatcher matcher) {
    globTable.setFilter(matcher);
  }

  public void setVisible(boolean visible) {
    table.setVisible(visible);
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
}

