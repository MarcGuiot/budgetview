package org.globsframework.sqlstreams.drivers.jdbc.impl;

import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.drivers.jdbc.BlobUpdater;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

public class SqlValueFieldVisitor implements FieldVisitor {
  private PreparedStatement preparedStatement;
  private BlobUpdater blobUpdater;
  private Object value;
  private int index;

  public SqlValueFieldVisitor(PreparedStatement preparedStatement, BlobUpdater blobUpdater) {
    this.preparedStatement = preparedStatement;
    this.blobUpdater = blobUpdater;
  }

  public void setValue(Object value, int index) {
    this.value = value;
    this.index = index;
  }

  public void visitInteger(IntegerField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.INTEGER);
    }
    else {
      preparedStatement.setInt(index, (Integer)value);
    }
  }

  public void visitLink(LinkField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.INTEGER);
    }
    else {
      preparedStatement.setInt(index, (Integer)value);
    }
  }

  public void visitLong(LongField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.BIGINT);
    }
    else {
      preparedStatement.setLong(index, (Long)value);
    }
  }

  public void visitDouble(DoubleField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.DOUBLE);
    }
    else {
      preparedStatement.setDouble(index, (Double)value);
    }
  }

  public void visitString(StringField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.VARCHAR);
    }
    else {
      preparedStatement.setString(index, (String)value);
    }
  }

  public void visitDate(DateField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.DATE);
    }
    else {
      preparedStatement.setDate(index, new java.sql.Date(((Date)value).getTime()));
    }
  }

  public void visitBoolean(BooleanField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.BOOLEAN);
    }
    else {
      preparedStatement.setBoolean(index, (Boolean)value);
    }
  }

  public void visitTimeStamp(TimeStampField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.TIMESTAMP);
    }
    else {
      preparedStatement.setTimestamp(index, new Timestamp(((Date)value).getTime()));
    }
  }

  public void visitBlob(BlobField field) throws Exception {
    if (value == null) {
      preparedStatement.setNull(index, Types.BLOB);
    }
    else {
      blobUpdater.setBlob(preparedStatement, index, ((byte[])value));
    }
  }
}
