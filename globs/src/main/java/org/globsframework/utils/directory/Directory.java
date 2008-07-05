package org.globsframework.utils.directory;

import org.globsframework.utils.exceptions.ItemAlreadyExists;

public interface Directory {

  <T> T find(Class<T> serviceClass);

  <T> T get(Class<T> serviceClass);

  boolean contains(Class serviceClass);

  <T, D extends T> void add(Class<T> serviceClass, D service) throws ItemAlreadyExists;

  void add(Object service) throws ItemAlreadyExists;
}
