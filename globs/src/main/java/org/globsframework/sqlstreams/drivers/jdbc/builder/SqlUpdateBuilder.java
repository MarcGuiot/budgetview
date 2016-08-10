package org.globsframework.sqlstreams.drivers.jdbc.builder;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.UpdateBuilder;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.request.SqlUpdateRequest;
import org.globsframework.sqlstreams.exceptions.SqlException;
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
  private GlobsDatabase globsDB;
  private Constraint constraint;
  private BlobUpdater blobUpdater;

  public SqlUpdateBuilder(Connection connection, GlobType globType, GlobsDatabase globsDB,
                          Constraint constraint, BlobUpdater blobUpdater) {
    this.blobUpdater = blobUpdater;
    this.connection = connection;
    this.globType = globType;
    this.globsDB = globsDB;
    this.constraint = constraint;
  }

  public UpdateBuilder updateUntyped(Field field, final Object value) {
    field.safeVisit(new FieldVisitor() {
      public void visitInteger(IntegerField field) throws Exception {
        set(field, (Integer)value);
      }

      public void visitLong(LongField field) throws Exception {
        set(field, (Long)value);
      }

      public void visitDouble(DoubleField field) throws Exception {
        set(field, (Double)value);
      }

      public void visitString(StringField field) throws Exception {
        set(field, (String)value);
      }

      public void visitDate(DateField field) throws Exception {
        set(field, (Date)value);
      }

      public void visitBoolean(BooleanField field) throws Exception {
        set(field, (Boolean)value);
      }

      public void visitTimeStamp(TimeStampField field) throws Exception {
        set(field, (Date)value);
      }

      public void visitBlob(BlobField field) throws Exception {
        set(field, (byte[])value);
      }

      public void visitLink(LinkField field) throws Exception {
        set(field, (Integer)value);
      }
    });
    return this;
  }

  public UpdateBuilder updateUntyped(Field field, Accessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(IntegerField field, IntegerAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(IntegerField field, Integer value) {
    return set(field, new ValueIntegerAccessor(value));
  }

  public UpdateBuilder set(LongField field, LongAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(LongField field, Long value) {
    return set(field, new ValueLongAccessor(value));
  }

  public UpdateBuilder set(DoubleField field, DoubleAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(DoubleField field, Double value) {
    return set(field, new ValueDoubleAccessor(value));
  }

  public UpdateBuilder set(DateField field, DateAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(DateField field, Date value) {
    return set(field, new ValueDateAccessor(value));
  }

  public UpdateBuilder set(TimeStampField field, Date value) {
    return set(field, new ValueDateAccessor(value));
  }

  public UpdateBuilder set(TimeStampField field, DateAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(StringField field, StringAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(StringField field, String value) {
    return set(field, new ValueStringAccessor(value));
  }

  public UpdateBuilder set(BooleanField field, BooleanAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(BooleanField field, Boolean value) {
    return set(field, new ValueBooleanAccessor(value));
  }

  public UpdateBuilder set(BlobField field, byte[] value) {
    return set(field, new ValueBlobAccessor(value));
  }

  public UpdateBuilder set(BlobField field, BlobAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(LinkField field, IntegerAccessor accessor) {
    values.put(field, accessor);
    return this;
  }

  public UpdateBuilder set(LinkField field, Integer value) {
    return set(field, new ValueIntegerAccessor(value));
  }

  public SqlRequest getRequest() {
    try {
      return new SqlUpdateRequest(globType, constraint, values, connection, globsDB, blobUpdater);
    }
    finally {
      values.clear();
    }
  }

  public void run() throws SqlException {
    getRequest().run();
  }
}
