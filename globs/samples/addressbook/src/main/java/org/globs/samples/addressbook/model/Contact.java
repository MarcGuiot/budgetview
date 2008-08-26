package org.globs.samples.addressbook.model;

import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Contact {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField FIRST_NAME;
  public static StringField LAST_NAME;
  public static StringField PHONE;
  public static StringField EMAIL;

  static {
    GlobTypeLoader.init(Contact.class);
  }
}
