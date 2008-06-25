package org.globsframework.gui.views;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.impl.*;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.CompositeComparator;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class GlobTableView extends AbstractGlobComponentHolder<GlobTableView> implements GlobSelectionListener {
  private List<GlobTableColumn> columns = new ArrayList<GlobTableColumn>();
  private Comparator<Glob> initialComparator;
  private GlobTableModel tableModel;
  private PopupMenuFactory popupMenuFactory;
  private LabelCustomizer headerLabelCustomizer = LabelCustomizer.NULL;
  private CellPainter headerBackgroundPainter = CellPainter.NULL;
  private JTable table;
  private boolean selectionEnabled = true;
  private Font defaultFont = new JTable().getFont();

  public static GlobTableView init(GlobType type, GlobRepository globRepository,
                                   Comparator<Glob> comparator, Directory directory) {
    return new GlobTableView(type, globRepository, comparator, directory);
  }

  private GlobTableView(GlobType type, GlobRepository globRepository, Comparator<Glob> comparator, Directory directory) {
    super(type, globRepository, directory);
    this.initialComparator = comparator;
  }

  public GlobTableView addColumn(Field field) {
    return addColumn(field, LabelCustomizer.NULL, CellPainter.NULL);
  }

  public GlobTableView addColumn(Field field, TableCellEditor editor) {
    return addColumn(field, LabelCustomizer.NULL, CellPainter.NULL, editor);
  }

  public GlobTableView addColumn(Field field,
                                 LabelCustomizer customizer,
                                 CellPainter backgroundPainter) {
    return addColumn(field, customizer, backgroundPainter, null);
  }

  public GlobTableView addColumn(Field field,
                                 LabelCustomizer customizer,
                                 CellPainter backgroundPainter,
                                 TableCellEditor editor) {
    GlobStringifier stringifier = descriptionService.getStringifier(field);

    LabelCustomizer customizers =
      chain(GlobLabelCustomizerFactory.create(field, stringifier, repository),
            customizer);

    LabelTableCellRenderer renderer =
      new LabelTableCellRenderer(customizers, backgroundPainter);

    return addColumn(descriptionService.getLabel(field),
                     renderer, editor,
                     stringifier.getComparator(repository));
  }

  public GlobTableView addColumn(LinkField link) {
    return addColumn((Link)link);
  }

  public GlobTableView addColumn(Link link) {
    GlobStringifier stringifier = descriptionService.getStringifier(link);
    return addColumn(descriptionService.getLabel(link),
                     new LabelTableCellRenderer(LabelCustomizers.stringifier(stringifier, repository),
                                                CellPainter.NULL),
                     stringifier.getComparator(repository));
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier) {
    return addColumn(name, stringifier, LabelCustomizer.NULL);
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier, LabelCustomizer customizer) {
    return addColumn(name, stringifier, customizer, CellPainter.NULL);
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier, LabelCustomizer customizer, CellPainter backgroundPainter) {
    return addColumn(name,
                     chain(LabelCustomizers.stringifier(stringifier, repository), customizer),
                     backgroundPainter,
                     stringifier.getComparator(repository));
  }

  public GlobTableView addColumn(String name,
                                 LabelCustomizer customizer,
                                 CellPainter backgroundPainter,
                                 Comparator<Glob> comparator) {
    return addColumn(name,
                     new LabelTableCellRenderer(customizer, backgroundPainter),
                     comparator);
  }

  public GlobTableView addColumn(String name, TableCellRenderer renderer, Comparator<Glob> comparator) {
    columns.add(new GlobTableColumn(name, renderer, new CompositeComparator<Glob>(comparator, initialComparator)));
    return this;
  }

  public GlobTableView addColumn(String name, TableCellRenderer renderer, TableCellEditor editor, Comparator<Glob> comparator) {
    columns.add(new GlobTableColumn(name, renderer, editor, new CompositeComparator<Glob>(comparator, initialComparator)));
    return this;
  }

  public GlobTableView setHeaderCustomizer(LabelCustomizer customizer,
                                           CellPainter backgroundPainter) {
    this.headerLabelCustomizer = customizer;
    this.headerBackgroundPainter = backgroundPainter;
    return this;
  }

  public GlobTableView setPopupFactory(PopupMenuFactory factory) {
    this.popupMenuFactory = factory;
    return this;
  }

  public PopupMenuFactory getPopupMenuFactory() {
    return popupMenuFactory;
  }

  public GlobTableView setFilter(GlobMatcher matcher) {
    GlobList selection = getCurrentSelection();
    int initialSize = selection.size();
    selection.filterSelf(matcher, repository);
    boolean selectionChanged = (initialSize != selection.size());

    disableSelectionNotification();
    try {
      tableModel.setFilter(matcher);
    }
    finally {
      if (!selection.isEmpty() && !selectionChanged) {
        select(selection, false);
      }
      enableSelectionNotification(false);
    }

    if (!selection.isEmpty() && selectionChanged) {
      select(selection, true);
    }
    return this;
  }

  public void selectionUpdated(GlobSelection selection) {
    Set newSelection = new HashSet(selection.getAll(type));
    Set currentSelection = new HashSet(getCurrentSelection());
    if (!newSelection.equals(currentSelection)) {
      select(newSelection, false);
    }
  }

  public void select(Glob... globs) throws ItemNotFound {
    select(Arrays.asList(globs), true);
  }

  public void select(Iterable<Glob> globs, boolean sendPending) throws ItemNotFound {
    ListSelectionModel selectionModel = table.getSelectionModel();
    try {
      disableSelectionNotification();
      selectionModel.setValueIsAdjusting(true);
      selectionEnabled = false;
      doSelect(selectionModel, globs);
    }
    finally {
      enableSelectionNotification(sendPending);
    }
  }

  public JTable getComponent() {
    if (table == null) {
      table = new JTable();
      table.setAutoscrolls(true);
      tableModel = new GlobTableModel(type, repository, columns, table,
                                      new TableResetListener(), initialComparator);
      table.setModel(tableModel);
      table.setName(type.getName());
      table.setFont(defaultFont);
      table.setDefaultRenderer(Glob.class, new CompositeRenderer());
      selectionService.addListener(this, type);
      registerSelectionListener();
      initHeader();
      initPopupFactory();
      registerEditors();
    }
    return table;
  }

  private class TableResetListener implements GlobTableModel.ResetListener {
    private GlobList currentSelection = GlobList.EMPTY;

    public void preReset() {
      currentSelection = getCurrentSelection();
      disableSelectionNotification();
    }

    public void reset() {
      GlobList newSelection = new GlobList();
      for (Glob glob : currentSelection) {
        if (tableModel.indexOf(glob) >= 0) {
          newSelection.add(glob);
        }
      }
      select(newSelection, false);
      if (newSelection.size() > 0) {
        Glob first = newSelection.get(0);
        int index = tableModel.indexOf(first);
        Rectangle rect = table.getCellRect(index, 0, true);
        table.scrollRectToVisible(rect);
      }
      enableSelectionNotification(false);
    }
  }

  private void registerEditors() {
    int index = 0;
    for (GlobTableColumn column : columns) {
      TableCellEditor editor = column.getEditor();
      if (editor != null) {
        table.getColumnModel().getColumn(index).setCellEditor(editor);
      }
      index++;
    }
  }

  public GlobTableView setDefaultFont(Font font) {
    defaultFont = font;
    if (table != null) {
      table.setFont(font);
    }
    return this;
  }

  public Font getDefaultFont() {
    return defaultFont;
  }

  public Glob getGlobAt(int index) {
    return tableModel.getValueAt(index, 0);
  }

  public void refresh() {
    ListSelectionModel selectionModel = table.getSelectionModel();
    disableSelectionNotification();
    try {
      GlobList selection = new GlobList();
      for (int index : table.getSelectedRows()) {
        selection.add(tableModel.get(index));
      }
      tableModel.refresh();
      doSelect(selectionModel, selection);
    }
    finally {
      enableSelectionNotification(false);
    }
  }

  public void reset() {
    if (tableModel != null) {
      tableModel.reset();
    }
  }

  private void disableSelectionNotification() {
    selectionEnabled = false;
    table.getSelectionModel().setValueIsAdjusting(true);
  }

  private void enableSelectionNotification(boolean sendPending) {
    if (sendPending) {
      selectionEnabled = true;
      table.getSelectionModel().setValueIsAdjusting(false);
    }
    else {
      table.getSelectionModel().setValueIsAdjusting(false);
      selectionEnabled = true;
    }
  }

  private void doSelect(ListSelectionModel selectionModel, Iterable<Glob> globs) {
    selectionModel.clearSelection();
    for (Glob glob : globs) {
      int index = tableModel.indexOf(glob);
      if (index >= 0) {
        selectionModel.addSelectionInterval(index, index);
      }
    }
  }

  private void initHeader() {
    ColumnHeaderRenderer headerRenderer = new ColumnHeaderRenderer(table, tableModel);

    JTableHeader header = table.getTableHeader();
    header.setDefaultRenderer(
      new LabelTableCellRenderer(chain(headerRenderer, headerLabelCustomizer), headerBackgroundPainter));
    header.setReorderingAllowed(false);
    header.addMouseListener(new GlobTableColumnHeaderMouseListener(table, tableModel));
  }

  private void initPopupFactory() {
    if (popupMenuFactory == null) {
      return;
    }
    table.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        selectRowOnRightClickIfNeeded(table, event);
        showPopup(event);
      }

      public void mouseReleased(MouseEvent event) {
        selectRowOnRightClickIfNeeded(table, event);
        showPopup(event);
      }

      private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
          JPopupMenu popup = popupMenuFactory.createPopup();
          if (popup != null) {
            popup.show(e.getComponent(), e.getX(), e.getY());
          }
        }
      }
    });
  }

  private void selectRowOnRightClickIfNeeded(JTable table, MouseEvent e) {
    int modifiers = e.getModifiers();
    if (((modifiers & MouseEvent.BUTTON3_MASK) == 0) ||
        (modifiers & MouseEvent.CTRL_MASK) != 0) {
      return;
    }
    int selectedRow = table.rowAtPoint(e.getPoint());
    if (selectedRow == -1) {
      return;
    }

    if (!table.isRowSelected(selectedRow)) {
      table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    }
  }

  public void dispose() {
    tableModel.dispose();
    selectionService.removeListener(this);
  }

  private static class ColumnHeaderRenderer implements LabelCustomizer {
    public static Icon NONE = new SortingIcon(SortingIcon.NONE);
    public static Icon UP = new SortingIcon(SortingIcon.UP);
    public static Icon DOWN = new SortingIcon(SortingIcon.DOWN);

    private JTable table;
    private GlobTableModel model;

    public ColumnHeaderRenderer(JTable table, GlobTableModel model) {
      this.table = table;
      this.model = model;
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      JTableHeader header = table.getTableHeader();
      if (header != null) {
        label.setForeground(header.getForeground());
        label.setBackground(header.getBackground());
        label.setFont(header.getFont());
        Object value = header.getColumnModel().getColumn(column).getHeaderValue();
        label.setText((value == null) ? " " : value.toString());
      }

      label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      label.setHorizontalAlignment(JLabel.CENTER);
      label.setHorizontalTextPosition(JLabel.LEFT);

      boolean isSorted = model.isColumnSorted(column);
      boolean ascending = model.isSortAscending();
      Icon sortedIcon = ascending ? UP : DOWN;
      label.setIcon(isSorted ? sortedIcon : NONE);
    }
  }

  private void registerSelectionListener() {
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (!selectionEnabled || table.getSelectionModel().getValueIsAdjusting()) {
          return;
        }
        GlobList selection = getCurrentSelection();
        selectionService.select(selection, type);
      }
    });
  }

  public GlobList getCurrentSelection() {
    GlobList selection = new GlobList();
    for (int index : table.getSelectedRows()) {
      selection.add(tableModel.getValueAt(index, 0));
    }
    return selection;
  }

  private class CompositeRenderer implements TableCellRenderer {
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
      TableCellRenderer renderer = columns.get(column).getRenderer();
      return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
  }
}