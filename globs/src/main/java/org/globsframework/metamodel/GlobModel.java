package org.globsframework.metamodel;

import org.globsframework.metamodel.properties.Property;
import org.globsframework.metamodel.utils.GlobTypeDependencies;
import org.globsframework.model.GlobList;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Collection;

public interface GlobModel extends Iterable<GlobType> {

  Collection<GlobType> getAll();

  GlobType getType(String name) throws ItemNotFound;

  GlobList getConstants();

  GlobTypeDependencies getDependencies();

  <T> Property<GlobType, T> createGlobTypeProperty(String name);

  <T> Property<Field, T> createFieldProperty(String name);
}
