package org.globsframework.wicket.editors;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.links.FieldMappingFunctor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.wicket.GlobPage;
import org.globsframework.wicket.GlobSession;

import java.util.List;

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
    choice.setLabel(new Model(descriptionService.getLabel(link)));
    choice.setRequired(link.isRequired());
    add(choice);
  }

  private GlobRepository getRepository() {
    return ((GlobPage)getPage()).getRepository();
  }

  private DescriptionService getDescriptionService() {
    return ((GlobSession)getSession()).getDescriptionService();
  }

  private class SelectionModel extends Model {

    public Object getObject() {
      Glob targetGlob = getRepository().find(link.getTargetKey(values));
      if (targetGlob == null) {
        return null;
      }
      return targetGlob.getKey();
    }

    public void setObject(Object targetKey) {
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
