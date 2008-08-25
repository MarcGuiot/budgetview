package org.globsframework.wicket.table.columns;

import org.apache.wicket.Component;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;

public abstract class DefaultGlobTableColumn extends AbstractGlobTableColumn {

  private String tableId;
  private Key key;
  private MutableFieldValues fieldValues;
  private int rowIndex;
  private Component row;

  protected DefaultGlobTableColumn() {
  }

  protected DefaultGlobTableColumn(String title) {
    super(title);
  }

  public final Component getComponent(String id,
                                      String tableId,
                                      Key key,
                                      MutableFieldValues fieldValues,
                                      int rowIndex,
                                      Component row,
                                      GlobRepository repository,
                                      DescriptionService descriptionService) {
    this.key = key;
    this.fieldValues = fieldValues;
    this.row = row;
    this.rowIndex = rowIndex;
    this.tableId = tableId;
    return getComponent(id, repository, descriptionService);
  }

  protected abstract Component getComponent(String id,
                                            GlobRepository repository,
                                            DescriptionService descriptionService);

  public String getTableId() {
    return tableId;
  }

  public Key getKey() {
    return key;
  }

  public MutableFieldValues getFieldValues() {
    return fieldValues;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public Component getRow() {
    return row;
  }
}
