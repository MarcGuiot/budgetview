package org.crossbowlabs.globs.metamodel;

import java.util.Collection;
import org.crossbowlabs.globs.metamodel.properties.Property;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeDependencies;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;

public interface GlobModel extends Iterable<GlobType> {

  Collection<GlobType> getAll();

  GlobType getType(String name) throws ItemNotFound;

  GlobList getConstants();

  GlobTypeDependencies getDependencies();

  <T> Property<GlobType, T> createGlobTypeProperty(String name);

  <T> Property<Field, T> createFieldProperty(String name);
}
