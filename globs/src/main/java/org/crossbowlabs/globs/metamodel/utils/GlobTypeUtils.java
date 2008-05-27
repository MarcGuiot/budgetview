package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.NamingField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.ItemAmbiguity;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public class GlobTypeUtils {

  public static GlobType getType(Class targetClass) throws InvalidParameter {
    for (java.lang.reflect.Field field : targetClass.getFields()) {
      if (field.getType().equals(GlobType.class)) {
        try {
          return (GlobType)field.get(null);
        }
        catch (Exception e) {
          throw new InvalidParameter("Cannot access GlobType field in class " + targetClass.getName(), e);
        }
      }
    }
    throw new InvalidParameter("Class " + targetClass.getName() + " does not define a GlobType");
  }

  public static StringField findNamingField(GlobType type) {
    Field[] fields = type.getFieldsWithAnnotation(NamingField.class);
    if (fields.length == 1) {
      return stringField(fields, type);
    }
    return null;
  }

  public static StringField getNamingField(GlobType type) throws ItemNotFound, ItemAmbiguity, InvalidParameter {
    Field[] fields = type.getFieldsWithAnnotation(NamingField.class);
    if (fields.length == 0) {
      throw new ItemNotFound("Type '" + type + "' has no naming field");
    }
    if (fields.length > 1) {
      throw new ItemAmbiguity("Type '" + type + "' has too many naming fields");
    }
    return stringField(fields, type);
  }

  private static StringField stringField(Field[] fields, GlobType type) {
    Field namingField = fields[0];
    if (!(namingField instanceof StringField)) {
      throw new InvalidParameter("Naming field of type '" + type + "' should be a StringField");
    }
    return (StringField)namingField;
  }
}
