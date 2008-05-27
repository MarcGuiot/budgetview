package org.crossbowlabs.globs.wicket.table.columns;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.annotations.MultiLineText;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.wicket.component.CollapsibleTextPanel;
import wicket.Component;
import wicket.markup.html.basic.Label;

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
