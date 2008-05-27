package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.Link;

public interface MutableGlobType extends GlobType {
  void addField(AbstractField field);

  void addKey(Field field);

  void addLink(Link link);
}
