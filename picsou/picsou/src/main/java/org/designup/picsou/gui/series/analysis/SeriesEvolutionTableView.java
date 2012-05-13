package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.components.expansion.*;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.analysis.evolution.SeriesEvolutionLabelColumn;
import org.designup.picsou.gui.series.analysis.evolution.SeriesEvolutionMonthColumn;
import org.designup.picsou.gui.series.view.*;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.utils.PopupMenuFactory;
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
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.not;
import static org.globsframework.utils.Utils.intRange;

public class SeriesEvolutionTableView extends View {

  public static final int LABEL_COLUMN_INDEX = 1;

  public int monthColumnsCount = 12;

  private GlobTableView tableView;
  private JTable table;
  private List<SeriesEvolutionMonthColumn> monthColumns = new ArrayList<SeriesEvolutionMonthColumn>();
  private Gui.RolloverMouseMotionListener rolloverMouseMotionListener;
  private SeriesEvolutionLabelColumn seriesEvolutionLabelColumn;
  private Integer referenceMonthId;
  private SelectionService parentSelectionService;
  private SeriesChartsColors seriesChartsColors;
  private SeriesExpansionModel expansionModel;
  private int lastWidth;

  protected SeriesEvolutionTableView(GlobRepository repository,
                                     SeriesChartsColors seriesChartsColors,
                                     Directory directory, Directory parentDirectory) {
    super(repository, directory);
    this.seriesChartsColors = seriesChartsColors;
    this.parentSelectionService = parentDirectory.get(SelectionService.class);
  }

  public void registerComponents(GlobsPanelBuilder builder) {

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
    expansionModel = new SeriesExpansionModel(repository, tableAdapter, true, directory);
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
    CellPainter backgroundPainter = new SeriesChartsBackgroundPainter(seriesChartsColors);
    TableExpansionColumn expandColumn = new TableExpansionColumn(backgroundPainter);

    tableView
      .setDefaultBackgroundPainter(backgroundPainter)
      .setHeaderActionsDisabled()
      .setDefaultFont(Gui.DEFAULT_TABLE_FONT);

    tableView
      .addColumn("", expandColumn, expandColumn, GlobStringifiers.empty(stringifier.getComparator(repository)));

    seriesEvolutionLabelColumn = new SeriesEvolutionLabelColumn(tableView, repository, directory, seriesChartsColors);
    tableView.addColumn(seriesEvolutionLabelColumn);

    for (int offset = -1; offset < -1 + monthColumnsCount; offset++) {
      SeriesEvolutionMonthColumn monthColumn =
        new SeriesEvolutionMonthColumn(offset, tableView, repository, directory, seriesChartsColors);
      monthColumns.add(monthColumn);
      tableView.addColumn(monthColumn);
    }

    tableView.setPopupFactory(new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(tableView.getCopyAction(Lang.get("copy")));
        return popup;
      }
    });

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
                                             seriesEvolutionLabelColumn.setReferenceMonthId(referenceMonthId);
                                             tableView.reset();
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

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        lastWidth = scrollPane.getWidth();
      }
    });
  }

  private void addOrRemoveColumn(final JScrollPane scrollPane) {
    int[] size;
    size = getColumnSize();
    int lastColumnSize = size[size.length - 1];
    int scrollWidth = scrollPane.getWidth();
    if (lastWidth == scrollWidth) {
      return;
    }
    lastWidth = scrollWidth;
    
    int free = lastWidth - table.getColumnModel().getTotalColumnWidth();
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
        new SeriesEvolutionMonthColumn(monthColumnsCount - 2, tableView, repository, directory, seriesChartsColors);
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

  public Action getExpandAction() {
    return new ExpandTableAction(expansionModel);
  }

  public Action getCollapseAction() {
    return new CollapseTableAction(expansionModel);
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
        Glob seriesStat = repository.find(SeriesStat.createKey(wrapper.get(SeriesWrapper.ITEM_ID), monthId));
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
