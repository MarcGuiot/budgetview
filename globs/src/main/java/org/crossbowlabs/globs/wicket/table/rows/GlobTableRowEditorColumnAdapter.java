package org.crossbowlabs.globs.wicket.table.rows;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.table.GlobTableColumn;
import wicket.Component;

import java.util.Comparator;

public class GlobTableRowEditorColumnAdapter implements GlobTableColumn {
  private final String columnName;

  public GlobTableRowEditorColumnAdapter(String columnName) {
    this.columnName = columnName;
  }

  public String getTitle() {
    return columnName;
  }

  public Component getComponent(String id,
                                String tableId, Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {
    return null;
  }

  public Comparator<Glob> getComparator() {
    return null;
  }
}
