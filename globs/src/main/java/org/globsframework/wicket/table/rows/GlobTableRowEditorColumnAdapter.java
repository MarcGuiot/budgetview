package org.globsframework.wicket.table.rows;

import org.apache.wicket.Component;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.table.GlobTableColumn;

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
