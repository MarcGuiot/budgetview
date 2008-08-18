package org.globsframework.gui.views.impl;

import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.GlobTableColumn;
import org.globsframework.gui.views.GlobViewModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobMatcher;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GlobTableModel extends AbstractTableModel implements SortableTableModel {
  private GlobViewModel model;
  private int sortedColumnIndex = -1;
  private boolean sortAscending = true;
  private GlobType type;
  private GlobRepository repository;
  private List<GlobTableColumn> columns;
  private Comparator<Glob> initialComparator;

  public interface ResetListener {
    void preReset();

    void reset();
  }

  public GlobTableModel(GlobType type, GlobRepository repository,
                        List<GlobTableColumn> columns, final JTable table, final ResetListener resetListener,
                        Comparator<Glob> initialComparator) {
    this.type = type;
    this.repository = repository;
    this.columns = columns;
    this.initialComparator = initialComparator;
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

      public void globListPreReset() {
        resetListener.preReset();
      }

      public void globListReset() {
        TableUtils.stopEditing(table);
        fireTableDataChanged();
        resetListener.reset();
      }
    });
  }

  public void refresh() {
    model.refresh();
    fireTableDataChanged();
  }

  public void reset() {
    model.globsReset(repository, Collections.singleton(type));
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
    return columns.get(column).isEditable();
  }

  public Glob getValueAt(int row, int column) {
    try {
      return model.get(row);
    }
    catch (RuntimeException e) {
      throw e;
    }
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
    model.setFilter(matcher);
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
