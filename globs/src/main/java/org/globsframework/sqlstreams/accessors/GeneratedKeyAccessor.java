package org.globsframework.sqlstreams.accessors;

import java.sql.ResultSet;

public interface GeneratedKeyAccessor {
  void setResult(ResultSet generatedKeys);
}
