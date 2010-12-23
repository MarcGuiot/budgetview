package org.designup.picsou.gui.series.view;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.CustomBoldLabelCustomizer;
import org.designup.picsou.gui.components.SelectorBackgroundPainter;
import org.designup.picsou.gui.components.expansion.*;
import org.designup.picsou.gui.series.utils.SeriesSelectionConverter;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

/** @deprecated A SUPPRIMER */
public class SeriesView extends View {
  public static final int LABEL_COLUMN_INDEX = 1;
  private SelectionService parentSelectionService;

  private GlobTableView tableView;
  private SeriesExpansionModel expansionModel;

  public SeriesView(GlobRepository repository, Directory directory) {
    super(repository, createLocalDirectory(directory));
    this.parentSelectionService = directory.get(SelectionService.class);
  }

  private static Directory createLocalDirectory(Directory directory) {
    Directory localDirectory = new DefaultDirectory(directory);
    SelectionService localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder builder) {

    SeriesSelectionConverter converter = 
      new SeriesSelectionConverter(parentSelectionService, selectionService, repository);
    converter.register();

    ExpandableTable tableAdapter = new ExpandableTable(new SeriesWrapperMatcher());

    // attention CategoryExpansionModel doit etre enregistré comme listener de changetSet avant la table.
    expansionModel = new SeriesExpansionModel(repository, tableAdapter, false, directory);

    SeriesWrapperStringifier stringifier = new SeriesWrapperStringifier(repository, directory);

    tableView = GlobTableView.init(SeriesWrapper.TYPE, repository,
                                   new SeriesWrapperComparator(repository, repository, stringifier), directory);

    CustomBoldLabelCustomizer customizer = new CustomBoldLabelCustomizer(directory) {
      protected boolean isBold(Glob glob) {
        return glob.get(SeriesWrapper.MASTER) == null;
      }
    };

    SelectorBackgroundPainter backgroundPainter = new SelectorBackgroundPainter(directory);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    tableView
      .setDefaultBackgroundPainter(backgroundPainter)
      .addColumn(" ", expandColumn, expandColumn,
                 GlobStringifiers.empty(stringifier.getComparator(repository)))
      .addColumn(Lang.get("series"), stringifier, customizer)
      .setHeaderHidden()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);
    tableAdapter.setTable(tableView);
    JTable table = tableView.getComponent();

    tableAdapter.setFilter(expansionModel);

    expandColumn.init(tableAdapter, expansionModel);
    TableExpansionInstaller.setUp(tableAdapter, expansionModel, table, expandColumn, LABEL_COLUMN_INDEX);
    table.setDragEnabled(false);

    expansionModel.completeInit();

    builder.add("seriesView", table);

    builder.add("expand", new ExpandTableAction(expansionModel));
    builder.add("collapse", new CollapseTableAction(expansionModel));
  }

  public void selectBudgetArea(BudgetArea budgetArea) {
    Glob wrapper = SeriesWrapper.find(repository, SeriesWrapperType.BUDGET_AREA, budgetArea.getId());
    tableView.select(wrapper);
  }

  public void selectSeries(Glob series) {
    Glob wrapper = SeriesWrapper.find(repository, SeriesWrapperType.SERIES, series.get(Series.ID));
    Glob master = repository.findLinkTarget(wrapper, SeriesWrapper.MASTER);
    if (master != null) {
      expansionModel.setExpanded(master);
    }

    tableView.select(wrapper);
  }

  public void selectAll() {
    tableView.selectFirst();
  }

  private class SeriesWrapperMatcher implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      return !SeriesWrapper.isSummary(wrapper);
    }
  }

}
