package org.crossbowlabs.globs.wicket.editors;

import java.util.List;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.metamodel.links.FieldMappingFunctor;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.KeyBuilder;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.wicket.GlobPage;
import org.crossbowlabs.globs.wicket.GlobSession;
import wicket.Component;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractModel;

public class LinkEditorPanel extends Panel {

  public static String ID = "select";
  private final Link link;
  private final MutableFieldValues values;

  public LinkEditorPanel(String parentId,
                         final String componentId,
                         Link link,
                         MutableFieldValues values,
                         GlobRepository repository,
                         DescriptionService descriptionService) {
    super(parentId);
    this.link = link;
    this.values = values;
    GlobStringifier stringifier = descriptionService.getStringifier(link.getTargetType());
    List targetKeys =
          repository
                .getAll(link.getTargetType())
                .sort(stringifier.getComparator(repository))
                .toKeyList();
    DropDownChoice choice = new DropDownChoice("select", new SelectionModel(), targetKeys, new Renderer()) {
      public String getInputName() {
        return componentId;
      }
    };
    choice.setLabel(new wicket.model.Model(descriptionService.getLabel(link)));
    choice.setRequired(link.isRequired());
    add(choice);
  }

  private GlobRepository getRepository() {
    return ((GlobPage)getPage()).getRepository();
  }

  private DescriptionService getDescriptionService() {
    return ((GlobSession)getSession()).getDescriptionService();
  }

  private class SelectionModel extends AbstractModel {

    public Object getObject(Component component) {
      Glob targetGlob = getRepository().find(link.getTargetKey(values));
      if (targetGlob == null) {
        return null;
      }
      return targetGlob.getKey();
    }

    public void setObject(Component component, Object targetKey) {
      final Glob targetGlob = getRepository().find((Key)targetKey);
      FieldMappingFunctor functor;
      if (targetGlob == null) {
        functor = new FieldMappingFunctor() {
          public void process(Field sourceField, Field targetField) {
            values.setValue(sourceField, null);
          }
        };
      }
      else {
        functor = new FieldMappingFunctor() {
          public void process(Field sourceField, Field targetField) {
            values.setValue(sourceField, targetGlob.getValue(targetField));
          }
        };
      }
      link.apply(functor);
    }
  }

  private class Renderer implements IChoiceRenderer {
    public Object getDisplayValue(Object key) {
      GlobRepository repository = getRepository();
      GlobStringifier stringifier = getDescriptionService().getStringifier(link.getTargetType());
      return stringifier.toString(repository.get((Key)key), repository);
    }

    public String getIdValue(Object object, int index) {
      return String.valueOf(index);
    }
  }
}
