package org.globsframework.wicket.form;

import org.apache.wicket.markup.html.panel.Panel;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.utils.DefaultFieldValues;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.List;

public class GlobFormBuilder {

  private GlobType type;
  private Key key;
  private MutableFieldValues values;
  private List fields = new ArrayList();
  private DefaultFieldValues defaultValues = new DefaultFieldValues();
  private GlobFormCancelAction cancelAction = GlobFormCancelAction.NULL;

  public static GlobFormBuilder init(GlobType type) {
    return new GlobFormBuilder(type, null, new DefaultFieldValues());
  }

  public static GlobFormBuilder init(Key key, MutableFieldValues values) {
    return new GlobFormBuilder(key.getGlobType(), key, values);
  }

  private GlobFormBuilder(GlobType type, Key key, MutableFieldValues values) {
    this.type = type;
    this.key = key;
    this.values = values;
  }

  public GlobFormBuilder add(Field field) throws InvalidParameter {
    if (!field.getGlobType().equals(type)) {
      throw new InvalidParameter("Field '" + field + "' is not part of type '" + type.getName() + "'");
    }
    if ((key != null) && field.isKeyField()) {
      throw new InvalidParameter("Key field '" + field.getName() + "' cannot be edited in a form");
    }
    fields.add(field);
    return this;
  }

  public GlobFormBuilder add(LinkField field) {
    return add((Link)field);
  }

  public GlobFormBuilder add(Link link) {
    fields.add(link);
    return this;
  }

  public GlobFormBuilder addDefaultValue(Field field, Object value) {
    defaultValues.setValue(field, value);
    return this;
  }

  public GlobFormBuilder setCancelAction(GlobFormCancelAction cancelAction) {
    this.cancelAction = cancelAction;
    return this;
  }

  public Panel create(String id) {
    return new GlobForm(id, type, key, fields, values, defaultValues, cancelAction);
  }
}
