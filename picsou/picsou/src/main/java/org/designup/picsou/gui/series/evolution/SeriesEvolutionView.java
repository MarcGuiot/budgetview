package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.actions.SelectNextMonthAction;
import org.designup.picsou.gui.actions.SelectPreviousMonthAction;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.expansion.*;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.*;
import org.designup.picsou.gui.utils.Gui;
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
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.utils.Utils.intRange;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;

public class SeriesEvolutionView extends View {

  public static final int LABEL_COLUMN_INDEX = 1;
  public int monthColumnsCount = 12;

  private Directory parentDirectory;
  private SelectionService parentSelectionService;
  private GlobTableView tableView;
  private JTable table;
  private SeriesEvolutionChartPanel chartPanel;
  private List<SeriesEvolutionMonthColumn> monthColumns = new ArrayList<SeriesEvolutionMonthColumn>();
  private Integer referenceMonthId;
  private SeriesEvolutionColors seriesEvolutionColors;
  private SeriesAmountEditionDialog seriesAmountEditionDialog;
  private Gui.RolloverMouseMotionListener rolloverMouseMotionListener;

  public SeriesEvolutionView(GlobRepository repository, Directory directory) {
    super(repository, createLocalDirectory(directory));
    this.parentDirectory = directory;
    this.parentSelectionService = directory.get(SelectionService.class);
  }

  private static Directory createLocalDirectory(Directory parentDirectory) {
    Directory localDirectory = new DefaultDirectory(parentDirectory);
    SelectionService localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    return localDirectory;
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    parentBuilder.add("seriesEvolutionView", createLocalPanel());
  }

