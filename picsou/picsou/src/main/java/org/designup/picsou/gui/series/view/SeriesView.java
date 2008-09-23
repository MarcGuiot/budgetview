package org.designup.picsou.gui.series.view;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.CustomBoldLabelCustomizer;
import org.designup.picsou.gui.components.SelectorBackgroundPainter;
import org.designup.picsou.gui.components.expansion.ExpandableTable;
import org.designup.picsou.gui.components.expansion.TableExpansionColumn;
import org.designup.picsou.gui.components.expansion.TableExpansionInstaller;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesView extends View implements ExpandableTable {
  public static final int LABEL_COLUMN_INDEX = 1;
  private GlobRepository parentRepository;
  private SelectionService parentSelectionService;

  private GlobTableView globTable;
  private SeriesExpansionModel expansionModel;
  private JTable table;

  public SeriesView(GlobRepository repository, Directory directory) {
    super(createLocalRepository(repository), createLocalDirectory(directory));
    this.parentRepository = repository;
    this.parentSelectionService = directory.get(SelectionService.class);
  }

  private static Directory createLocalDirectory(Directory directory) {
    Directory localDirectory = new DefaultDirectory(directory);
    SelectionService localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    return localDirectory;
  }

  private static GlobRepository createLocalRepository(GlobRepository parentRepository) {
    GlobRepository localRepository = GlobRepositoryBuilder.createEmpty();

    SeriesWrapperUpdater updater = new SeriesWrapperUpdater(localRepository);
    updater.globsReset(parentRepository, Utils.set(BudgetArea.TYPE, Series.TYPE));
    parentRepository.addChangeListener(updater);
    return localRepository;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    registerSelectionUpdater();

    // attention CategoryExpansionModel doit etre enregistré comme listener de changetSet avant la table.
    expansionModel = new SeriesExpansionModel(repository, this);

    SeriesWrapperStringifier stringifier = new SeriesWrapperStringifier(parentRepository, directory);

    globTable = GlobTableView.init(SeriesWrapper.TYPE, repository,
                                   new SeriesWrapperComparator(parentRepository, repository, stringifier), directory);

    CustomBoldLabelCustomizer customizer = new CustomBoldLabelCustomizer(directory) {
      protected boolean isBold(Glob glob) {
        return glob.get(SeriesWrapper.MASTER) == null;
      }
    };

    SelectorBackgroundPainter backgroundPainter = new SelectorBackgroundPainter(directory);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    globTable
      .setDefaultBackgroundPainter(backgroundPainter)
      .addColumn(" ", expandColumn, expandColumn, stringifier.getComparator(repository))
      .addColumn(Lang.get("series"), stringifier, customizer)
      .hideHeader()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    table = globTable.getComponent();

    setFilter(expansionModel);

    expandColumn.init(this, expansionModel);
    TableExpansionInstaller.setUp(this, expansionModel, table, expandColumn, LABEL_COLUMN_INDEX);
    table.setDragEnabled(false);

    expansionModel.completeInit();

    builder.add("seriesView", table);
  }

  private void registerSelectionUpdater() {
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobSelectionBuilder newSelection = new GlobSelectionBuilder();
        for (Glob wrapper : selection.getAll(SeriesWrapper.TYPE)) {
          Integer itemId = wrapper.get(SeriesWrapper.ITEM_ID);
          if (Boolean.TRUE.equals(wrapper.get(SeriesWrapper.IS_BUDGET_AREA))) {
            Glob budgetArea = parentRepository.get(Key.create(BudgetArea.TYPE, itemId));
            newSelection.add(budgetArea);
          }
          else {
            Glob series = parentRepository.get(Key.create(Series.TYPE, itemId));
            newSelection.add(series);
          }
        }
        parentSelectionService.select(newSelection.get());
      }
    }, SeriesWrapper.TYPE);
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

  public void selectAll() {
    globTable.selectFirst();
  }
}
