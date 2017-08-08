package org.globsframework.sqlstreams.drivers.jdbc.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GeneratedIds {

  public static FieldValues convert(final ResultSet resultSet, GlobType globType) throws SQLException {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    Field[] keyFields = globType.getKeyFields();
    KeyFieldVisitor visitor = new KeyFieldVisitor(resultSet);
    for (int i = 0; i < keyFields.length; i++) {
      if (resultSet.next()) {
        visitor.setIndex(i);
        Field field = keyFields[i];
        field.safeVisit(visitor);
        builder.setValue(field, visitor.value);
      }
    }
    return builder.get();
  }

  private static class KeyFieldVisitor implements FieldVisitor {
    private final ResultSet resultSet;
    private int index;
    private Object value;

    public KeyFieldVisitor(ResultSet resultSet) {
      this.resultSet = resultSet;
    }

    public void setIndex(int i) {
      this.index = i + 1;
    }

    public void visitInteger(IntegerField field) throws Exception {
      value = resultSet.getInt(index);
    }

    public void visitDouble(DoubleField field) throws Exception {
      value = resultSet.getDouble(index);
    }

    public void visitString(StringField field) throws Exception {
      value = resultSet.getString(index);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      value = resultSet.getBoolean(index);
    }

    public void visitLong(LongField field) throws Exception {
      value = resultSet.getLong(index);
    }

    public void visitDate(DateField field) throws Exception {
      value = resultSet.getDate(index);
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      value = resultSet.getTimestamp(index);
    }

    public void visitLink(LinkField field) throws Exception {
      value = resultSet.getInt(index);
    }

    public void visitBlob(BlobField field) throws Exception {
      value = resultSet.getBlob(index);
    }
  }
}
