package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;

public interface SqlService {

  SqlConnection getDb();

  String getTableName(GlobType globType);

  String getColumnName(Field field);
}
