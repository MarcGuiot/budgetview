package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.CustomBoldLabelCustomizer;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.expansion.*;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.*;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class SeriesEvolutionView extends View {

  public static final int LABEL_COLUMN_INDEX = 1;
  public static final int MONTH_COLUMNS_COUNT = 8;

  private GlobRepository parentRepository;

  private SelectionService parentSelectionService;
  private GlobTableView globTable;
  private JTable table;
  private List<SeriesEvolutionMonthColumn> monthColumns = new ArrayList<SeriesEvolutionMonthColumn>();
  private Integer referenceMonthId;

  public SeriesEvolutionView(GlobRepository repository, Directory directory) {
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
    updater.setExcludeBudgetAreaAll(true);
    updater.setCreateSummaries(true);
    updater.globsReset(parentRepository, Utils.set(BudgetArea.TYPE, Series.TYPE));
    parentRepository.addChangeListener(updater);
    return localRepository;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    parentBuilder.add("seriesEvolutionView", createLocalPanel());
  }

  private GlobsPanelBuilder createLocalPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/seriesEvolutionView.splits",
                                                      repository, directory);

    SeriesEditionDialog seriesEditionDialog = new SeriesEditionDialog(directory.get(JFrame.class), parentRepository, directory);

    ExpandableTableAdapter tableAdapter = new ExpandableTableAdapter();

    // attention CategoryExpansionModel doit etre enregistré comme listener de changetSet avant la table.
    SeriesExpansionModel expansionModel = new SeriesExpansionModel(repository, tableAdapter, true);
    expansionModel.setBaseMatcher(new ActiveSeriesMatcher());

    SeriesWrapperStringifier stringifier = new SeriesWrapperStringifier(parentRepository, directory);

    SeriesWrapperComparator comparator = new SeriesWrapperComparator(parentRepository, repository, stringifier);
    globTable = GlobTableView.init(SeriesWrapper.TYPE, repository, comparator, directory);

    CustomBoldLabelCustomizer customizer = new CustomBoldLabelCustomizer(directory) {
      protected boolean isBold(Glob glob) {
        return glob.get(SeriesWrapper.MASTER) == null;
      }
    };

    SeriesEvolutionColors colors = new SeriesEvolutionColors(parentRepository, directory);
    CellPainter backgroundPainter = new SeriesEvolutionBackgroundPainter(colors);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    globTable
      .setDefaultBackgroundPainter(backgroundPainter)
      .setHeaderActionsDisabled()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    globTable
      .addColumn("", expandColumn, expandColumn, GlobStringifiers.empty(stringifier.getComparator(repository)))
      .addColumn("", stringifier, customizer);

    for (int offset = -1; offset < -1 + MONTH_COLUMNS_COUNT; offset++) {
      SeriesEvolutionMonthColumn monthColumn =
        new SeriesEvolutionMonthColumn(offset, globTable, parentRepository, directory, colors, seriesEditionDialog);
      monthColumns.add(monthColumn);
      globTable.addColumn(monthColumn);
    }

    PicsouTableHeaderPainter.install(globTable, directory);

    table = globTable.getComponent();

    Gui.installRolloverOnButtons(table, Utils.intRange(2, 10));
    table.setDragEnabled(false);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());

    tableAdapter.setFilter(expansionModel);

    expandColumn.init(tableAdapter, expansionModel);
    TableExpansionInstaller.setUp(tableAdapter, expansionModel, table, expandColumn, LABEL_COLUMN_INDEX);
    table.setDragEnabled(false);
    TableUtils.setSize(table, LABEL_COLUMN_INDEX, 150);

    expansionModel.completeInit();

    builder.add("seriesEvolutionTable", table);

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        if (!monthIds.isEmpty()) {
          referenceMonthId = monthIds.iterator().next();
          for (SeriesEvolutionMonthColumn column : monthColumns) {
            column.setReferenceMonthId(referenceMonthId);
          }
          globTable.reset();
        }
      }
    }, Month.TYPE);

    parentRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!changeSet.containsCreationsOrDeletions(SeriesWrapper.TYPE) &&
            changeSet.containsChanges(SeriesStat.TYPE)) {
          globTable.reset();
        }
      }
    });

    builder.add("expand", new ExpandTableAction(expansionModel));
    builder.add("collapse", new CollapseTableAction(expansionModel));

    return builder;
  }

  private class ExpandableTableAdapter implements ExpandableTable {
    public Glob getSelectedGlob() {
      int index = table.getSelectedRow();
      if (index < 0) {
        return null;
      }
      return globTable.getGlobAt(index);
    }

    public void select(Glob seriesWrapper) {
      globTable.select(seriesWrapper);
    }

    public void setFilter(GlobMatcher matcher) {
      globTable.setFilter(matcher);
    }
  }

  private class ActiveSeriesMatcher implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      if (!SeriesWrapperType.SERIES.isOfType(wrapper)) {
        return true;
      }

      if (referenceMonthId == null) {
        return false;
      }

      for (int offset = -1; offset < -1 + MONTH_COLUMNS_COUNT; offset++) {
        int monthId = Month.normalize(referenceMonthId + offset);
        Glob seriesStat = parentRepository.find(Key.create(SeriesStat.SERIES, wrapper.get(SeriesWrapper.ITEM_ID),
                                                           SeriesStat.MONTH, monthId));
        if ((seriesStat != null) && Amounts.isNotZero(seriesStat.get(SeriesStat.PLANNED_AMOUNT))) {
          return true;
        }
      }

      return false;
    }
  }

}
