package org.globsframework.model.format;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.utils.Strings;

public class GlobDump {

  private Glob glob;
  private StringBuilder builder = new StringBuilder();
  private boolean start = true;

  public static GlobDump init(Glob glob) {
    return new GlobDump(glob);
  }

  private GlobDump(Glob glob) {
    this.glob = glob;
    builder.append(Strings.capitalize(glob.getType().getName())).append(" ");
  }

  public GlobDump add(StringField field) {
    return add(field, glob.get(field));
  }

  public GlobDump add(BooleanField field) {
    Boolean value = glob.get(field);
    return add(field, value == null ? "null" : value.toString());
  }

  public GlobDump add(IntegerField field) {
    return add(field, Integer.toString(glob.get(field)));
  }

  public GlobDump addIfTrue(BooleanField field) {
    if (glob.isTrue(field)) {
      add(field.getName(), "true");
    }
    return this;
  }

  public GlobDump add(Field field, String value) {
    return add(field.getName(), value);
  }

  public GlobDump add(String name, String value) {
    if (!start) {
      builder.append(" - ");
    }
    start = false;
    builder.append(name).append(":").append(value);
    return this;
  }

  public String toString() {
    return builder.toString();
  }
}
