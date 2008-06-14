package org.globsframework.addressbook.model;

import org.globsframework.globs.metamodel.GlobType;
import org.globsframework.globs.metamodel.annotations.Key;
import org.globsframework.globs.metamodel.annotations.Target;
import org.globsframework.globs.metamodel.fields.IntegerField;
import org.globsframework.globs.metamodel.fields.LinkField;
import org.globsframework.globs.metamodel.fields.StringField;
import org.globsframework.globs.metamodel.utils.GlobTypeLoader;

public class Contact {
  public static GlobType TYPE;

  @Key public static IntegerField ID;

  public static StringField FIRST_NAME;
  public static StringField LAST_NAME;
  public static StringField PHONE;
  public static StringField EMAIL;

  static {
    GlobTypeLoader.init(Contact.class);
  }
}
