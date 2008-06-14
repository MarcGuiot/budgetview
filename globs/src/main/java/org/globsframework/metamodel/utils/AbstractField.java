package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.lang.annotation.Annotation;
import java.util.Map;

abstract class AbstractField extends AnnotatedPropertyHolder<Field> implements Field {
  private String name;
  private GlobType globType;
  private Class valueClass;
  private int index;
  private boolean keyField;
  private boolean required;
  private Object defaultValue;

  protected AbstractField(String name,
                          GlobType globType,
                          Class valueClass,
                          Map<Class<? extends Annotation>, Annotation> annotations) {
    super(annotations);
    this.name = name;
    this.globType = globType;
    this.valueClass = valueClass;
    this.required = annotations.containsKey(Required.class);
  }

  public String getName() {
    return name;
  }

  public GlobType getGlobType() {
    return globType;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public boolean isKeyField() {
    return keyField;
  }

  public void setKeyField(boolean keyField) {
    this.keyField = keyField;
    this.required |= keyField;
  }

  public boolean isRequired() {
    return required;
  }

  public void checkValue(Object object) throws InvalidParameter {
    if ((object != null) && (!valueClass.equals(object.getClass()))) {
      throw new InvalidParameter("Value '" + object + "' (" + object.getClass().getName()
                                 + ") is not authorized for field: " + getName() +
                                 " (expected " + valueClass.getName() + ")");
    }
  }

  public Class getValueClass() {
    return valueClass;
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public String toString() {
    return globType.getName() + "." + name;
  }

  public boolean valueEqual(Object o1, Object o2) {
    return Utils.equal(o1, o2);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AbstractField other = (AbstractField)o;
    return globType.equals(other.globType) && name.equals(other.name);
  }

  public int hashCode() {
    int result;
    result = name.hashCode();
    result = 29 * result + globType.hashCode();
    return result;
  }
}