  private GlobsPanelBuilder createLocalPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/evolution/seriesEvolutionView.splits",
                                                      repository, directory);

    SeriesEditionDialog seriesEditionDialog = directory.get(SeriesEditionDialog.class);
    seriesAmountEditionDialog = new SeriesAmountEditionDialog(repository, directory, seriesEditionDialog);

    ExpandableTable tableAdapter = new ExpandableTable(new SeriesWrapperMatcher()) {
      public Glob getSelectedGlob() {
        int index = table.getSelectedRow();
        if (index < 0) {
          return null;
        }
        return tableView.getGlobAt(index);
      }
    };

    // attention CategoryExpansionModel doit etre enregistré comme listener de changetSet avant la table.
    SeriesExpansionModel expansionModel = new SeriesExpansionModel(repository, tableAdapter, true, directory);
    expansionModel.setBaseMatcher(new ActiveSeriesMatcher());

    SeriesWrapperStringifier stringifier = new SeriesWrapperStringifier(repository, directory);

    SeriesWrapperComparator comparator = new SeriesWrapperComparator(repository, repository, stringifier);
    tableView = GlobTableView.init(SeriesWrapper.TYPE, repository, comparator, directory);
    tableAdapter.setTable(tableView);
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Series.TYPE)) {
          tableView.refresh(false);
        }
      }
    });

    seriesEvolutionColors = new SeriesEvolutionColors(repository, directory);
    CellPainter backgroundPainter = new SeriesEvolutionBackgroundPainter(seriesEvolutionColors);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    tableView
      .setDefaultBackgroundPainter(backgroundPainter)
      .setHeaderActionsDisabled()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    tableView
      .addColumn("", expandColumn, expandColumn, GlobStringifiers.empty(stringifier.getComparator(repository)));

    SeriesEvolutionLabelColumn labelColumn =
      new SeriesEvolutionLabelColumn(tableView, repository, directory, seriesEvolutionColors, seriesEditionDialog);
    tableView.addColumn(labelColumn);

    for (int offset = -1; offset < -1 + monthColumnsCount; offset++) {
      SeriesEvolutionMonthColumn monthColumn =
        new SeriesEvolutionMonthColumn(offset, tableView, repository, directory,
                                       seriesEvolutionColors, seriesAmountEditionDialog);
      monthColumns.add(monthColumn);
      tableView.addColumn(monthColumn);
    }

    PicsouTableHeaderPainter.install(tableView, directory);

    table = tableView.getComponent();
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    rolloverMouseMotionListener = Gui.installRolloverOnButtons(table, intRange(2, monthColumnsCount + 1));
    table.setDragEnabled(false);
    ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());

    tableAdapter.setFilter(expansionModel);

    expandColumn.init(tableAdapter, expansionModel);
    TableExpansionInstaller.setUp(tableAdapter, expansionModel, table, expandColumn, LABEL_COLUMN_INDEX);
    table.setDragEnabled(false);
    TableUtils.setSize(table, LABEL_COLUMN_INDEX, 150);
    int width = monthColumns.get(0).getWidth();
    for (int i = 0; i < monthColumns.size(); i++) {
      TableUtils.setSize(table, i + 2, width);
    }

    expansionModel.completeInit();

    builder.add("seriesEvolutionTable", table);

    final JScrollPane scrollPane = new JScrollPane();
    builder.add("tableScrollPane", scrollPane);
    scrollPane.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        addOrRemoveColumn(scrollPane);
      }
    });

    parentSelectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SortedSet<Integer> monthIds = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        if (!monthIds.isEmpty()) {
          referenceMonthId = monthIds.iterator().next();
          for (SeriesEvolutionMonthColumn column : monthColumns) {
            column.setReferenceMonthId(referenceMonthId);
          }
          tableView.reset();
          chartPanel.monthSelected(referenceMonthId);
        }
      }
    }, Month.TYPE);

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (!changeSet.containsCreationsOrDeletions(SeriesWrapper.TYPE) &&
            changeSet.containsChanges(SeriesStat.TYPE)) {
          tableView.reset();
        }
      }
    });

    builder.add("expand", new ExpandTableAction(expansionModel));
    builder.add("collapse", new CollapseTableAction(expansionModel));
    builder.add("previousMonth", new SelectPreviousMonthAction(repository, parentDirectory));
    builder.add("nextMonth", new SelectNextMonthAction(repository, parentDirectory));

    this.chartPanel = new SeriesEvolutionChartPanel(repository, directory, parentSelectionService);
    this.chartPanel.registerCharts(builder);

    return builder;
  }

  private void addOrRemoveColumn(final JScrollPane scrollPane) {
    int[] size;
    size = getColumnSize();
    int lastColumnSize = size[size.length - 1];
    int free = scrollPane.getWidth() - table.getColumnModel().getTotalColumnWidth();
    if (free < -lastColumnSize) {
      if (monthColumns.isEmpty()) {
        return;
      }
      monthColumnsCount--;
      rolloverMouseMotionListener.removeColumn(monthColumnsCount + 2);
      monthColumns.remove(monthColumnsCount);
      tableView.removeColumn(monthColumnsCount + 2);
      parentSelectionService.select(parentSelectionService.getSelection(Month.TYPE), Month.TYPE);
    }
    else if (free > lastColumnSize / 3.) {
      monthColumnsCount++;
      SeriesEvolutionMonthColumn monthColumn =
        new SeriesEvolutionMonthColumn(monthColumnsCount - 2, tableView, repository, directory,
                                       seriesEvolutionColors, seriesAmountEditionDialog);
      monthColumns.add(monthColumn);
      tableView.addColumn(monthColumn);
      rolloverMouseMotionListener.addColumn(monthColumnsCount + 1);
      TableUtils.setSize(table, monthColumnsCount + 1, monthColumn.getWidth());
      parentSelectionService.select(parentSelectionService.getSelection(Month.TYPE), Month.TYPE);
    }
    else {
      return;
    }
    for (int i = 0; i < size.length && i < table.getColumnModel().getColumnCount(); i++) {
      TableUtils.setSize(table, i, size[i]);
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        addOrRemoveColumn(scrollPane);
      }
    });
  }

  private int[] getColumnSize() {
    int size[] = new int[table.getColumnModel().getColumnCount()];
    Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
    int i = 0;
    while (columns.hasMoreElements()) {
      TableColumn column = columns.nextElement();
      size[i] = column.getWidth();
      i++;
    }
    return size;
  }

  public void reset() {
    chartPanel.reset();
    referenceMonthId = null;
  }

  private class ActiveSeriesMatcher implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      if (!SeriesWrapperType.SERIES.isOfType(wrapper)) {
        return true;
      }

      if (referenceMonthId == null) {
        return false;
      }

      for (int offset = -1; offset < -1 + monthColumnsCount; offset++) {
        int monthId = Month.offset(referenceMonthId, offset);
        Glob seriesStat = SeriesEvolutionView.this.repository.find(Key.create(SeriesStat.SERIES, wrapper.get(SeriesWrapper.ITEM_ID),
                                                                              SeriesStat.MONTH, monthId));
        if ((seriesStat != null) &&
            (Amounts.isNotZero(seriesStat.get(SeriesStat.PLANNED_AMOUNT))
             || Amounts.isNotZero(seriesStat.get(SeriesStat.AMOUNT)))) {
          return true;
        }
      }

      return false;
    }
  }

  private class SeriesWrapperMatcher implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      return !SeriesWrapper.isAll(wrapper);
    }
  }
}

