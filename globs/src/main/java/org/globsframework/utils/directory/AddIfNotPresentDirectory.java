package org.globsframework.utils.directory;

import org.globsframework.utils.exceptions.ItemAlreadyExists;

public class AddIfNotPresentDirectory implements Directory {
  private Directory directory;

  public AddIfNotPresentDirectory(Directory wrappedDirectory) {
    this.directory = wrappedDirectory;
  }

  public <T> T find(Class<T> serviceClass) {
    return directory.find(serviceClass);
  }

  public <T> T get(Class<T> serviceClass) {
    return directory.get(serviceClass);
  }

  public boolean contains(Class serviceClass) {
    return directory.contains(serviceClass);
  }

  public <T, D extends T> void add(Class<T> serviceClass, D service) throws ItemAlreadyExists {
    if (directory.contains(serviceClass)) {
      return;
    }
    directory.add(serviceClass, service);
  }

  public void add(Object service) throws ItemAlreadyExists {
    if (directory.contains(service.getClass())) {
      return;
    }
    directory.add(service);
  }

  public <T, D extends T> void addFactory(Class<T> serviceClass, Factory<D> factory) throws ItemAlreadyExists {
    directory.addFactory(serviceClass, factory);
  }

  public <T, D extends T> void replace(Class<T> serviceClass, D service) {
    directory.add(serviceClass, service);
  }
}
