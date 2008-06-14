package org.globsframework.wicket.table.columns;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import wicket.Component;

public abstract class ButtonColumn extends AbstractGlobTableColumn {
  protected String buttonLabel;
  protected final String buttonIdPrefix;

  protected ButtonColumn(String title, String buttonLabel, String buttonIdPrefix) {
    super(title);
    this.buttonLabel = buttonLabel;
    this.buttonIdPrefix = buttonIdPrefix;
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row, GlobRepository repository,
                                DescriptionService descriptionService) {
    return new ButtonPanel(id, buttonLabel, buttonIdPrefix, this, key, fieldValues, rowIndex);
  }

  public abstract void onSubmit(Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                GlobRepository repository,
                                DescriptionService descriptionService);
}
