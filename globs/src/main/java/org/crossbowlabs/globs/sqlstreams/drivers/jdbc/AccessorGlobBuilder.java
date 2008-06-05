package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.impl.DefaultGlob;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.accessors.Accessor;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.Pair;

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
    for (Map.Entry<GlobType, List<Pair<Field, Accessor>>> entry : accessors.values()) {
      DefaultGlob defaultGlob = new DefaultGlob(entry.getKey());
      globs.add(defaultGlob);
      for (Pair<Field, Accessor> pair : entry.getValue()) {
        defaultGlob.setObject(pair.getFirst(), pair.getSecond().getObjectValue());
      }
    }
    return globs;
  }
}
