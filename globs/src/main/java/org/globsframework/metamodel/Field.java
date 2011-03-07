package org.globsframework.metamodel;

import org.globsframework.metamodel.fields.FieldValueVisitor;
import org.globsframework.metamodel.fields.FieldVisitor;
import org.globsframework.metamodel.properties.PropertyHolder;
import org.globsframework.metamodel.utils.Annotable;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.Serializable;

public interface Field extends PropertyHolder<Field>, Annotable, Serializable {
  String getName();

  String getFullName();

  GlobType getGlobType();

  void checkValue(Object object) throws InvalidParameter;

  Class getValueClass();

  boolean isKeyField();

  Object getDefaultValue();

  boolean isRequired();

  void visit(FieldVisitor visitor) throws Exception;

  void safeVisit(FieldVisitor visitor);

  void safeVisit(FieldValueVisitor visitor, Object value);

  /**
   * Returns the index of the field within the containing GlobType. The order of fields
   * within a GlobType is that of the declaration. This method is mainly used for optimization purposes.
   */
  int getIndex();

  boolean valueEqual(Object o1, Object o2);

  Object normalize(Object value);
}
