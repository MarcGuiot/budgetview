package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.DefaultGlob;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccessorGlobBuilder {
  private MultiMap<GlobType, Pair<Field, Accessor>> accessors = new MultiMap<GlobType, Pair<Field, Accessor>>();

  public AccessorGlobBuilder(GlobStream globStream) {
    for (Field field : globStream.getFields()) {
      accessors.put(field.getGlobType(), new Pair<Field, Accessor>(field, globStream.getAccessor(field)));
    }
  }

  public static AccessorGlobBuilder init(GlobStream globStream) {
    return new AccessorGlobBuilder(globStream);
  }

  public List<Glob> getGlobs() {
    List globs = new ArrayList();
    for (Map.Entry<GlobType, List<Pair<Field, Accessor>>> entry : accessors.entries()) {
      DefaultGlob defaultGlob = new DefaultGlob(entry.getKey());
      globs.add(defaultGlob);
      for (Pair<Field, Accessor> pair : entry.getValue()) {
        defaultGlob.setObject(pair.getFirst(), pair.getSecond().getObjectValue());
      }
    }
    return globs;
  }
}
