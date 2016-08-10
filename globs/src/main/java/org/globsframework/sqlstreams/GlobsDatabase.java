package org.globsframework.sqlstreams;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;

public interface GlobsDatabase {

  SqlConnection connect();

  String getTableName(GlobType globType);

  String getColumnName(Field field);
}
