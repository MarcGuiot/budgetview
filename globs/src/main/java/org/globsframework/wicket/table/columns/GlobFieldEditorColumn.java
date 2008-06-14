package org.globsframework.wicket.table.columns;

import org.globsframework.metamodel.Field;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.editors.FieldEditorPanelFactory;
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
