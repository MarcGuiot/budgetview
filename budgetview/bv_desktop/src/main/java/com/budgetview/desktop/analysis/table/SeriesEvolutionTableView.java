package com.budgetview.desktop.analysis.table;

import com.budgetview.desktop.View;
import com.budgetview.desktop.analysis.SeriesChartsBackgroundPainter;
import com.budgetview.desktop.analysis.SeriesChartsColors;
import com.budgetview.desktop.card.NavigationPopup;
import com.budgetview.desktop.components.JPopupButton;
import com.budgetview.desktop.components.expansion.*;
import com.budgetview.desktop.components.table.TransactionTableHeaderPainter;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.series.view.*;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.utils.Lang;
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
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.*;
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
  private String name;

  protected SeriesEvolutionTableView(String name, GlobRepository repository,
                                     SeriesChartsColors seriesChartsColors,
                                     Directory directory, Directory parentDirectory) {
    super(repository, directory);
    this.name = name;
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
    expansionModel = new SeriesExpansionModel(repository, tableAdapter, directory);
    expansionModel.setBaseMatcher(or(not(fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId())),
                                     new SeriesWrapperMatchers.ActiveSeries() {
                                       public Iterable<Integer> getMonthRange() {
                                         if (monthColumns.isEmpty()) {
                                           return Collections.emptyList();
                                         }
                                         return Month.range(monthColumns.get(0).getReferenceMonthId(),
                                                            monthColumns.get(monthColumns.size() - 1).getReferenceMonthId());
                                       }

                                       public Integer getReferenceMonthId() {
                                         return referenceMonthId;
                                       }
                                     }));

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

    tableView.setPopupFactory(new TablePopupFactory());

    TransactionTableHeaderPainter.install(tableView, directory);

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

    builder.add(name, table);

    final JScrollPane scrollPane = new JScrollPane();
    builder.add("tableScrollPane", scrollPane);
    scrollPane.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        addOrRemoveColumns(scrollPane);
      }
    });

    JPopupMenu menu = new JPopupMenu();
    menu.add(new ExpandTableAction(expansionModel));
    menu.add(new CollapseTableAction(expansionModel));
    menu.addSeparator();
    menu.add(tableView.getCopyTableAction(Lang.get("copyTable"), 0));
    builder.add("tableActionsMenu", new JPopupButton(Lang.get("seriesAnalysis.tableMenu"), menu));

    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "doExpand");
    table.getActionMap().put("doExpand", new ExpandSelectionAction(expansionModel, repository, directory));
    table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "doCollapse");
    table.getActionMap().put("doCollapse", new CollapseSelectionAction(expansionModel, repository, directory));

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

  private void addOrRemoveColumns(final JScrollPane scrollPane) {
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
      monthColumn.setReferenceMonthId(referenceMonthId);
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
        addOrRemoveColumns(scrollPane);
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

  private class SeriesWrapperMatcher implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      return !SeriesWrapper.isAll(wrapper);
    }
  }

  private class TablePopupFactory implements PopupMenuFactory {

    private NavigationPopup navigationPopup;

    public JPopupMenu createPopup() {

      JPopupMenu popup = new JPopupMenu();

      if (navigationPopup == null) {
        navigationPopup = new NavigationPopup(tableView.getComponent(), repository, directory, parentSelectionService);
      }

      SortedSet<Integer> monthIds = parentSelectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
      GlobList wrappers = tableView.getCurrentSelection();
      navigationPopup.initPopup(popup, monthIds, SeriesWrapper.getWrappedGlobs(wrappers, repository).getKeySet());

      if (popup.getSubElements().length != 0) {
        popup.addSeparator();
      }

      popup.add(tableView.getCopySelectionAction(Lang.get("copy"), 0));
      return popup;
    }
  }
}
