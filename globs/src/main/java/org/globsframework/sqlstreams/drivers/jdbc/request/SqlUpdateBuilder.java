package org.globsframework.sqlstreams.drivers.jdbc.request;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.SqlUpdateRequest;
import org.globsframework.streams.accessors.*;
import org.globsframework.streams.accessors.utils.*;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SqlUpdateBuilder implements UpdateBuilder {
  private Map<Field, Accessor> values = new HashMap<Field, Accessor>();
  private Connection connection;
  private GlobType globType;
  private SqlService sqlService;
  private Constraint constraint;
  private BlobUpdater blobUpdater;

  public SqlUpdateBuilder(Connection connection, GlobType globType, SqlService sqlService,
                          Constraint constraint, BlobUpdater blobUpdater) {
    this.blobUpdater = blobUpdater;
    this.connection = connection;
    this.globType = globType;
    this.sqlService = sqlService;
    this.constraint = constraint;
  }

  public UpdateBuilder updateUntyped(Field field, final Object value) {
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        update(field, (Integer)value);
      }

      public void visitLong(LongField field) throws Exception {
        update(field, (Long)value);
      }

      public void visitDouble(DoubleField field) throws Exception {
        update(field, (Double)value);
      }

      public void visitString(StringField field) throws Exception {
        update(field, (String)value);
      }

      public void visitDate(DateField field) throws Exception {
        update(field, (Date)value);
      }

      public void visitBoolean(BooleanField field) throws Exception {
        update(field, (Boolean)value);
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        update(field, (Date)value);
      }

      public void visitBlob(BlobField field) throws Exception {
        update(field, (byte[])value);
      }

      public void visitLink(LinkField field) throws Exception {
        update(field, (Integer)value);
      }
    });
    return this;
  }

  public UpdateBuilder updateUntyped(Field field, Accessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(IntegerField field, IntegerAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(IntegerField field, Integer value) {
    return update(field, new ValueIntegerAccessor(value));
  }

  public UpdateBuilder update(LongField field, LongAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(LongField field, Long value) {
    return update(field, new ValueLongAccessor(value));
  }

  public UpdateBuilder update(DoubleField field, DoubleAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(DoubleField field, Double value) {
    return update(field, new ValueDoubleAccessor(value));
  }

  public UpdateBuilder update(DateField field, DateAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(DateField field, Date value) {
    return update(field, new ValueDateAccessor(value));
  }

  public UpdateBuilder update(TimeStampField field, Date value) {
    return update(field, new ValueDateAccessor(value));
  }

  public UpdateBuilder update(TimeStampField field, DateAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(StringField field, StringAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(StringField field, String value) {
    return update(field, new ValueStringAccessor(value));
  }

  public UpdateBuilder update(BooleanField field, BooleanAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(BooleanField field, Boolean value) {
    return update(field, new ValueBooleanAccessor(value));
  }

  public UpdateBuilder update(BlobField field, byte[] value) {
    return update(field, new ValueBlobAccessor(value));
  }

  public UpdateBuilder update(BlobField field, BlobAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(LinkField field, IntegerAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder update(LinkField field, Integer value) {
    return update(field, new ValueIntegerAccessor(value));
  }

  public SqlRequest getRequest() {
    try {
      return new SqlUpdateRequest(globType, constraint, values, connection, sqlService, blobUpdater);
    }
    finally {
      values.clear();
    }
  }
}
