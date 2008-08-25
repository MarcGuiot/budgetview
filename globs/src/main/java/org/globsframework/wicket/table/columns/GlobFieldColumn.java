package org.globsframework.wicket.table.columns;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.MultiLineText;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Strings;
import org.globsframework.wicket.component.CollapsibleTextPanel;

public class GlobFieldColumn extends AbstractGlobTableColumn {
  private final Field field;
  private static final int MAX_SIZE = 20;

  public GlobFieldColumn(Field field, DescriptionService service) {
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
    GlobStringifier stringifier = descriptionService.getStringifier(field);
    Glob glob = repository.get(key);
    String stringValue = stringifier.toString(glob, repository);
    return isLongText(stringValue) ?
           new CollapsibleTextPanel(id, stringValue, MAX_SIZE) :
           new Label(id, stringValue);
  }

  private boolean isLongText(String stringValue) {
    return field.hasAnnotation(MultiLineText.class)
           && (stringValue.length() > MAX_SIZE
               || stringValue.contains("\n")
               || stringValue.contains(Strings.LINE_SEPARATOR));
  }
}
