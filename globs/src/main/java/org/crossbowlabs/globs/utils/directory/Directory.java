package org.crossbowlabs.globs.utils.directory;

import org.crossbowlabs.globs.utils.exceptions.ItemAlreadyExists;

public interface Directory {

  <T> T find(Class<T> serviceClass);

  <T> T get(Class<T> serviceClass);

  boolean contains(Class serviceClass);

  void add(Class serviceClass, Object service) throws ItemAlreadyExists;

  void add(Object service) throws ItemAlreadyExists;

}
