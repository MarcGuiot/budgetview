package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.model.Glob;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.Comparator;

public class GlobTableColumn {
  private TableCellRenderer renderer;
  private TableCellEditor editor;
  private Comparator<Glob> comparator;
  private String name;

  public GlobTableColumn(String name, TableCellRenderer renderer, Comparator<Glob> comparator) {
    this(name, renderer, null, comparator);
  }

  public GlobTableColumn(String name, TableCellRenderer renderer, TableCellEditor editor, Comparator<Glob> comparator) {
    this.name = name;
    this.renderer = renderer;
    this.editor = editor;
    this.comparator = comparator;
  }

  public String getName() {
    return name;
  }

  public TableCellRenderer getRenderer() {
    return renderer;
  }

  public TableCellEditor getEditor() {
    return editor;
  }

  public Comparator<Glob> getComparator() {
    return comparator;
  }

  public boolean isEditable() {
    return editor != null;
  }
}
