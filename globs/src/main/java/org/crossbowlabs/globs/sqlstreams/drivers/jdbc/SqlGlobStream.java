package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.accessors.SqlAccessor;
import org.crossbowlabs.globs.sqlstreams.exceptions.SqlException;
import org.crossbowlabs.globs.streams.GlobStream;
import org.crossbowlabs.globs.streams.accessors.Accessor;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class SqlGlobStream implements GlobStream {
  private ResultSet resultSet;
  private int rowId = 0;
  private Map<Field, SqlAccessor> fieldToAccessorHolder;

  public SqlGlobStream(ResultSet resultSet, Map<Field, SqlAccessor> fieldToAccessorHolder) {
    this.resultSet = resultSet;
    this.fieldToAccessorHolder = fieldToAccessorHolder;
    for (SqlAccessor sqlAccessor : fieldToAccessorHolder.values()) {
      sqlAccessor.setMoStream(this);
    }
  }

  public boolean next() {
    try {
      rowId++;
      return resultSet.next();
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Collection<Field> getFields() {
    return fieldToAccessorHolder.keySet();
  }

  public Accessor getAccessor(Field field) {
    return fieldToAccessorHolder.get(field);
  }

  public Double getDouble(int index) {
    try {
      double aDouble = resultSet.getDouble(index);
      if (aDouble == 0 && resultSet.wasNull()) {
        return null;
      }
      else {
        return aDouble;
      }
    }
    catch (SQLException ex) {
      try {
        Number number = ((Number)resultSet.getObject(index));
        if (number == null) {
          return null;
        }
        if (number instanceof Double) {
          return (Double)number;
        }
        return number.doubleValue();
      }
      catch (SQLException e) {
        throw new UnexpectedApplicationState(e);
      }
    }
  }

  public Date getDate(int index) {
    try {
      return resultSet.getDate(index);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public boolean getBoolean(int index) {
    try {
      return resultSet.getBoolean(index);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public Integer getInteger(int index) {
    try {
      Number number = (Number)resultSet.getObject(index);
      if (number == null) {
        return null;
      }
      if (number instanceof Integer) {
        return (Integer)number;
      }
      return number.intValue();
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public String getString(int index) {
    try {
      return (String)resultSet.getObject(index);
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public Timestamp getTimeStamp(int index) {
    try {
      return resultSet.getTimestamp(index);
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public byte[] getBytes(int index) {
    try {
      return resultSet.getBytes(index);
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public Long getLong(int index) {
    try {
      return resultSet.getLong(index);
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public boolean isNull() {
    try {
      return resultSet.wasNull();
    }
    catch (SQLException e) {
      throw new SqlException(e);
    }
  }

  public int getCurrentRowId() {
    return rowId;
  }
}
