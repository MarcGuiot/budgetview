package org.crossbowlabs.globs.gui.utils;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.Enumeration;

public class TableUtils {
  public static final int DEFAULT_COLUMN_WIDTH = 75;
  public static final int MIN_COLUMN_WIDTH = 15;
  public static final int MAX_COLUMN_WIDTH = Integer.MAX_VALUE;

  private TableUtils() {
  }

  public static Component getRenderedComponentAt(JTable table, int row, int column) {
    return table.
      getCellRenderer(row, column).
      getTableCellRendererComponent(table,
                                    table.getValueAt(row, column),
                                    table.isCellSelected(row, column),
                                    false,
                                    row, column);
  }

  public static Component getRenderedComponent(JTable table, Object value, int row, int column) {
    return table.
      getCellRenderer(0, column).
      getTableCellRendererComponent(table, value, false, false, row, column);
  }

  public static void autosizeColumn(JTable table, int columnIndex) {
    if (columnIndex == -1) {
      return;
    }
    int largestWidth = computeLargestWidth(table, columnIndex);
    setSize(table, columnIndex, largestWidth);

    int newSize = 0;
    for (int i = 0; i < table.getColumnCount(); i++) {
      newSize += table.getColumnModel().getColumn(i).getMinWidth();
      newSize += getIntercellWidth(table);
    }
    if (newSize > table.getWidth()) {
      resetColumnsSize(table);
      TableColumn column = table.getColumnModel().getColumn(columnIndex);
      column.setPreferredWidth(column.getPreferredWidth());
    }
  }

  public static void setSize(JTable table, int columnIndex, int newSize) {
    TableColumn column = table.getColumnModel().getColumn(columnIndex);
    setSize(table, column, newSize);
  }

  public static void setSize(JTable table, TableColumn column, int newSize) {
    newSize += getIntercellWidth(table);
    column.setMinWidth(newSize);
    column.setMaxWidth(newSize);
    column.setPreferredWidth(newSize);
    column.setWidth(column.getPreferredWidth());
  }

  public static int getIntercellWidth(JTable table) {
    return (int)table.getIntercellSpacing().getWidth();
  }

  public static int getPreferredWidth(Component component) {
    return (int)(component.getPreferredSize().getWidth() + 1);
  }

  public static void stopEditing(JTable table) {
    if (table == null) {
      return;
    }
    if (table.isEditing()) {
      table.getCellEditor(table.getEditingRow(), table.getEditingColumn()).stopCellEditing();
    }
  }

  private static int computeLargestWidth(JTable table, int columnIndex) {
    int largestWidth = TableUtils.MIN_COLUMN_WIDTH;
    for (int rowIndex = 0; rowIndex < table.getRowCount(); rowIndex++) {
      Component component = TableUtils.getRenderedComponentAt(table, rowIndex, columnIndex);
      int componentWidth = getPreferredWidth(component);
      if (largestWidth < componentWidth) {
        largestWidth = componentWidth;
      }
    }
    return largestWidth;
  }

  private static void resetColumnsSize(JTable table) {
    Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
    while (columns.hasMoreElements()) {
      TableColumn column = columns.nextElement();
      column.setMinWidth(TableUtils.MIN_COLUMN_WIDTH);
      column.setMaxWidth(TableUtils.MAX_COLUMN_WIDTH);
    }
  }
}
