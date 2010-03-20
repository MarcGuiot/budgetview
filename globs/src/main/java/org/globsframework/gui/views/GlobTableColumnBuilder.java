package org.globsframework.gui.views;

import org.globsframework.gui.views.impl.GlobLabelCustomizerFactory;
import org.globsframework.gui.views.impl.LabelTableCellRenderer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.Field;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobTableColumnBuilder {
  private DefaultGlobTableColumn column;
  private DescriptionService descriptionService;
  private GlobRepository repository;

  public static GlobTableColumnBuilder init(GlobRepository repository, Directory directory) {
    return new GlobTableColumnBuilder(repository, directory);
  }

  private GlobTableColumnBuilder(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.descriptionService = directory.get(DescriptionService.class);
    this.column = new DefaultGlobTableColumn();
  }

  public GlobTableColumn getColumn() {
    return column;
  }

  public GlobTableColumnBuilder setName(String name) {
    column.name = name;
    return this;
  }

  public GlobTableColumnBuilder setField(Field field) {
    if (column.name == null) {
      column.name = descriptionService.getLabel(field);
    }

    column.stringifier = descriptionService.getStringifier(field);
    column.labelCustomizers.add(GlobLabelCustomizerFactory.create(field, column.stringifier, repository));
    column.comparator = column.stringifier.getComparator(repository);
    return this;
  }

  public GlobTableColumnBuilder addLabelCustomizer(LabelCustomizer customizer) {
    if (customizer == LabelCustomizer.NULL) {
      return this;
    }
    if (column.renderer != null) {
      throw new InvalidState("LabelCustomizers cannot be set at the same time as a Renderer");
    }
    column.labelCustomizers.add(customizer);
    return this;
  }

  public GlobTableColumnBuilder setBackgroundPainter(CellPainter painter) {
    column.backgroundPainter = painter;
    return this;
  }

  public GlobTableColumnBuilder setRenderer(TableCellRenderer renderer) {
    if (!column.labelCustomizers.isEmpty()) {
      throw new InvalidState("LabelCustomizers cannot be set at the same time as a Renderer");
    }
    column.renderer = renderer;
    return this;
  }

  public GlobTableColumnBuilder setEditor(TableCellEditor editor) {
    column.editor = editor;
    return this;
  }

  public GlobTableColumnBuilder setComparator(Comparator<Glob> comparator) {
    column.comparator = comparator;
    return this;
  }

  public GlobTableColumnBuilder setStringifier(GlobStringifier stringifier) {
    column.stringifier = stringifier;
    return this;
  }

  private static class DefaultGlobTableColumn implements GlobTableColumn {
    private String name;
    private GlobStringifier stringifier;
    private Comparator<Glob> comparator;
    private TableCellRenderer renderer;
    private List<LabelCustomizer> labelCustomizers = new ArrayList<LabelCustomizer>();
    private CellPainter backgroundPainter = CellPainter.NULL;
    private TableCellEditor editor;

    public String getName() {
      return name;
    }

    public TableCellRenderer getRenderer() {
      if (renderer != null) {
        return renderer;
      }

      LabelCustomizer customizer = LabelCustomizers.chain(labelCustomizers);
      renderer = new LabelTableCellRenderer(customizer, backgroundPainter);
      return renderer;
    }

    public TableCellEditor getEditor() {
      return editor;
    }

    public GlobStringifier getStringifier() {
      return stringifier;
    }

    public Comparator<Glob> getComparator() {
      return comparator;
    }

    public boolean isEditable(int row, Glob glob) {
      return editor != null;
    }

    public boolean isReSizable() {
      return true;
    }
  }
}
