package org.crossbowlabs.webdemo.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MultiLineText;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Person {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static StringField FIRST_NAME;

  @Required
  public static StringField LAST_NAME;

  public static StringField EMAIL;
  public static DoubleField WEIGHT;
  public static IntegerField AGE;
  public static DateField BIRTH_DATE;
  public static BooleanField REGISTERED;

  @MultiLineText
  public static StringField COMMENT;

  static {
    GlobTypeLoader.init(Person.class);
  }
}
