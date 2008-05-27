package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;

public interface SqlService {

  SqlConnection getDb();

  String getTableName(GlobType globType);

  String getColumnName(Field field);
}
