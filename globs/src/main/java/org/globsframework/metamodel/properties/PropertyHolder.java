package org.globsframework.metamodel.properties;

import org.globsframework.utils.exceptions.ItemNotFound;

public interface PropertyHolder<T> {

  String getName();

  <D> void updateProperty(Property<T, D> key, D value);

  <D> D getProperty(Property<T, D> key) throws ItemNotFound;

  <D> D getProperty(Property<T, D> key, D returnValueIfUnset);
}
