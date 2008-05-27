package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.form.SubmitButtonPanel;
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
