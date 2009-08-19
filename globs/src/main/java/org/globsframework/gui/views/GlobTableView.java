package org.globsframework.gui.views;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.impl.GlobTableColumnHeaderMouseListener;
import org.globsframework.gui.views.impl.LabelTableCellRenderer;
import org.globsframework.gui.views.impl.SortableTableModel;
import org.globsframework.gui.views.impl.SortingIcon;
import org.globsframework.gui.views.utils.GlobViewUtils;
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
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class GlobTableView extends AbstractGlobComponentHolder<GlobTableView> implements GlobSelectionListener {
  private List<GlobTableColumn> columns = new ArrayList<GlobTableColumn>();
  private Comparator<Glob> initialComparator;
  private GlobMatcher initialFilter;
  private JTable table;
  private GlobTableModel tableModel;
  private PopupMenuFactory popupMenuFactory;
  private LabelCustomizer headerLabelCustomizer = LabelCustomizer.NULL;
  private CellPainter headerBackgroundPainter = CellPainter.NULL;
  private boolean selectionEnabled = true;
  private Font defaultFont = new JTable().getFont();
  private String name;
  private boolean headerHidden;
  private boolean headerActionsDisabled;
  private CellPainter defaultBackgroundPainter = CellPainter.NULL;
  private LabelCustomizer defaultLabelCustomizer = LabelCustomizer.NULL;

  public static GlobTableView init(GlobType type, GlobRepository globRepository,
                                   Comparator<Glob> comparator, Directory directory) {
    return new GlobTableView(type, globRepository, comparator, directory);
  }

  private GlobTableView(GlobType type, GlobRepository globRepository, Comparator<Glob> comparator, Directory directory) {
    super(type, globRepository, directory);
    this.initialComparator = comparator;
  }

  public GlobTableView addColumn(Field field) {
    return addColumn(field, LabelCustomizer.NULL, defaultBackgroundPainter);
  }

  public GlobTableView addColumn(String name, Field field) {
    startColumn()
      .setName(name)
      .setField(field);
    return this;
  }

  public GlobTableView addColumn(String name, Field field, TableCellEditor editor) {
    startColumn()
      .setName(name)
      .setField(field)
      .setEditor(editor);
    return this;
  }

  public GlobTableView addColumn(Field field, TableCellEditor editor) {
    return addColumn(field, LabelCustomizer.NULL, defaultBackgroundPainter, editor);
  }

  public GlobTableView addColumn(String name, Field field, LabelCustomizer customizer) {
    startColumn()
      .setName(name)
      .setField(field)
      .addLabelCustomizer(customizer)
      .setBackgroundPainter(defaultBackgroundPainter)
      .setEditor(null);
    return this;
  }

  public GlobTableView addColumn(Field field, LabelCustomizer customizer) {
    return addColumn(field, customizer, defaultBackgroundPainter, null);
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
    startColumn()
      .setField(field)
      .addLabelCustomizer(customizer)
      .setBackgroundPainter(backgroundPainter)
      .setEditor(editor);
    return this;
  }

  public GlobTableView addColumn(LinkField link) {
    return addColumn((Link)link);
  }

  public GlobTableView addColumn(Link link) {
    GlobStringifier stringifier = descriptionService.getStringifier(link);
    return addColumn(descriptionService.getLabel(link),
                     new LabelTableCellRenderer(LabelCustomizers.stringifier(stringifier, repository),
                                                defaultBackgroundPainter),
                     stringifier);
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier) {
    return addColumn(name, stringifier, LabelCustomizer.NULL);
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier, LabelCustomizer customizer) {
    return addColumn(name, stringifier, customizer, defaultBackgroundPainter);
  }

  public GlobTableView addColumn(String name, GlobStringifier stringifier, LabelCustomizer customizer, CellPainter backgroundPainter) {
    startColumn()
      .setName(name)
      .setStringifier(stringifier)
      .setBackgroundPainter(backgroundPainter)
      .addLabelCustomizer(chain(LabelCustomizers.stringifier(stringifier, repository), customizer))
      .setComparator(new CompositeComparator<Glob>(stringifier.getComparator(repository), initialComparator));
    return this;
  }

  public GlobTableView addColumn(String name, TableCellRenderer renderer, GlobStringifier stringifier) {
    startColumn()
      .setName(name)
      .setRenderer(renderer)
      .setBackgroundPainter(defaultBackgroundPainter)
      .setStringifier(stringifier)
      .setComparator(new CompositeComparator<Glob>(stringifier.getComparator(repository), initialComparator));
    return this;
  }

  public GlobTableView addColumn(String name, TableCellRenderer renderer, TableCellEditor editor, GlobStringifier stringifier) {
    startColumn()
      .setName(name)
      .setBackgroundPainter(defaultBackgroundPainter)
      .setRenderer(renderer)
      .setEditor(editor)
      .setStringifier(stringifier)
      .setComparator(new CompositeComparator<Glob>(stringifier.getComparator(repository), initialComparator));
    return this;
  }

  public GlobTableView addColumn(GlobTableColumn column) {
    columns.add(column);
    return this;
  }

  public GlobTableColumnBuilder startColumn() {
    GlobTableColumnBuilder builder = GlobTableColumnBuilder.init(repository, directory)
      .setBackgroundPainter(defaultBackgroundPainter)
      .addLabelCustomizer(defaultLabelCustomizer);
    columns.add(builder.getColumn());
    return builder;
  }

  public GlobTableView setHeaderHidden() {
    this.headerHidden = true;
    return this;
  }

  public GlobTableView setHeaderActionsDisabled() {
    this.headerActionsDisabled = true;
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
    if (table == null) {
      this.initialFilter = matcher;
      return this;
    }
    GlobList selection = selectionService.getSelection(type);
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
    return this;
  }

  public void selectionUpdated(GlobSelection selection) {
    Set<Glob> newSelection = new HashSet<Glob>(selection.getAll(type));
    Set<Glob> currentSelection = new HashSet<Glob>(getCurrentSelection());
    if (!newSelection.equals(currentSelection)) {
      select(newSelection, false);
    }
  }

  public void selectFirst() {
    if (tableModel.getRowCount() > 0) {
      select(tableModel.get(0));
    }
    else {
      selectionService.clear(type);
    }
  }

  public void clearSelection() {
    select();

  }

  public void select(Glob... globs) throws ItemNotFound {
    select(Arrays.asList(globs), true);
  }

  public void select(Collection<Glob> globs) throws ItemNotFound {
    select(globs, true);
  }

  void select(Collection<Glob> globs, boolean sendPending) throws ItemNotFound {
    ListSelectionModel selectionModel = table.getSelectionModel();
    try {
      disableSelectionNotification();
      selectionModel.setValueIsAdjusting(true);
      doSelect(selectionModel, globs);
    }
    finally {
      enableSelectionNotification(sendPending);
    }
  }

  public GlobTableView addKeyBinding(KeyStroke keyStroke, String commandName, Action action) {
    getComponent().getInputMap().put(keyStroke, commandName);
    getComponent().getActionMap().put(commandName, action);
    return this;
  }

  public JTable getComponent() {
    if (table == null) {
      table = new JTable();
      table.setAutoscrolls(true);
      tableModel = new GlobTableModel();
      table.setModel(tableModel);
      table.setFont(defaultFont);
      table.setName(name != null ? name : type.getName());
      table.setDefaultRenderer(Glob.class, new CompositeRenderer());
      selectionService.addListener(this, type);
      registerSelectionListener();
      initHeader();
      initPopupFactory();
      initClipboardHandler();
      registerEditors();
      if (initialFilter != null) {
        setFilter(initialFilter);
      }
    }
    return table;
  }

  public GlobTableView setDefaultBackgroundPainter(CellPainter painter) {
    this.defaultBackgroundPainter = painter;
    return this;
  }

  public CellPainter getDefaultBackgroundPainter() {
    return defaultBackgroundPainter;
  }

  public GlobTableView setDefaultLabelCustomizer(LabelCustomizer labelCustomizer) {
    this.defaultLabelCustomizer = labelCustomizer;
    return this;
  }

  private void scrollToRow(int index) {
    Rectangle rect = table.getCellRect(index, 0, true);
    table.scrollRectToVisible(rect);
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

  public GlobTableView setName(String name) {
    this.name = name;
    if (table != null) {
      table.setName(name);
    }
    return this;
  }

  public Font getDefaultFont() {
    return defaultFont;
  }

  public GlobList getGlobs() {
    return tableModel.getAll();
  }

  public Glob getGlobAt(int index) {
    return tableModel.getValueAt(index, 0);
  }

  public int indexOf(Glob glob) {
    return tableModel.indexOf(glob);
  }

  public int getRowCount() {
    return tableModel.getRowCount();
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
    boolean first = true;
    for (Glob glob : globs) {
      int index = tableModel.indexOf(glob);
      if (index >= 0) {
        selectionModel.addSelectionInterval(index, index);
        if (first) {
          scrollToRow(index);
          first = false;
        }
      }
    }
  }

  private void initHeader() {
    if (headerHidden) {
      table.setTableHeader(null);
      return;
    }

    ColumnHeaderRenderer headerRenderer = new ColumnHeaderRenderer(table, tableModel);

    JTableHeader header = table.getTableHeader();
    header.setDefaultRenderer(
      new LabelTableCellRenderer(chain(headerRenderer, headerLabelCustomizer), headerBackgroundPainter));
    header.setReorderingAllowed(false);

    if (!headerActionsDisabled) {
      header.addMouseListener(new GlobTableColumnHeaderMouseListener(table, tableModel));
    }
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
        label.setText(value == null || "".equals(value) ? " " : value.toString());
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

  public class GlobTableModel extends AbstractTableModel implements SortableTableModel {
    private int sortedColumnIndex = -1;
    private boolean sortAscending = true;
    private GlobViewModel model;
    private GlobList currentSelection = GlobList.EMPTY;

    public GlobTableModel() {
      model = new GlobViewModel(type, repository, initialComparator, new GlobViewModel.Listener() {
        public void globInserted(int index) {
          TableUtils.stopEditing(table);
          fireTableRowsInserted(index, index);
        }

        public void globUpdated(int index) {
          TableUtils.stopEditing(table);
          fireTableRowsUpdated(index, index);
        }

        public void globRemoved(int index) {
          TableUtils.stopEditing(table);
          fireTableRowsDeleted(index, index);
        }

        public void globMoved(int previousIndex, int newIndex) {
          selectionEnabled = false;
          GlobViewUtils.updateSelectionAfterItemMoved(table.getSelectionModel(),
                                                      table.getSelectedRows(),
                                                      previousIndex, newIndex);
          selectionEnabled = true;
        }

        public void globListPreReset() {
          currentSelection = getCurrentSelection();
          disableSelectionNotification();
        }

        public void globListReset() {
          TableUtils.stopEditing(table);
          fireTableDataChanged();

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
            scrollToRow(index);
          }
          enableSelectionNotification(false);
          if (newSelection.size() != currentSelection.size()) {
            selectionService.select(newSelection, type);
          }
        }
      });
    }

    public void refresh() {
      model.refresh();
      refreshColumnNames();
      fireTableDataChanged();
    }

    private void refreshColumnNames() {
      if (headerHidden) {
        return;
      }

      int index = 0;
      for (GlobTableColumn column : columns) {
        TableColumn tableColumn = table.getColumnModel().getColumn(index++);
        tableColumn.setHeaderValue(column.getName());
      }
      table.getTableHeader().repaint();
    }

    public void reset() {
      model.globsReset(repository, Collections.singleton(type));
      refreshColumnNames();
    }

    public int getRowCount() {
      return model.size();
    }

    public String getColumnName(int columnIndex) {
      return columns.get(columnIndex).getName();
    }

    public int getColumnCount() {
      return columns.size();
    }

    public Class<?> getColumnClass(int i) {
      return Glob.class;
    }

    public boolean isCellEditable(int row, int column) {
      return columns.get(column).isEditable(row, getValueAt(row, column));
    }

    public Glob getValueAt(int row, int column) {
      return model.get(row);
    }

    public void sortColumn(int modelIndex) {
      if (sortedColumnIndex == modelIndex) {
        if (sortAscending) {
          sortAscending = false;
        }
        else {
          sortAscending = true;
          sortedColumnIndex = -1;
        }
      }
      else {
        sortAscending = true;
        sortedColumnIndex = modelIndex;
      }
      if (sortedColumnIndex == -1) {
        model.sort(initialComparator);
      }
      else {
        Comparator<Glob> columnComparator = columns.get(modelIndex).getComparator();
        model.sort(sortAscending ? columnComparator : Collections.reverseOrder(columnComparator));
      }
    }

    public boolean isColumnSorted(int modelIndex) {
      return modelIndex == sortedColumnIndex;
    }

    public boolean isSortAscending() {
      return sortAscending;
    }

    public int indexOf(Glob glob) {
      return model.indexOf(glob);
    }

    public void setFilter(GlobMatcher matcher) {
      model.setFilter(matcher, true);
    }

    public Glob get(int index) {
      return model.get(index);
    }

    public GlobList getAll() {
      return model.getAll();
    }

    public void dispose() {
      repository.removeChangeListener(model);
    }
  }

  private void initClipboardHandler() {
    addKeyBinding(GuiUtils.ctrl(KeyEvent.VK_C), "Copy", new CopySelectionToClipboardAction());
  }

  private class CopySelectionToClipboardAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      StringBuffer buffer = new StringBuffer();

      boolean firstColumn = true;
      for (GlobTableColumn column : columns) {
        if (!firstColumn) {
          buffer.append("\t");
        }
        firstColumn = false;
        buffer.append(column.getName());
      }
      buffer.append("\n");

      for (Glob glob : getCurrentSelection()) {
        firstColumn = true;
        for (GlobTableColumn column : columns) {
          if (!firstColumn) {
            buffer.append("\t");
          }
          firstColumn = false;
          GlobStringifier stringifier = column.getStringifier();
          buffer.append(stringifier.toString(glob, repository));
        }
        buffer.append("\n");
      }

      StringSelection selection = new StringSelection(buffer.toString());
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(selection, selection);
    }
  }
}