package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.sqlstreams.accessors.SqlAccessor;
import org.globsframework.sqlstreams.drivers.jdbc.select.SqlSelectQuery;
import org.globsframework.sqlstreams.exceptions.GlobsSqlException;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.Accessor;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

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
  private SqlSelectQuery query;

  public SqlGlobStream(ResultSet resultSet, Map<Field, SqlAccessor> fieldToAccessorHolder, SqlSelectQuery query) {
    this.resultSet = resultSet;
    this.fieldToAccessorHolder = fieldToAccessorHolder;
    this.query = query;
    for (SqlAccessor sqlAccessor : fieldToAccessorHolder.values()) {
      sqlAccessor.setMoStream(this);
    }
  }

  public boolean next() {
    try {
      rowId++;
      boolean hasNext = resultSet.next();
      if (!hasNext) {
        close();
      }
      return hasNext;
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void close() {
    try {
      resultSet.close();
      query.resultSetClose();
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
      throw new GlobsSqlException(e);
    }
  }

  public String getString(int index) {
    try {
      return (String)resultSet.getObject(index);
    }
    catch (SQLException e) {
      throw new GlobsSqlException(e);
    }
  }

  public Timestamp getTimeStamp(int index) {
    try {
      return resultSet.getTimestamp(index);
    }
    catch (SQLException e) {
      throw new GlobsSqlException(e);
    }
  }

  public byte[] getBytes(int index) {
    try {
      return resultSet.getBytes(index);
    }
    catch (SQLException e) {
      throw new GlobsSqlException(e);
    }
  }

  public Long getLong(int index) {
    try {
      return resultSet.getLong(index);
    }
    catch (SQLException e) {
      throw new GlobsSqlException(e);
    }
  }

  public boolean isNull() {
    try {
      return resultSet.wasNull();
    }
    catch (SQLException e) {
      throw new GlobsSqlException(e);
    }
  }

  public int getCurrentRowId() {
    return rowId;
  }
}
