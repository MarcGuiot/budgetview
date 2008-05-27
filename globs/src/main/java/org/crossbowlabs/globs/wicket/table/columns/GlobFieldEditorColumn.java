package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.editors.FieldEditorPanelFactory;
import wicket.Component;

public class GlobFieldEditorColumn extends AbstractGlobTableColumn {
  private final Field field;

  public GlobFieldEditorColumn(Field field, DescriptionService service) {
    super(service.getLabel(field));
    this.field = field;
  }

  public Component getComponent(String id,
                                String tableId,
                                Key key,
                                MutableFieldValues fieldValues,
                                int rowIndex,
                                Component row,
                                GlobRepository repository,
                                DescriptionService descriptionService) {
    String componentId = field.getName() + "_" + rowIndex;
    return FieldEditorPanelFactory.getPanel(id, componentId, field, fieldValues, descriptionService);
  }
}
