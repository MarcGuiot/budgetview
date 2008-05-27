package org.crossbowlabs.globs.gui.views.impl;

import org.crossbowlabs.globs.gui.utils.TableUtils;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GlobTableColumnHeaderMouseListener extends MouseAdapter {
  private JTable table;
  private TableColumnModel columnModel;
  private SortableTableModel model;
  private int x = -1;
  private int y = -1;
  private int columnIndexPressed = -1;

  public GlobTableColumnHeaderMouseListener(JTable table, SortableTableModel model) {
    this.table = table;
    this.model = model;
    columnModel = table.getColumnModel();
  }

  public void mousePressed(MouseEvent e) {
    x = e.getX();
    y = e.getY();
    int columnIndexToAutosize = getColumnIndexToAutosize(x);
    if (columnIndexToAutosize == -1) {
      columnIndexPressed = columnModel.getColumnIndexAtX(e.getX());
    }
    else {
      prepareColumnForAutosize(columnIndexToAutosize);
      prepareColumnForAutosize((columnIndexToAutosize + 1 == table.getColumnCount()) ? 0 : columnIndexToAutosize + 1);
    }
  }

  public void mouseReleased(MouseEvent e) {
    if ((columnIndexPressed == -1) ||
        (getColumnIndexToAutosize(e.getX()) != -1) ||
        (e.getButton() != MouseEvent.BUTTON1)) {
      return;
    }

    if (isAreaNearCoordinates(e.getX(), e.getY())) {
      int modelIndex = columnModel.getColumn(columnIndexPressed).getModelIndex();
      if (modelIndex < 0) {
        return;
      }
      model.sortColumn(modelIndex);
      table.getTableHeader().repaint();
    }
  }

  public void mouseClicked(MouseEvent e) {
    if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2)) {
      TableUtils.autosizeColumn(table, getColumnIndexToAutosize(e.getX()));
    }
  }

  private boolean isAreaNearCoordinates(int x, int y) {
    return ((Math.abs(x - this.x) < 5) && (Math.abs(y - this.y) < 5));
  }

  private void prepareColumnForAutosize(int columnIndexToAutosize) {
    TableColumn column = columnModel.getColumn(columnIndexToAutosize);
    column.setMinWidth(TableUtils.MIN_COLUMN_WIDTH);
    column.setMaxWidth(TableUtils.MAX_COLUMN_WIDTH);
  }

  private int getColumnIndexToAutosize(int x) {
    int columnIndex = columnModel.getColumnIndexAtX(x);
    int size = 0;
    for (int i = 0; i <= columnIndex; i++) {
      size += columnModel.getColumn(i).getWidth();
      if ((i >= columnIndex - 1) && (Math.abs(size - x) < 4)) {
        return columnIndex -= columnIndex - i;
      }
    }
    return -1;
  }
}
