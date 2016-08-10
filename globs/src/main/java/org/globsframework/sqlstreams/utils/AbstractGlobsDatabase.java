package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.utils.Strings;

public abstract class AbstractGlobsDatabase implements GlobsDatabase {

  private static final String[] RESERVED_KEYWORDS = {
    "COUNT", "WHERE"
  };

  public String getTableName(GlobType globType) {
    return toSqlName(globType.getName());
  }

  public String getColumnName(Field field) {
    return toSqlName(field.getName());
  }

  private String toSqlName(String name) {
    String upper = Strings.toNiceUpperCase(name);
    for (String keyword : RESERVED_KEYWORDS) {
      if (upper.equals(keyword)) {
        return "_" + upper + "_";
      }
    }
    return upper;
  }
}
