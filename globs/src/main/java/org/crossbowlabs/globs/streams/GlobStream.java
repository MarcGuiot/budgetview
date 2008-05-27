package org.crossbowlabs.globs.streams;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.streams.accessors.Accessor;

import java.util.Collection;

public interface GlobStream {

  boolean next();

  Collection<Field> getFields();

  Accessor getAccessor(Field field);
}
