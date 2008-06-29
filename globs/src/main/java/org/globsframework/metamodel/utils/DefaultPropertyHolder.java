package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.properties.Property;
import org.globsframework.metamodel.properties.PropertyHolder;
import org.globsframework.utils.exceptions.ItemNotFound;

public abstract class DefaultPropertyHolder<T> implements PropertyHolder<T> {
  private static Object NULL_OBJECT = new Object();
  private Object properties[] = new Object[]{NULL_OBJECT, NULL_OBJECT};

  public <D> void updateProperty(Property<T, D> key, D value) {
    if (properties.length < key.getId()) {
      Object[] tmp = properties;
      properties = new Object[key.getId() + 2];
      int i;
      for (i = 0; i < tmp.length; i++) {
        properties[i] = tmp[i];

      }
      for (; i < properties.length; i++) {
        properties[i] = NULL_OBJECT;
      }
    }
    properties[key.getId()] = value;
  }

  public <D> D getProperty(Property<T, D> key) throws ItemNotFound {
    Object value = properties[key.getId()];
    if (value == NULL_OBJECT) {
      throw new ItemNotFound("No property '" + key.getName() + "' on " + getName());
    }
    return (D)value;
  }

  public <D> D getProperty(Property<T, D> key, D returnValueIfUnset) {
    Object value = properties[key.getId()];
    if (value == NULL_OBJECT) {
      return returnValueIfUnset;
    }
    return (D)value;
  }
}
