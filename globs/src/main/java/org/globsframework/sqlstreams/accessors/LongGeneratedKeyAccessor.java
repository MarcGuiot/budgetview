package org.globsframework.sqlstreams.accessors;

import org.globsframework.sqlstreams.exceptions.SqlException;
import org.globsframework.streams.accessors.LongAccessor;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LongGeneratedKeyAccessor implements GeneratedKeyAccessor, LongAccessor {
  private ResultSet generatedKeys;
  protected Boolean hasGeneratedKey;

  public void setResult(ResultSet generatedKeys) {
    this.generatedKeys = generatedKeys;
    try {
      hasGeneratedKey = generatedKeys.next();
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public Long getLong() {
    return getValue();
  }

  public long getValue() {
    if (hasGeneratedKey) {
      try {
        return generatedKeys.getLong(1);
      }
      catch (SQLException e) {
        throw new SqlException(e);
      }
    }
    else {
      throw new SqlException("No generated key for request : ");
    }
  }

  public Object getObjectValue() {
    return getLong();
  }
}
