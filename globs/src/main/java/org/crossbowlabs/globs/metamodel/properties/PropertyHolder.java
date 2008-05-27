package org.crossbowlabs.globs.metamodel.properties;

import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public interface PropertyHolder<T> {

  <D> void updateProperty(Property<T, D> key, D value);

  <D> D getProperty(Property<T, D> key) throws ItemNotFound;

  <D> D getProperty(Property<T, D> key, D returnValueIfUnset);
}
