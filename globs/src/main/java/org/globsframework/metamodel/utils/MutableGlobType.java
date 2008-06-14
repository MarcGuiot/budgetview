package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;

public interface MutableGlobType extends GlobType {
  void addField(AbstractField field);

  void addKey(Field field);

  void addLink(Link link);
}
