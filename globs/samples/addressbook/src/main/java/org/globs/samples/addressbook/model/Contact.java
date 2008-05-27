package org.crossbowlabs.addressbook.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
