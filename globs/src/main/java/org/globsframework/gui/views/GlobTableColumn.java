package org.globsframework.gui.views;

import org.globsframework.model.Glob;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public interface GlobTableColumn {

  String getName();

  TableCellRenderer getRenderer();

  TableCellEditor getEditor();

  Comparator<Glob> getComparator();

  boolean isEditable();
}
