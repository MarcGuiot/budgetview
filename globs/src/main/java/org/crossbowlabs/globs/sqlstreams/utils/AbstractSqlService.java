package org.crossbowlabs.globs.sqlstreams.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.utils.Strings;

public abstract class AbstractSqlService implements SqlService {

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
