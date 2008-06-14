package org.globsframework.wicket.table.columns;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.form.SubmitButtonPanel;
import wicket.Component;

public class SubmitColumn extends AbstractGlobTableColumn {

  public SubmitColumn(String columnTitle) {
    super(columnTitle);
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {
    return new SubmitButtonPanel(id);
  }
}
