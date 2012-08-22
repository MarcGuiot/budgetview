package org.globsframework.gui.views;

import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobStringifier;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public interface GlobTableColumn {

  String getName();

  TableCellRenderer getRenderer();

  TableCellEditor getEditor();

  GlobStringifier getStringifier();

  Comparator<Glob> getComparator();

  boolean isEditable(int row, Glob glob);

  boolean isResizable();
}
